package com.dudek.footballbalancer.service.request;

import com.dudek.footballbalancer.mapper.RoomMapper;
import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewUserResponseDto;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.model.message.RecipientType;
import com.dudek.footballbalancer.model.message.RoomNewMemberRequestEvent;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.entity.request.RequestStatus;
import com.dudek.footballbalancer.model.entity.request.RequestType;
import com.dudek.footballbalancer.repository.RequestRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.dudek.footballbalancer.service.message.MessageService;
import com.dudek.footballbalancer.service.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomRequestService implements RoomService {

    private final RoomRepository roomRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final MessageService messageService;

    @Autowired
    public RoomRequestService(final RoomRepository roomRepository, final RequestRepository requestRepository, final UserRepository userRepository, final RoomMapper roomMapper, final MessageService messageService) {
        this.roomRepository = roomRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.roomMapper = roomMapper;
        this.messageService = messageService;
    }

    @Transactional
    public void sendAddUserRequest(final Long roomId, final Long requesterId) {
        Room targetRoomFromDb = roomRepository.findByIdFetchAdmins(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User requesterFromDb = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Request newRequest = createRequest(targetRoomFromDb, requesterId);
        newRequest.setType(RequestType.NEW_MEMBER);
        Request savedRequest = requestRepository.save(newRequest);

        RoomNewMemberRequestEvent roomNewMemberRequestEvent = new RoomNewMemberRequestEvent(targetRoomFromDb, requesterFromDb,
                                                                                            List.of(RecipientType.ADMIN), RequestStatus.PENDING);
        messageService.sendMessageForRoomAdmins(roomNewMemberRequestEvent, targetRoomFromDb.getId());

        targetRoomFromDb.addRequest(savedRequest);
    }

    @Transactional
    public RoomNewUserResponseDto acceptAddUserRequest(final RoomAddOrRemoveUserRequestDto requestDto) {
        Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(requestDto.getRoomId(), requestDto.getAdminId(), roomRepository);

        Request targetRequestFromDb = requestRepository.findByRequestableAndRequesterIdAndType(targetRoomFromDb, requestDto.getUserId(), RequestType.NEW_MEMBER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.removeRequest(targetRequestFromDb);

        User requesterFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.getUsersInRoom().add(requesterFromDb);

        RoomNewMemberRequestEvent roomNewMemberRequestEvent = new RoomNewMemberRequestEvent(targetRoomFromDb, requesterFromDb,
                                                                                            List.of(RecipientType.ADMIN, RecipientType.USER), RequestStatus.ACCEPTED);
        messageService.sendMessageForRoomAdmins(roomNewMemberRequestEvent, targetRoomFromDb.getId());
        messageService.sendMessageForPrivateUser(roomNewMemberRequestEvent, requesterFromDb.getId());

        return roomMapper.roomToRoomNewUserResponseDto(targetRoomFromDb, requesterFromDb);
    }

    @Transactional
    public void rejectAddUserRequest(final RoomAddOrRemoveUserRequestDto requestDto) {
        Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(requestDto.getRoomId(), requestDto.getAdminId(), roomRepository);

        Request targetRequestFromDb = requestRepository.findByRequestableAndRequesterIdAndType(targetRoomFromDb, requestDto.getUserId(), RequestType.NEW_MEMBER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User requesterFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.removeRequest(targetRequestFromDb);

        RoomNewMemberRequestEvent roomNewMemberRequestEvent = new RoomNewMemberRequestEvent(targetRoomFromDb, requesterFromDb, List.of(RecipientType.USER), RequestStatus.REJECTED);
        messageService.sendMessageForRoomAdmins(roomNewMemberRequestEvent, targetRoomFromDb.getId());
    }

    private Request createRequest(final Room targetRoom, Long requesterId) {
        return Request.builder()
                .requestable(targetRoom)
                .createdAt(LocalDateTime.now())
                .requesterId(requesterId)
                .status(RequestStatus.PENDING)
                .requestableType(targetRoom.getClass().getSimpleName())
                .build();
    }
}
