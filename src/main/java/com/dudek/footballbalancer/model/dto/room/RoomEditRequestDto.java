package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomEditRequestDto {

    @NotNull
    @Positive
    private Long roomAdminId;

    @Size(min = 3, max = 30)
    private String name;

    @Size(min = 3, max = 30)
    private String description;

    private boolean isPublic;

    @Nullable
    private FieldLocationRequestDto location;
}
