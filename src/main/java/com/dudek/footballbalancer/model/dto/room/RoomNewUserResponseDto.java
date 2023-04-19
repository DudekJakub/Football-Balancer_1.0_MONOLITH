package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.user.UserSimpleDtoForRoom;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomNewUserResponseDto {

    private Long id;
    private String name;
    private UserSimpleDtoForRoom newUser;
}
