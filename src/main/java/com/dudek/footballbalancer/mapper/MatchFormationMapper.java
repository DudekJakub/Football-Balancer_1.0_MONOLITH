package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.matchFormation.MatchFormationSimpleDto;
import com.dudek.footballbalancer.model.entity.MatchFormationStrategy;

import java.util.Collection;
import java.util.List;

public interface MatchFormationMapper {

    MatchFormationSimpleDto matchFormationToSimpleDto(final MatchFormationStrategy matchFormationStrategy);
    List<MatchFormationSimpleDto> matchFormationCollectionToSimpleDtoList(final Collection<MatchFormationStrategy> matchFormationStrategyCollection);
}
