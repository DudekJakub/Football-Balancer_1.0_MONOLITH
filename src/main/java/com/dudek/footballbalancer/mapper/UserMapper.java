package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.user.UserDto;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import com.dudek.footballbalancer.model.entity.User;

import java.util.Collection;
import java.util.List;

public interface UserMapper {

    UserSimpleDto userToSimpleDto(final User user);
    List<UserSimpleDto> userCollectionToSimpleDtoList(final Collection<User> userCollection);
    UserDto userToUserDto(User user);
}
