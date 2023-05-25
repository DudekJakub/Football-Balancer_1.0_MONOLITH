package com.dudek.footballbalancer.model.dto.player;

import com.dudek.footballbalancer.model.dto.skill.SkillsUpdateResponseDto;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PlayerEditResponseDto {

    private SkillsUpdateResponseDto updatedSkillsDto;
    private UserSimpleDto updatedLinkedRoomUser;
}
