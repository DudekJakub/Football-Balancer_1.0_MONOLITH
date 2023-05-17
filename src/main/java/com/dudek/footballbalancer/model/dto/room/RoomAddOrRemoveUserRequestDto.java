package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.validation.customAnnotation.RoomAdminId;
import com.dudek.footballbalancer.validation.customAnnotation.RoomId;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

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
    @Positive
    @RoomAdminId
    private Long roomAdminId;

    @NotNull
    @Positive
    @RoomId
    private Long roomId;
}
