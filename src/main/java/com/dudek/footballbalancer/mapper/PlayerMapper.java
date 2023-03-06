package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.player.PlayerSimpleDto;
import com.dudek.footballbalancer.model.entity.Player;

import java.util.Collection;
import java.util.List;

public interface PlayerMapper {

    PlayerSimpleDto playerToSimpleDto(final Player player);
    List<PlayerSimpleDto> playerCollectionToSimpleDtoList(final Collection<Player> playerCollection);
}
