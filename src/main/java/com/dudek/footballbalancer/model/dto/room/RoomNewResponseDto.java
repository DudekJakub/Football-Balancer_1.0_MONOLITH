package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomNewResponseDto {

    private Long id;
    private String name;
    private boolean isPublic;
    private String description;
    private FieldLocationSimpleDto location;
    private List<UserSimpleDto> admins;
    private List<UserSimpleDto> users;
}
