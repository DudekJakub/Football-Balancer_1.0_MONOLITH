package com.dudek.footballbalancer.model.dto.player;

import com.dudek.footballbalancer.model.SexStatus;
import com.dudek.footballbalancer.model.dto.skill.SkillSimpleDto;
import com.dudek.footballbalancer.validation.customAnnotation.RoomAdminId;
import com.dudek.footballbalancer.validation.customAnnotation.RoomId;
import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEditRequestDto {

    @NotNull
    @Positive
    @RoomId
    private Long roomId;

    @NotNull
    @Positive
    @RoomAdminId
    private Long roomAdminId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private SexStatus sex;

    @Nullable
    @Positive
    private Long userToLinkWithId;

    private List<SkillSimpleDto> updatedSkillsList;
}
