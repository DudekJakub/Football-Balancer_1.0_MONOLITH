package com.dudek.footballbalancer.model.dto.player;

import com.dudek.footballbalancer.model.SexStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PlayerSimpleDto {

    private Long id;
    private SexStatus sex;
    private String firstName;
    private String lastName;
    private double generalOverall;
}
