package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.room.RoomEnteredResponseDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewResponseDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.model.dto.room.RoomSimpleDto;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

public interface RoomMapper {

    RoomEnteredResponseDto roomToRoomEnteredResponseDto(Room room);

    RoomNewResponseDto roomToRoomNewResponseDto(Room room);

    RoomNewUserResponseDto roomToRoomNewUserResponseDto(Room room, User newUser);

    List<RoomSimpleDto> roomCollectionToRoomSimpleDtoList(Collection<Room> roomCollection, @Nullable Long userId);
}
