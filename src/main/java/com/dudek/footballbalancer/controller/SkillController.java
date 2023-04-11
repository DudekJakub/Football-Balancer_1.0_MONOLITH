package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.skill.SkillSimpleDto;
import com.dudek.footballbalancer.model.dto.skill.SkillsUpdateRequestDto;
import com.dudek.footballbalancer.model.dto.skill.SkillsUpdateResponseDto;
import com.dudek.footballbalancer.service.SkillService;
import com.dudek.footballbalancer.validation.customAnnotation.RequiresRoomAdmin;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill")
@Tag(name = "Skill", description = "This API provides all operations about player skills.")
public class SkillController {

    private final SkillService skillService;

    @Autowired
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/all-by-playerId")
    public ResponseEntity<List<SkillSimpleDto>> findPlayerSkills(@RequestParam("playerId") Long playerId) {
        return ResponseEntity.ok(skillService.findPlayerSkills(playerId));
    }

    @PutMapping("/all-for-player-by-playerId")
    @RequiresRoomAdmin
    public ResponseEntity<SkillsUpdateResponseDto> updateSkillsForPlayer(@RequestBody SkillsUpdateRequestDto requestDto) {
        return ResponseEntity.ok(skillService.updateSkillsForPlayer(requestDto));
    }
}
