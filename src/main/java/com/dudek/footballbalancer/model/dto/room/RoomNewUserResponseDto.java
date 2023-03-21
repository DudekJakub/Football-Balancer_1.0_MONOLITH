package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import com.dudek.footballbalancer.model.entity.request.RequestStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomNewUserResponseDto {

    private Long id;
    private String name;
    private UserSimpleDto newUser;
}
