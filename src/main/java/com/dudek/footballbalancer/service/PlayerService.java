package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.exception.SkillsMismatchException;
import com.dudek.footballbalancer.exception.UserAlreadyLinkedException;
import com.dudek.footballbalancer.mapper.PlayerMapper;
import com.dudek.footballbalancer.mapper.UserMapper;
import com.dudek.footballbalancer.model.dto.player.PlayerEditRequestDto;
import com.dudek.footballbalancer.model.dto.player.PlayerEditResponseDto;
import com.dudek.footballbalancer.model.dto.player.PlayerNewRequestDto;
import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.dto.skill.SkillsUpdateRequestDto;
import com.dudek.footballbalancer.model.dto.skill.SkillsUpdateResponseDto;
import com.dudek.footballbalancer.model.entity.Player;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.SkillTemplate;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.PlayerRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dudek.footballbalancer.service.util.RequestContextHolderUtil.*;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final PlayerMapper playerMapper;
    private final UserMapper userMapper;
    private final SkillService skillService;

    @Autowired
    public PlayerService(final PlayerRepository playerRepository, final RoomRepository roomRepository, final PlayerMapper playerMapper, final UserMapper userMapper, final SkillService skillService) {
        this.playerRepository = playerRepository;
        this.roomRepository = roomRepository;
        this.playerMapper = playerMapper;
        this.userMapper = userMapper;
        this.skillService = skillService;
    }

    public List<PlayerSimpleDto> getAllPlayersByRoomId(final Long roomId) {
        return playerRepository.findAllByRoomIdFetchRoom(roomId)
                .stream().map(playerMapper::playerToSimpleDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlayerSimpleDto createPlayer(final PlayerNewRequestDto requestDto) {
        Room targetRoomFromDb = roomRepository.findByIdFetchSkillTemplatesAndUsers(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Player newPlayer = Player.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .sex(requestDto.getSex())
                .room(targetRoomFromDb)
                .build();

        if (requestDto.getUserToLinkWithId() != null) {
            User userToLinkWith = targetRoomFromDb.getUsersInRoom()
                    .stream()
                    .filter(u -> Objects.equals(u.getId(), requestDto.getUserToLinkWithId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            if (playerRepository.isAnyPlayerLinkedWithUserByUserId(userToLinkWith.getId())) {
                throw new UserAlreadyLinkedException();
            }

            newPlayer.setUser(userToLinkWith);
        }

        Player savedPlayer = playerRepository.save(newPlayer);

        skillService.assignSkillsForNewPlayer(savedPlayer, targetRoomFromDb);

        setContextAttributes(Map.of(
                TARGET_ROOM, targetRoomFromDb,
                TARGET_PLAYER, newPlayer
        ));

        return playerMapper.playerToSimpleDto(savedPlayer);
    }

    @Transactional
    public PlayerEditResponseDto updatePlayer(final Long playerId, final PlayerEditRequestDto requestDto) {
        Player targetPlayerFromDb = playerRepository.findByIdFetchUser(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Room targetRoomFromDb = roomRepository.findByIdFetchSkillTemplatesAndUsers(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String firstNameBeforeUpdate = targetPlayerFromDb.getFirstName();
        String lastNameBeforeUpdate = targetPlayerFromDb.getLastName();
        User userToLinkWith;
        SkillsUpdateResponseDto skillsUpdateResponseDto;
        PlayerEditResponseDto responseDto = new PlayerEditResponseDto();

        if (requestDto.getUserToLinkWithId() != null && targetPlayerFromDb.getUser() != null && !Objects.equals(requestDto.getUserToLinkWithId(), targetPlayerFromDb.getUser().getId())) {
            userToLinkWith = targetRoomFromDb.getUsersInRoom()
                    .stream()
                    .filter(u -> Objects.equals(u.getId(), requestDto.getUserToLinkWithId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            if (playerRepository.isAnyPlayerLinkedWithUserByUserId(userToLinkWith.getId())) {
                throw new UserAlreadyLinkedException();
            }

            targetPlayerFromDb.setUser(userToLinkWith);
            responseDto.setUpdatedLinkedRoomUser(userMapper.userToSimpleDto(userToLinkWith));
        } else {
            targetPlayerFromDb.setFirstName(requestDto.getFirstName());
            targetPlayerFromDb.setLastName(requestDto.getLastName());
            targetPlayerFromDb.setSex(requestDto.getSex());
            targetPlayerFromDb.setUser(null);
        }

        if (requestDto.getUpdatedSkillsList() != null) {
            Set<Long> skillTemplateIdsFromRequest = requestDto.getUpdatedSkillsList()
                    .stream()
                    .map(s -> s.getSkillTemplate().getId())
                    .collect(Collectors.toSet());
            Set<Long> skillTemplateIdsFromRoom = targetRoomFromDb.getSkillTemplatesForRoom()
                    .stream()
                    .map(SkillTemplate::getId)
                    .collect(Collectors.toSet());

            if (!skillTemplateIdsFromRoom.containsAll(skillTemplateIdsFromRequest)) {
                throw new SkillsMismatchException();
            }

            SkillsUpdateRequestDto skillsUpdateRequestDto = SkillsUpdateRequestDto.builder()
                    .playerId(playerId)
                    .roomAdminId(requestDto.getRoomAdminId())
                    .roomId(requestDto.getRoomId())
                    .updatedSkillsList(requestDto.getUpdatedSkillsList())
                    .build();

            skillsUpdateResponseDto = skillService.updateSkillsForPlayer(skillsUpdateRequestDto);
            responseDto.setUpdatedSkillsDto(skillsUpdateResponseDto);
        }

        setContextAttributes(Map.of(
                PLAYER_FIRSTNAME_BEFORE_UPDATE, firstNameBeforeUpdate,
                PLAYER_LASTNAME_BEFORE_UPDATE, lastNameBeforeUpdate,
                TARGET_ROOM, targetRoomFromDb,
                TARGET_PLAYER, targetPlayerFromDb
        ));

        return responseDto;
    }
}
