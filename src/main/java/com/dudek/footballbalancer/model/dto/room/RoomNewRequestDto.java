package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import reactor.util.annotation.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomNewRequestDto {

    @NotNull
    private Long userRequestSenderId;

    @NotBlank
    @Length(min = 4, max = 50)
    private String roomName;

    @NotBlank
    private String roomPassword;

    private boolean isPublic;

    @Nullable
    private FieldLocationRequestDto location;
}
