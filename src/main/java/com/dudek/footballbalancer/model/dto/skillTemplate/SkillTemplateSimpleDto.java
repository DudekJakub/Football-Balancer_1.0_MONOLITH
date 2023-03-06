package com.dudek.footballbalancer.model.dto.skillTemplate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SkillTemplateSimpleDto {

    private Long id;
    private String name;
    private boolean isActive;
}
