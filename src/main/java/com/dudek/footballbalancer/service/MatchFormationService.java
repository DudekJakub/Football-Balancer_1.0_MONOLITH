package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.model.dto.matchFormation.MatchFormationStrategyNewRequestDto;
import com.dudek.footballbalancer.model.entity.MatchFormationStrategy;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.repository.MatchFormationStrategyRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.HashSet;

@Service
public class MatchFormationService {

    private final MatchFormationStrategyRepository matchFormationStrategyRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public MatchFormationService(final MatchFormationStrategyRepository matchFormationStrategyRepository, final RoomRepository roomRepository) {
        this.matchFormationStrategyRepository = matchFormationStrategyRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public void createMatchFormationStrategyForGivenRoom(final MatchFormationStrategyNewRequestDto requestDto) {
        Room targetRoomFromDb = roomRepository.findById(requestDto.getTargetRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        MatchFormationStrategy newFormationStrategy = MatchFormationStrategy.builder()
                .formation(requestDto.getFormation().getProvidedFormationSchema())
                .fieldSections(new HashSet<>(requestDto.getFormation().getProvidedFieldSections()))
                .room(targetRoomFromDb)
                .build();

        matchFormationStrategyRepository.save(newFormationStrategy);
    }
}
