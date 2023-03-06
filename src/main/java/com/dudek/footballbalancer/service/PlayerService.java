package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.mapper.PlayerMapper;
import com.dudek.footballbalancer.model.dto.player.PlayerNewRequestDto;
import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.entity.Player;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.Skill;
import com.dudek.footballbalancer.model.entity.SkillTemplate;
import com.dudek.footballbalancer.repository.PlayerRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Set;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final RoomRepository roomRepository;
    private final SkillService skillService;

    @Autowired
    public PlayerService(final PlayerRepository playerRepository, final PlayerMapper playerMapper, final RoomRepository roomRepository, final SkillService skillService) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.roomRepository = roomRepository;
        this.skillService = skillService;
    }

    @Transactional
    public PlayerSimpleDto createPlayer(final PlayerNewRequestDto requestDto) {
        Room targetRoom = roomRepository.findByIdFetchSkillTemplates(requestDto.getTargetRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Player newPlayer = Player.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .sex(requestDto.getSex())
                .build();

        Set<SkillTemplate> roomSkillTemplates = targetRoom.getSkillTemplatesForRoom();
        Set<Skill> newSKillsFromPlayer = skillService.createSkillsFromRoomSkillTemplatesForNewPlayerInRoom(roomSkillTemplates, newPlayer);

        newPlayer.setSkills(newSKillsFromPlayer);

        return playerMapper.playerToSimpleDto(playerRepository.save(newPlayer));
    }

    @Transactional
    public void removePlayer(final Long playerId) {

    }
}
