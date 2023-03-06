package com.dudek.footballbalancer.model.dto.player;

import com.dudek.footballbalancer.model.SexStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PlayerNewRequestDto {

    private String firstName;
    private String lastName;
    private SexStatus sex;
    private Long targetRoomId;
}
