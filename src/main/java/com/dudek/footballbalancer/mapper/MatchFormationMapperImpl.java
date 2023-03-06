package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.matchFormation.MatchFormationSimpleDto;
import com.dudek.footballbalancer.model.entity.MatchFormationStrategy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MatchFormationMapperImpl implements MatchFormationMapper {

    @Override
    public MatchFormationSimpleDto matchFormationToSimpleDto(final MatchFormationStrategy matchFormationStrategy) {
        return MatchFormationSimpleDto.builder()
                .id(matchFormationStrategy.getId())
                .formation(matchFormationStrategy.getFormation())
                .fieldSectionsQuantity(matchFormationStrategy.getFieldSectionsQuantity())
                .playersQuantity(matchFormationStrategy.getPlayersQuantity())
                .build();
    }

    @Override
    public List<MatchFormationSimpleDto> matchFormationCollectionToSimpleDtoList(Collection<MatchFormationStrategy> matchFormationStrategyCollection) {
        return matchFormationStrategyCollection.stream()
                .map(this::matchFormationToSimpleDto)
                .collect(Collectors.toList());
    }
}
