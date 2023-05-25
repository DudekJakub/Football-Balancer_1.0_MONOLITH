package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlayerMapperImpl implements PlayerMapper {

    private final UserMapper userMapper;

    @Autowired
    public PlayerMapperImpl(@Lazy UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public PlayerSimpleDto playerToSimpleDto(final Player player) {
        return PlayerSimpleDto.builder()
                .id(player.getId())
                .firstName(player.getFirstName())
                .lastName(player.getLastName())
                .sex(player.getSex())
                .generalOverall(player.getGeneralOverall())
                .roomId(player.getRoom().getId())
                .linkedRoomUser(player.getUser() != null ? userMapper.userToSimpleDto(player.getUser()) : null)
                .build();
    }

    @Override
    public List<PlayerSimpleDto> playerCollectionToSimpleDtoList(Collection<Player> playerList) {
        return playerList.stream()
                .map(this::playerToSimpleDto)
                .collect(Collectors.toList());
    }
}
