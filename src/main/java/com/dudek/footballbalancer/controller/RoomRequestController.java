package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.service.request.RoomRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room/request")
@Tag(name = "Room", description = "This API provides basic operations about room requests such as SEND/PROCESS ADD USER REQUEST etc.")
public class RoomRequestController {

    private final RoomRequestService roomRequestService;

    @Autowired
    public RoomRequestController(final RoomRequestService roomRequestService) {
        this.roomRequestService = roomRequestService;
    }

    @PostMapping("/new-member-request")
    public ResponseEntity<Void> sendAddUserRequest(@RequestParam("roomId") Long roomId, @RequestParam("requesterId") Long requesterId) {
        roomRequestService.sendAddUserRequest(roomId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept-new-member-request")
    public ResponseEntity<Void> acceptAddUserRequest(@RequestBody RoomAddOrRemoveUserRequestDto requestDto) {
        roomRequestService.acceptAddUserRequest(requestDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reject-new-member-request")
    public ResponseEntity<Void> rejectAddUserRequest(@RequestBody RoomAddOrRemoveUserRequestDto requestDto) {
        roomRequestService.rejectAddUserRequest(requestDto);
        return ResponseEntity.noContent().build();
    }
}
