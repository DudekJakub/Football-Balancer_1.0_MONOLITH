package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.skill.SkillSimpleDto;
import com.dudek.footballbalancer.model.entity.Skill;

import java.util.Collection;
import java.util.List;

public interface SkillMapper {

    SkillSimpleDto skillToSimpleDto(final Skill skill);

    List<SkillSimpleDto> skillCollectionToSimpleDtoList(final Collection<Skill> skillCollection);
}
