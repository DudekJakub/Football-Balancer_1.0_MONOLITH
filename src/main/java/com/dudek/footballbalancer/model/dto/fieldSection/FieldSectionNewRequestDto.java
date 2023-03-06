package com.dudek.footballbalancer.model.dto.fieldSection;

import com.dudek.footballbalancer.model.entity.SkillTemplate;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FieldSectionNewRequestDto {

    int playersQuantity;
    List<SkillTemplate> providedSkillTemplatesForSection;
}
