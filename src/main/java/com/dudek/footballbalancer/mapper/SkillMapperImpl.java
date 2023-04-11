package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.skill.SkillSimpleDto;
import com.dudek.footballbalancer.model.entity.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SkillMapperImpl implements SkillMapper {

    private final PlayerMapper playerMapper;
    private final SkillTemplateMapper skillTemplateMapper;

    @Autowired
    public SkillMapperImpl(PlayerMapper playerMapper, SkillTemplateMapper skillTemplateMapper) {
        this.playerMapper = playerMapper;
        this.skillTemplateMapper = skillTemplateMapper;
    }

    @Override
    public SkillSimpleDto skillToSimpleDto(Skill skill) {
        return SkillSimpleDto.builder()
                .id(skill.getId())
                .points(skill.getPoints())
                .player(playerMapper.playerToSimpleDto(skill.getPlayer()))
                .skillTemplate(skillTemplateMapper.skillTemplateToSimpleDto(skill.getSkillTemplate()))
                .build();
    }

    @Override
    public List<SkillSimpleDto> skillCollectionToSimpleDtoList(Collection<Skill> skillCollection) {
        return skillCollection.stream()
                .map(this::skillToSimpleDto)
                .collect(Collectors.toList());
    }
}
