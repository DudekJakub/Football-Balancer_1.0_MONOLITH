package com.dudek.footballbalancer.model.dto.room;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomEnterRequestDto {

    @NotBlank
    private String password;

    @NotNull
    private Long roomId;

    @NotNull
    private Long userId;
}
