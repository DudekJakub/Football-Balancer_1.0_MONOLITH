package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.mapper.PlayerMapper;
import com.dudek.footballbalancer.model.dto.player.PlayerNewRequestDto;
import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.entity.*;
import com.dudek.footballbalancer.model.message.MessageEvent;
import com.dudek.footballbalancer.repository.PlayerRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.dudek.footballbalancer.service.message.MessageCreator;
import com.dudek.footballbalancer.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PlayerMapper playerMapper;
    private final SkillService skillService;
    private final MessageCreator messageCreator;
    private final MessageService messageService;

    @Autowired
    public PlayerService(final PlayerRepository playerRepository, final UserRepository userRepository, final RoomRepository roomRepository, final PlayerMapper playerMapper, final SkillService skillService, final MessageCreator messageCreator, final MessageService messageService) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.playerMapper = playerMapper;
        this.skillService = skillService;
        this.messageCreator = messageCreator;
        this.messageService = messageService;
    }

    public List<PlayerSimpleDto> getAllPlayersByRoomId(final Long roomId) {
        return playerRepository.findAllByRoomIdFetchLinkedUser(roomId)
                .stream().map(playerMapper::playerToSimpleDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlayerSimpleDto createPlayer(final PlayerNewRequestDto requestDto) {
        Room targetRoomFromDb = roomRepository.findByIdFetchSkillTemplates(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Player newPlayer = Player.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .sex(requestDto.getSex())
                .room(targetRoomFromDb)
                .build();

        if (requestDto.getUserToLinkId() != null) {
            User userToLinkWith = userRepository.findById(requestDto.getUserToLinkId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            newPlayer.setUser(userToLinkWith);
        }

        Player savedPlayer = playerRepository.save(newPlayer);

        assignSkillsForNewPlayer(savedPlayer, targetRoomFromDb);

        MessageEvent messageFromRequest = messageCreator.createSimpleMessage(targetRoomFromDb, targetRoomFromDb, "New player have been created: " + newPlayer.getFirstName() + " " + newPlayer.getLastName() + (newPlayer.getUser() != null ? " (linked with user: " + newPlayer.getUser().getUsername() + ")" : ""));
        messageService.sendMessageForRoomUsers(messageFromRequest, targetRoomFromDb.getId());

        return playerMapper.playerToSimpleDto(savedPlayer);
    }

    private void assignSkillsForNewPlayer(final Player savedPlayer, final Room targetRoom) {
        Set<SkillTemplate> roomSkillTemplates = targetRoom.getSkillTemplatesForRoom();
        Set<Skill> newSKillsForPlayer = skillService.createSkillsFromRoomSkillTemplatesForNewPlayerInRoom(roomSkillTemplates, savedPlayer);
        savedPlayer.setSkills(newSKillsForPlayer);
    }
}
