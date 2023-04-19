package com.dudek.footballbalancer.service.room;

import com.dudek.footballbalancer.mapper.RoomMapper;
import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;

@Service
public class RoomUserManagementService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;

    @Autowired
    public RoomUserManagementService(final RoomRepository roomRepository, final UserRepository userRepository, final RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomMapper = roomMapper;
    }

    @Transactional
    public RoomNewUserResponseDto addUserToRoom(final RoomAddOrRemoveUserRequestDto requestDto) {
        final Room targetRoomFromDb = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User userToAddFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.getUsersInRoom().add(userToAddFromDb);

        return roomMapper.roomToRoomNewUserResponseDto(targetRoomFromDb, userToAddFromDb);
    }

    @Transactional
    public Long removeUserFromRoom(final RoomAddOrRemoveUserRequestDto requestDto) {
        final Room targetRoomFromDb = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User userToRemoveFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.getUsersInRoom().remove(userToRemoveFromDb);

        return userToRemoveFromDb.getId();
    }

    public boolean isUserMemberOfRoom(final Long userId, final Long roomId) {
        return roomRepository.isUserMemberOfRoom(userId, roomId);
    }

    public boolean isAdminOfRoom(final Long adminId, final Long roomId) {
        return roomRepository.isAdminOfRoom(adminId, roomId);
    }
}
