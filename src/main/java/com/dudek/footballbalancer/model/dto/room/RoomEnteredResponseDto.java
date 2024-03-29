package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDtoForRoom;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomEnteredResponseDto {

    private Long id;
    private String name;
    private FieldLocationSimpleDto location;
    private boolean isPublic;
    private boolean isRegistrationForNextMatchOpen;
    private String description;
    private LocalDateTime nextMatchDate;
    private LocalDateTime nextMatchRegistrationStartDate;
    private LocalDateTime nextMatchRegistrationEndDate;
    private List<PlayerSimpleDto> players;
    private List<UserSimpleDtoForRoom> users;
    private List<UserSimpleDtoForRoom> admins;
}
