package com.dudek.footballbalancer.model.dto.room;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomAddOrRemoveUserRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Long roomAdminId;

    @NotNull
    private Long roomId;
}
