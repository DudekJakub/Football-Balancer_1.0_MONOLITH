package com.dudek.footballbalancer.model.dto.player;

import com.dudek.footballbalancer.model.SexStatus;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PlayerSimpleDto {

    private Long id;
    private Long roomId;
    private SexStatus sex;
    private String firstName;
    private String lastName;
    private double generalOverall;
    private UserSimpleDto linkedRoomUser;
}
