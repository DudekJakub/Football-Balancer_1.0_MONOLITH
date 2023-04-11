package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.mapper.SkillTemplateMapper;
import com.dudek.footballbalancer.model.dto.skillTemplate.SkillTemplateNewRequestDto;
import com.dudek.footballbalancer.model.dto.skillTemplate.SkillTemplateSimpleDto;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.SkillTemplate;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.SkillTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Set;

@Service
public class SkillTemplateService {

    private final SkillTemplateRepository skillTemplateRepository;
    private final RoomRepository roomRepository;
    private final SkillTemplateMapper skillTemplateMapper;
    private final SkillService skillService;

    @Autowired
    public SkillTemplateService(final SkillTemplateRepository skillTemplateRepository, final RoomRepository roomRepository, final SkillTemplateMapper skillTemplateMapper, final SkillService skillService) {
        this.skillTemplateRepository = skillTemplateRepository;
        this.roomRepository = roomRepository;
        this.skillTemplateMapper = skillTemplateMapper;
        this.skillService = skillService;
    }

    @Transactional
    public SkillTemplateSimpleDto createSkillTemplate(final SkillTemplateNewRequestDto requestDto) {
        Room targetRoom = roomRepository.findByIdFetchPlayersInRoom(requestDto.getTargetRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        SkillTemplate newSkillTemplate = SkillTemplate.builder()
                .name(requestDto.getName())
                .active(requestDto.isActive())
                .defaultSkill(requestDto.isDefaultSkill())
                .room(targetRoom)
                .build();

        SkillTemplate savedNewSkillTemplate = skillTemplateRepository.save(newSkillTemplate);
        skillService.createSkillFromNewSkillTemplateForAllPlayersInRoom(savedNewSkillTemplate);
        return skillTemplateMapper.skillTemplateToSimpleDto(savedNewSkillTemplate);
    }
}
