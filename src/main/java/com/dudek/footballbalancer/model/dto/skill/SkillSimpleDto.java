package com.dudek.footballbalancer.model.dto.skill;

import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.dto.skillTemplate.SkillTemplateSimpleDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SkillSimpleDto {

    private Long id;
    private double points;
    private PlayerSimpleDto player;
    private SkillTemplateSimpleDto skillTemplate;
}
