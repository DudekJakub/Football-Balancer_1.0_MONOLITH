package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.service.room.RoomUserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room/user-management")
@Tag(name = "Room", description = "This API provides user operations regarding the room such as ADD_USER/REMOVE_USER.")
public class RoomUserManagementController {

    private final RoomUserManagementService roomUserManagementService;

    @Autowired
    public RoomUserManagementController(RoomUserManagementService roomUserManagementService) {
        this.roomUserManagementService = roomUserManagementService;
    }

    @GetMapping("/validate-user-for-room")
    public boolean isUserMemberOfRoom(@RequestParam("userId") Long userId, @RequestParam("roomId") Long roomId) {
        return roomUserManagementService.isUserMemberOfRoom(userId, roomId);
    }

    @GetMapping("/validate-admin-for-room")
    public boolean isAdminOfRoom(@RequestParam("adminId") Long adminId, @RequestParam("roomId") Long roomId) {
        return roomUserManagementService.isAdminOfRoom(adminId, roomId);
    }
    @PostMapping("/add")
    public RoomNewUserResponseDto addUserToRoom(@RequestBody RoomAddOrRemoveUserRequestDto requestDto) {
        return roomUserManagementService.addUserToRoom(requestDto);
    }

    @PostMapping("/remove")
    public Long removeUserFromRoom(@RequestBody RoomAddOrRemoveUserRequestDto requestDto) {
        return roomUserManagementService.removeUserFromRoom(requestDto);
    }
}
