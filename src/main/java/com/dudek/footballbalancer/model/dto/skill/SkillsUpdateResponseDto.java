package com.dudek.footballbalancer.model.dto.skill;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SkillsUpdateResponseDto {

    private double updatedPlayerGeneralOverall;
    private List<SkillSimpleDto> updatedSkills;
}
