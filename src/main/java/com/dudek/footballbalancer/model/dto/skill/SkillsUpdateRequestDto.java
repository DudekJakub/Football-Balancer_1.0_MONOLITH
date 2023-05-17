package com.dudek.footballbalancer.model.dto.skill;

import com.dudek.footballbalancer.validation.customAnnotation.RoomAdminId;
import com.dudek.footballbalancer.validation.customAnnotation.RoomId;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SkillsUpdateRequestDto {

    @NotNull
    @Positive
    @RoomId
    private Long roomId;

    @NotNull
    @Positive
    @RoomAdminId
    private Long roomAdminId;

    @NotNull
    @Positive
    private Long playerId;

    @NotNull
    private List<SkillSimpleDto> updatedSkillsList;
}
