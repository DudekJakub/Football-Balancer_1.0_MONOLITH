package com.dudek.footballbalancer.model.dto.player;

import com.dudek.footballbalancer.model.SexStatus;
import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PlayerNewRequestDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private SexStatus sex;

    @NotNull
    @Positive
    private Long roomId;

    @NotNull
    @Positive
    private Long roomAdminId;

    @Nullable
    @Positive
    private Long userToLinkId;
}
