package com.dudek.footballbalancer.model.dto.skillTemplate;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SkillTemplateNewRequestDto {

    @NotBlank
    private String name;

    @NotNull
    @Max(groups = Double.class, value = 2L)
    private double sectionMultiplier;

    private boolean active;
    private boolean defaultSkill;
    private Long targetRoomId;
}
