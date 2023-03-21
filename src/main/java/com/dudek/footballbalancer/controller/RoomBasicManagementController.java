package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.room.*;
import com.dudek.footballbalancer.service.room.RoomBasicManagementService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<RoomSimpleDto>> findPaginated(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(value = "fetchPublic", defaultValue = "true") boolean fetchPublic,
            @RequestParam(value = "userId", defaultValue = "0") long userId
    ) {
        return ResponseEntity.ok(roomBasicManagementService.findPaginated(pageNumber, pageSize, sortDirection, sortField, fetchPublic, userId));
    }

    @GetMapping("/paginated/{userId}")
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
    public ResponseEntity<List<RoomSimpleDto>> findRoomByNameOrLocation(@RequestParam(name = "searchItem") String roomNameOrLocation) {
        return ResponseEntity.ok(roomBasicManagementService.findRoomByNameOrLocation(roomNameOrLocation));
    }

    @PostMapping("/enter")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<RoomEnteredResponseDto> enterRoom(@RequestBody RoomEnterRequestDto requestDto) {
        return ResponseEntity.ok(roomBasicManagementService.enterRoom(requestDto));
    }
    @PostMapping
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<RoomNewResponseDto> createRoom(@RequestBody RoomNewRequestDto requestDto) {
        return ResponseEntity.ok(roomBasicManagementService.createRoom(requestDto));
    }

    @PutMapping("/{roomId}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Void> updateRoom(@PathVariable Long roomId, @RequestBody RoomEditRequestDto requestDto) {
        roomBasicManagementService.updateRoom(roomId, requestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/next-match-all-dates/{roomId}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Void> updateNextMatchDates(@PathVariable Long roomId, @RequestParam(value = "adminId") Long adminId, @RequestBody RoomNewDatesRequestDto requestDto) {
        roomBasicManagementService.updateNextMatchDates(roomId, adminId, requestDto);
        return ResponseEntity.noContent().build();
    }
}
