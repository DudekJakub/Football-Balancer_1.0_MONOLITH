package com.dudek.footballbalancer.model.dto.user;

import com.dudek.footballbalancer.model.Role;
import com.dudek.footballbalancer.model.SexStatus;
import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.entity.Player;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserSimpleDtoForRoom {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private SexStatus sex;
    private Role role;
    private String email;
    private PlayerSimpleDto linkedRoomPlayer;
}
