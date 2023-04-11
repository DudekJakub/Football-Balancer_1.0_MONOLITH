package com.dudek.footballbalancer.model.dto.player;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PlayerLinkRequestDto {

    @NotNull
    @Positive
    private Long roomId;

    @NotNull
    @Positive
    private Long roomAdminId;

    @NotNull
    @Positive
    private Long userId;

    @Nullable
    @Positive
    private Long playerId;
}
