package com.dudek.footballbalancer.model.dto.matchFormation;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MatchFormationSimpleDto {

    private Long id;
    private int formation;
    private int playersQuantity;
    private int fieldSectionsQuantity;
}
