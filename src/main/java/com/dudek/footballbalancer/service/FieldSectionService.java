package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.model.entity.FieldSection;
import com.dudek.footballbalancer.model.entity.SkillTemplate;
import com.dudek.footballbalancer.repository.FieldSectionRepository;
import com.dudek.footballbalancer.repository.SkillTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FieldSectionService {

    private final SkillTemplateRepository skillTemplateRepository;
    private final FieldSectionRepository fieldSectionRepository;

    @Autowired
    public FieldSectionService(final SkillTemplateRepository skillTemplateRepository, final FieldSectionRepository fieldSectionRepository) {
        this.skillTemplateRepository = skillTemplateRepository;
        this.fieldSectionRepository = fieldSectionRepository;
    }

    public FieldSection createFieldSection(int playersQuantity, Map<SkillTemplate, Double> providedSkillTemplatesWithMultipliers) {
        validateListOfSkillTemplatesForGivenSection(providedSkillTemplatesWithMultipliers);

        FieldSection newSection = FieldSection.builder()
                .playersQuantity(playersQuantity)
                .skillTemplatesWithMultipliers(providedSkillTemplatesWithMultipliers)
                .build();

        return fieldSectionRepository.save(newSection);
    }

    private void validateListOfSkillTemplatesForGivenSection(Map<SkillTemplate, Double> providedSkillTemplatesWithMultipliers) {
        List<SkillTemplate> activeAndDefaultSkillTemplatesFromDb = skillTemplateRepository.findAllByActiveAndDefaultSkill(true, true);

        List<SkillTemplate> activeAndDefaultSkillTemplatesFromProvidedList = providedSkillTemplatesWithMultipliers.keySet().stream()
                .filter(SkillTemplate::isActive)
                .filter(SkillTemplate::isDefaultSkill)
                .collect(Collectors.toList());

        if (!new HashSet<>(activeAndDefaultSkillTemplatesFromDb).containsAll(activeAndDefaultSkillTemplatesFromProvidedList)) {
            throw new IllegalArgumentException("Provided skill list doesn't contain all required default skills!");
        }
    }
}
