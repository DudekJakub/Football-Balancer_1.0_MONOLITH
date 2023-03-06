package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.skillTemplate.SkillTemplateSimpleDto;
import com.dudek.footballbalancer.model.entity.SkillTemplate;

import java.util.Collection;
import java.util.List;

public interface SkillTemplateMapper {

    SkillTemplateSimpleDto skillTemplateToSimpleDto(final SkillTemplate skillTemplate);

    List<SkillTemplateSimpleDto> skillTemplateCollectionToSimpleDtoList(final Collection<SkillTemplate> skillTemplateCollection);
}
