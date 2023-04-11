package com.dudek.footballbalancer.model.dto.skill;

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
    private Long roomId;

    @NotNull
    @Positive
    private Long roomAdminId;

    @NotNull
    @Positive
    private Long playerId;

    @NotNull
    private List<SkillSimpleDto> updatedSkillsList;
}
