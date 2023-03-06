package com.dudek.footballbalancer.model.dto.matchFormation;

import com.dudek.footballbalancer.model.Formation;
import com.dudek.footballbalancer.validation.customAnnotation.ValidFormation;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MatchFormationStrategyNewRequestDto {

    private Long targetRoomId;

    @ValidFormation
    private Formation formation;
}
