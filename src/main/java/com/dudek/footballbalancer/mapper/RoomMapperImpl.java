package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.room.RoomEnteredResponseDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewResponseDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.model.dto.room.RoomSimpleDto;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class RoomMapperImpl implements RoomMapper {

    private final FieldLocationMapper fieldLocationMapper;
    private final PlayerMapper playerMapper;
    private final UserMapper userMapper;

    @Autowired
    public RoomMapperImpl(final FieldLocationMapper fieldLocationMapper, final PlayerMapper playerMapper, final UserMapper userMapper) {
        this.fieldLocationMapper = fieldLocationMapper;
        this.playerMapper = playerMapper;
        this.userMapper = userMapper;
    }

    @Override
    public RoomEnteredResponseDto roomToRoomEnteredResponseDto(Room room) {
        return RoomEnteredResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .location(fieldLocationMapper.fieldLocationToSimpleDto(room.getFieldLocation()))
                .isPublic(room.isPublic())
                .isRegistrationForNextMatchOpen(isRegistrationForNextMatchOpen(room))
                .description(room.getDescription())
                .nextMatchDate(room.getNextMatchDate())
                .nextMatchRegistrationStartDate(room.getNextMatchRegistrationStartDate())
                .nextMatchRegistrationEndDate(room.getNextMatchRegistrationEndDate())
                .players(playerMapper.playerCollectionToSimpleDtoList(room.getPlayersInRoom()))
                .users(userMapper.userCollectionToSimpleDtoList(room.getUsersInRoom()))
                .admins(userMapper.userCollectionToSimpleDtoList(room.getAdminsInRoom()))
                .build();
    }

    @Override
    public RoomNewResponseDto roomToRoomNewResponseDto(Room room) {
        return RoomNewResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .isPublic(room.isPublic())
                .description(room.getDescription())
                .admins(userMapper.userCollectionToSimpleDtoList(room.getAdminsInRoom()))
                .users(userMapper.userCollectionToSimpleDtoList(room.getUsersInRoom()))
                .location(fieldLocationMapper.fieldLocationToSimpleDto(room.getFieldLocation()))
                .build();
    }

    @Override
    public RoomNewUserResponseDto roomToRoomNewUserResponseDto(Room room, User newUser) {
        return RoomNewUserResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .newUser(userMapper.userToSimpleDto(newUser))
                .build();
    }

    @Override
    public List<RoomSimpleDto> roomCollectionToRoomSimpleDtoList(Collection<Room> roomCollection, Long userId) {
        Predicate<Room> isUserInRoom = room -> room.getUsersInRoom()
                .stream()
                .anyMatch(user -> user.getId().equals(userId));

        return roomCollection.stream()
                .map(room -> RoomSimpleDto.builder()
                        .id(room.getId())
                        .name(room.getName())
                        .usersInRoomQuantity((long) room.getUsersInRoom().size())
                        .isRegistrationForNextMatchOpen(isRegistrationForNextMatchOpen(room))
                        .isUserInRoom(isUserInRoom.test(room))
                        .isPublic(room.isPublic())
                        .city(room.getFieldLocation().getCity())
                        .build())
                .collect(Collectors.toList());
    }

    private boolean isRegistrationForNextMatchOpen(final Room room) {
        if (room.getNextMatchRegistrationStartDate() != null && room.getNextMatchRegistrationEndDate() != null) {
            return room.getNextMatchRegistrationStartDate().isBefore(LocalDateTime.now()) &&
                   room.getNextMatchRegistrationEndDate().isAfter(LocalDateTime.now());
        }
        return false;
    }
}
