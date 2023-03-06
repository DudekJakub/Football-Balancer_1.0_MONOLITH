package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.user.UserDto;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import com.dudek.footballbalancer.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserSimpleDto userToSimpleDto(User user) {
        return UserSimpleDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .sex(user.getSex())
                .build();
    }

    @Override
    public List<UserSimpleDto> userCollectionToSimpleDtoList(Collection<User> userCollection) {
        return userCollection.stream()
                .map(this::userToSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto userToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .sex(user.getSex())
                .email(user.getEmail())
                .userRole(user.getRole())
                .build();
    }
}
