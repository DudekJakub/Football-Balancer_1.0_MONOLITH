package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.mapper.SkillMapper;
import com.dudek.footballbalancer.model.dto.skill.SkillSimpleDto;
import com.dudek.footballbalancer.model.dto.skill.SkillsUpdateRequestDto;
import com.dudek.footballbalancer.model.dto.skill.SkillsUpdateResponseDto;
import com.dudek.footballbalancer.model.entity.Player;
import com.dudek.footballbalancer.model.entity.Skill;
import com.dudek.footballbalancer.model.entity.SkillTemplate;
import com.dudek.footballbalancer.repository.PlayerRepository;
import com.dudek.footballbalancer.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** internal service, shouldn't be exposed via controller */
@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final PlayerRepository playerRepository;
    private final SkillMapper skillMapper;

    @Autowired
    public SkillService(final SkillRepository skillRepository, final PlayerRepository playerRepository, final SkillMapper skillMapper) {
        this.skillRepository = skillRepository;
        this.playerRepository = playerRepository;
        this.skillMapper = skillMapper;
    }

    public List<SkillSimpleDto> findPlayerSkills(final Long playerId) {
        return skillMapper.skillCollectionToSimpleDtoList(skillRepository.findAllByPlayerIdFetchSkillTemplates(playerId));
    }

    @Transactional
    public SkillsUpdateResponseDto updateSkillsForPlayer(final SkillsUpdateRequestDto requestDto) {
        Player targetPlayerFromDb = playerRepository.findByIdFetchSkills(requestDto.getPlayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Map<Long, SkillSimpleDto> updatedSkillsMap = requestDto.getUpdatedSkillsList()
                        .stream()
                                .collect(Collectors.toMap(SkillSimpleDto::getId, Function.identity()));

        targetPlayerFromDb.getSkills()
                        .forEach(skill -> {
                            SkillSimpleDto updatedSkill = updatedSkillsMap.get(skill.getId());
                            if (updatedSkill != null) {
                                skill.setPoints(updatedSkill.getPoints());
                            }
                        });

        double arithmeticAverageFromSkillPoints = targetPlayerFromDb.getSkills()
                .stream()
                .map(Skill::getPoints)
                .reduce(0.0, Double::sum) / targetPlayerFromDb.getSkills().size();

        targetPlayerFromDb.setGeneralOverall(arithmeticAverageFromSkillPoints);

        return SkillsUpdateResponseDto.builder()
                .updatedPlayerGeneralOverall(targetPlayerFromDb.getGeneralOverall())
                .updatedSkills(skillMapper.skillCollectionToSimpleDtoList(targetPlayerFromDb.getSkills()))
                .build();
    }

    @Transactional
    void createSkillFromNewSkillTemplateForAllPlayersInRoom(final SkillTemplate skillTemplate) {
        Set<Player> targetPlayersForNewSkill = new HashSet<>(skillTemplate.getRoom().getPlayersInRoom());

        Supplier<Skill> newSkillSupplier = () -> Skill.builder()
                .skillTemplate(skillTemplate)
                .player(null)
                .points(0)
                .build();

        targetPlayersForNewSkill.forEach(player -> {
            Skill newSkill = newSkillSupplier.get();
            Skill savedSkill = skillRepository.save(newSkill);
            player.getSkills().add(savedSkill);
        });
    }

    @Transactional
    Set<Skill> createSkillsFromRoomSkillTemplatesForNewPlayerInRoom(final Set<SkillTemplate> skillTemplateFromRoom, final Player targetPlayer) {
        Supplier<Skill> newSkillSupplier = () -> Skill.builder()
                .skillTemplate(null)
                .player(targetPlayer)
                .points(0)
                .build();

        return skillTemplateFromRoom.stream()
                .map(skillTemplate -> {
                    Skill newSkill = newSkillSupplier.get();
                    newSkill.setSkillTemplate(skillTemplate);
                    return skillRepository.save(newSkill);
                }).collect(Collectors.toSet());
    }
}
