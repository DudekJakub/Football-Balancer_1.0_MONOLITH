package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.model.entity.Player;
import com.dudek.footballbalancer.model.entity.Skill;
import com.dudek.footballbalancer.model.entity.SkillTemplate;
import com.dudek.footballbalancer.repository.PlayerRepository;
import com.dudek.footballbalancer.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** internal service, shouldn't be exposed via controller */
@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public SkillService(final SkillRepository skillRepository, final PlayerRepository playerRepository) {
        this.skillRepository = skillRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    void createSkillFromNewSkillTemplateForAllPlayersInRoom(final SkillTemplate skillTemplate) {
        Set<Player> targetPlayersForNewSkill = skillTemplate.getRooms().stream()
                .flatMap(room -> room.getPlayersInRoom().stream())
                .collect(Collectors.toSet());

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
