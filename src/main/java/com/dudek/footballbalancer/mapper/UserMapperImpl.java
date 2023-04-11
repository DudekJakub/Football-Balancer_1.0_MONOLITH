package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDtoForRoom;
import com.dudek.footballbalancer.model.entity.Player;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    private final PlayerMapper playerMapper;

    @Autowired
    public UserMapperImpl(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    @Override
    public UserSimpleDto userToSimpleDto(User user) {
        return UserSimpleDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .sex(user.getSex())
                .build();
    }

    @Override
    public UserSimpleDtoForRoom userToSimpleDtoForRoom(final User user, final Room room) {
        UserSimpleDtoForRoom userSimpleDtoForRoom = UserSimpleDtoForRoom.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .sex(user.getSex())
                .build();

        room.getPlayersInRoom()
                .stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(user.getId()))
                .findFirst().ifPresent(player -> userSimpleDtoForRoom.setLinkedRoomPlayer(playerMapper.playerToSimpleDto(player)));

        return userSimpleDtoForRoom;
    }

    @Override
    public List<UserSimpleDtoForRoom> userCollectionToSimpleDtoForRoomList(final Collection<User> userCollection, final Room room) {
        return userCollection.stream()
                .map(u -> userToSimpleDtoForRoom(u, room))
                .collect(Collectors.toList());
    }
}
