package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDtoForRoom;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;

import java.util.Collection;
import java.util.List;

public interface UserMapper {

    UserSimpleDto userToSimpleDto(final User user);

    UserSimpleDtoForRoom userToSimpleDtoForRoom(final User user, final Room room);
    List<UserSimpleDtoForRoom> userCollectionToSimpleDtoForRoomList(final Collection<User> userCollection, final Room room);
}
