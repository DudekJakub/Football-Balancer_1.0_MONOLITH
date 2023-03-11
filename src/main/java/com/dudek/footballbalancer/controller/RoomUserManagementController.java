package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.service.room.RoomUserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/room/user-management")
@Tag(name = "Room", description = "This API provides user operations regarding the room such as ADD_USER/REMOVE_USER.")
public class RoomUserManagementController {

    private final RoomUserManagementService roomUserManagementService;

    @Autowired
    public RoomUserManagementController(RoomUserManagementService roomUserManagementService) {
        this.roomUserManagementService = roomUserManagementService;
    }

    @PostMapping("/add")
    public RoomNewUserResponseDto addUserToRoom(final RoomAddOrRemoveUserRequestDto requestDto) {
        return roomUserManagementService.addUserToRoom(requestDto);
    }

    @PostMapping("/remove")
    public Long removeUserFromRoom(final RoomAddOrRemoveUserRequestDto requestDto) {
        return roomUserManagementService.removeUserFromRoom(requestDto);
    }
}
