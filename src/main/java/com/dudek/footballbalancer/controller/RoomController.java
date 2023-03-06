package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.room.*;
import com.dudek.footballbalancer.service.RoomService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")
@Tag(name = "Room", description = "This API provides all operations about room.")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(final RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/paginated")
    public List<RoomSimpleDto> findPaginated(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(value = "fetchPublic", defaultValue = "true") boolean fetchPublic,
            @RequestParam(value = "userId", defaultValue = "0", required = false) long userId
    ) {
        return roomService.findPaginated(pageNumber, pageSize, sortDirection, sortField, fetchPublic, userId);
    }

    @GetMapping("/paginated/{userId}")
    public List<RoomSimpleDto> findPaginatedByUserId(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection,
            @PathVariable("userId") Long userId
    ) {
        return roomService.findPaginatedByUserId(pageNumber, pageSize, sortDirection, sortField, userId);
    }

    @GetMapping("/search")
    public List<RoomSimpleDto> findRoomByNameOrLocation(@RequestParam(name = "searchItem") String roomNameOrLocation) {
        return roomService.findRoomByNameOrLocation(roomNameOrLocation);
    }

    @PostMapping("/enter")
    @SecurityRequirement(name = "JWT")
    public RoomEnteredResponseDto enterRoom(@RequestBody RoomEnterRequestDto requestDto) {
        return roomService.enterRoom(requestDto);
    }

    @PostMapping
    @SecurityRequirement(name = "JWT")
    public RoomNewResponseDto createRoom(@RequestBody RoomNewRequestDto requestDto) {
        return roomService.createRoom(requestDto);
    }

    @PutMapping("/{roomId}")
    @SecurityRequirement(name = "JWT")
    public void updateRoom(@PathVariable Long roomId, @RequestBody RoomEditRequestDto requestDto) {
        roomService.updateRoom(roomId, requestDto);
    }

    @PatchMapping("/next-match-date/{roomId}")
    @SecurityRequirement(name = "JWT")
    public void updateNextMatchDate(@PathVariable Long roomId, @RequestParam(value = "adminId") Long adminId, @RequestParam(value = "date") @DateTimeFormat(pattern = "MMMM dd yyyy'T'HH:mm:ss.SSS'Z'") String dateString) {
        roomService.updateNextMatchDate(roomId, adminId, dateString);
    }
}
