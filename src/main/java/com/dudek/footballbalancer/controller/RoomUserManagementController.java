package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.service.room.RoomUserManagementService;
import com.dudek.footballbalancer.validation.customAnnotation.RequiresRoomAdmin;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room/user-management")
@Tag(name = "Room", description = "This API provides user operations regarding the room such as ADD/EDIT/REMOVE USER.")
public class RoomUserManagementController {

    private final RoomUserManagementService roomUserManagementService;

    @Autowired
    public RoomUserManagementController(RoomUserManagementService roomUserManagementService) {
        this.roomUserManagementService = roomUserManagementService;
    }

    @GetMapping("/validate-user-for-room")
    public ResponseEntity<Boolean> isUserMemberOfRoom(@RequestParam("userId") Long userId, @RequestParam("roomId") Long roomId) {
        return ResponseEntity.ok(roomUserManagementService.isUserMemberOfRoom(userId, roomId));
    }

    @GetMapping("/validate-admin-for-room")
    public ResponseEntity<Boolean> isAdminOfRoom(@RequestParam("adminId") Long adminId, @RequestParam("roomId") Long roomId) {
        return ResponseEntity.ok(roomUserManagementService.isAdminOfRoom(adminId, roomId));
    }
    @PostMapping("/add")
    @RequiresRoomAdmin
    public ResponseEntity<RoomNewUserResponseDto> addUserToRoom(@RequestBody RoomAddOrRemoveUserRequestDto requestDto) {
        return ResponseEntity.ok(roomUserManagementService.addUserToRoom(requestDto));
    }

    @PostMapping("/remove")
    @RequiresRoomAdmin
    public ResponseEntity<Long> removeUserFromRoom(@RequestBody RoomAddOrRemoveUserRequestDto requestDto) {
        return ResponseEntity.ok(roomUserManagementService.removeUserFromRoom(requestDto));
    }
}
