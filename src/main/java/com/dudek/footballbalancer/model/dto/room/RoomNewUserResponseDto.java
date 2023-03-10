package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomNewUserResponseDto {

    private Long id;
    private String name;
    private UserSimpleDto newUser;
}
