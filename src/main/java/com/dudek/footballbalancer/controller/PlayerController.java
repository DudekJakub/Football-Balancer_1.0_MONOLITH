package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.player.PlayerEditRequestDto;
import com.dudek.footballbalancer.model.dto.player.PlayerEditResponseDto;
import com.dudek.footballbalancer.model.dto.player.PlayerNewRequestDto;
import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.service.PlayerService;
import com.dudek.footballbalancer.validation.customAnnotation.RequiresRoomAdmin;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/player")
@Tag(name = "Player", description = "This API provides all operations about player.")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(final PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/all-by-room-id")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<PlayerSimpleDto>> allPlayersByRoomId(@RequestParam Long roomId) {
        return ResponseEntity.ok(playerService.getAllPlayersByRoomId(roomId));
    }

    @PostMapping
    @RequiresRoomAdmin
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PlayerSimpleDto> createPlayer(@RequestBody PlayerNewRequestDto requestDto) {
        return ResponseEntity.ok(playerService.createPlayer(requestDto));
    }

    @PutMapping("/{playerId}")
    @RequiresRoomAdmin
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PlayerEditResponseDto> updatePlayer(@PathVariable Long playerId, @RequestBody PlayerEditRequestDto requestDto) {
        return ResponseEntity.ok(playerService.updatePlayer(playerId, requestDto));
    }
}
