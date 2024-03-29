package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.room.*;
import com.dudek.footballbalancer.service.room.RoomBasicManagementService;
import com.dudek.footballbalancer.validation.customAnnotation.RequiresRoomAdmin;
import com.dudek.footballbalancer.validation.customAnnotation.RoomId;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room/basic-management")
@Tag(name = "Room", description = "This API provides basic operations about room such as CREATE/ENTER/UPDATE/FIND.")
public class RoomBasicManagementController {

    private final RoomBasicManagementService roomBasicManagementService;

    @Autowired
    public RoomBasicManagementController(final RoomBasicManagementService roomBasicManagementService) {
        this.roomBasicManagementService = roomBasicManagementService;
    }

    @GetMapping("/paginated")
    public ResponseEntity<List<RoomSimpleDto>> findPaginatedWithoutUserOwned(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(value = "fetchPublic", defaultValue = "true") boolean fetchPublic,
            @RequestParam(value = "userId", defaultValue = "0") long userId
    ) {
        return ResponseEntity.ok(roomBasicManagementService.findPaginatedWithoutUserOwned(pageNumber, pageSize, sortDirection, sortField, fetchPublic, userId));
    }

    @GetMapping("/paginated/{userId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<RoomSimpleDto>> findPaginatedByUserId(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection,
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(roomBasicManagementService.findPaginatedByUserId(pageNumber, pageSize, sortDirection, sortField, userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomSimpleDto>> findRoomByNameOrLocation(@RequestParam("searchItem") String roomNameOrLocation, @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(roomBasicManagementService.findRoomByNameOrLocation(roomNameOrLocation, userId));
    }

    @PostMapping("/enter")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<RoomEnteredResponseDto> enterRoom(@RequestBody RoomEnterRequestDto requestDto) {
        return ResponseEntity.ok(roomBasicManagementService.enterRoom(requestDto));
    }
    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<RoomNewResponseDto> createRoom(@RequestBody RoomNewRequestDto requestDto) {
        return ResponseEntity.ok(roomBasicManagementService.createRoom(requestDto));
    }

    @PutMapping("/{roomId}")
    @RequiresRoomAdmin
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<RoomEditResponseDto> updateRoom(@PathVariable @RoomId Long roomId, @RequestBody RoomEditRequestDto requestDto) {
        return ResponseEntity.ok(roomBasicManagementService.updateRoom(roomId, requestDto));
    }

    @PatchMapping("/next-match-all-dates/{roomId}")
    @RequiresRoomAdmin
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> updateNextMatchDates(@PathVariable @RoomId Long roomId, @RequestBody RoomNewDatesRequestDto requestDto) {
        roomBasicManagementService.updateNextMatchDates(roomId, requestDto);
        return ResponseEntity.noContent().build();
    }
}
