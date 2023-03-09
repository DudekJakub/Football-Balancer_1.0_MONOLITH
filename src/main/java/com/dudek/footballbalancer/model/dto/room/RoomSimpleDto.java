package com.dudek.footballbalancer.model.dto.room;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomSimpleDto {

    private Long id;
    private String name;
    private Long usersInRoomQuantity;
    private String city;
    private boolean isRegistrationForNextMatchOpen;
    private boolean isUserInRoom;
    private boolean isPublic;
}
