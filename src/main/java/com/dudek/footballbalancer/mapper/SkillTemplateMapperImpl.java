package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.skillTemplate.SkillTemplateSimpleDto;
import com.dudek.footballbalancer.model.entity.SkillTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SkillTemplateMapperImpl implements SkillTemplateMapper {

    @Override
    public SkillTemplateSimpleDto skillTemplateToSimpleDto(SkillTemplate skillTemplate) {
        return SkillTemplateSimpleDto.builder()
                .id(skillTemplate.getId())
                .name(skillTemplate.getName())
                .isActive(skillTemplate.isActive())
                .build();
    }

    @Override
    public List<SkillTemplateSimpleDto> skillTemplateCollectionToSimpleDtoList(Collection<SkillTemplate> skillTemplateCollection) {
        return skillTemplateCollection.stream()
                .map(this::skillTemplateToSimpleDto)
                .collect(Collectors.toList());
    }
}
