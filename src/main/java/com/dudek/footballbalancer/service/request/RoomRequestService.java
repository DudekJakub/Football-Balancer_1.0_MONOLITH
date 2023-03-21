package com.dudek.footballbalancer.service.request;

import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.entity.request.RequestStatus;
import com.dudek.footballbalancer.model.entity.request.RequestType;
import com.dudek.footballbalancer.model.message.MessageEvent;
import com.dudek.footballbalancer.model.message.RecipientType;
import com.dudek.footballbalancer.repository.RequestRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.dudek.footballbalancer.service.message.MessageCreator;
import com.dudek.footballbalancer.service.message.MessageService;
import com.dudek.footballbalancer.service.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoomRequestService implements RoomService {

    private final RoomRepository roomRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final MessageCreator messageCreator;
    private final MessageService messageService;

    @Autowired
    public RoomRequestService(final RoomRepository roomRepository, final RequestRepository requestRepository, final UserRepository userRepository,
                              final MessageCreator messageCreator, final MessageService messageService) {
        this.roomRepository = roomRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.messageCreator = messageCreator;
        this.messageService = messageService;
    }

    @Transactional
    public void sendAddUserRequest(final Long roomId, final Long requesterId) {
        Room targetRoomFromDb = roomRepository.findByIdFetchAdmins(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User requesterFromDb = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Optional<Request> newMemberRequestFromDb = requestRepository.findByRequestableAndRequesterIdAndType(targetRoomFromDb, requesterId, RequestType.NEW_MEMBER);

        if (newMemberRequestFromDb.isPresent() && !newMemberRequestFromDb.get().getStatus().equals(RequestStatus.REJECTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already sent or processed!");
        }

        Request newMemberRequest = createRequest(targetRoomFromDb, requesterFromDb.getId(), requesterFromDb.getUsername(), RequestType.NEW_MEMBER);
        Request savedNewMemberRequest = requestRepository.save(newMemberRequest);
        targetRoomFromDb.addRequest(savedNewMemberRequest);

        MessageEvent messageFromRequest = messageCreator.createMessageFromRequest(savedNewMemberRequest, List.of(RecipientType.ADMIN), "New member request from user: " + requesterFromDb.getUsername());
        messageService.sendMessageForRoomAdmins(messageFromRequest, targetRoomFromDb.getId());
    }

    @Transactional
    public void acceptAddUserRequest(final RoomAddOrRemoveUserRequestDto requestDto) {
        Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(requestDto.getRoomId(), requestDto.getAdminId(), roomRepository);

        Request newMemberRequestFromDb = requestRepository.findByRequestableAndRequesterIdAndType(targetRoomFromDb, requestDto.getUserId(), RequestType.NEW_MEMBER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (newMemberRequestFromDb.getStatus().equals(RequestStatus.ACCEPTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already processed!");
        }

        User requesterFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        newMemberRequestFromDb.setStatus(RequestStatus.ACCEPTED);
        targetRoomFromDb.getUsersInRoom().add(requesterFromDb);

        MessageEvent simpleMessageForRoom = messageCreator.createSimpleMessage(targetRoomFromDb, targetRoomFromDb, List.of(RecipientType.USER), "Room has gain new member: " + requesterFromDb.getUsername());
        messageService.sendMessageForRoomUsers(simpleMessageForRoom, targetRoomFromDb.getId());
        MessageEvent simpleMessageForUser = messageCreator.createSimpleMessage(targetRoomFromDb, requesterFromDb, "Your request to become member of room: '" + targetRoomFromDb.getName() + "' has changed status to: ACCEPTED");
        messageService.sendMessageForPrivateUser(simpleMessageForUser, requesterFromDb.getId());
    }

    @Transactional
    public void rejectAddUserRequest(final RoomAddOrRemoveUserRequestDto requestDto) {
        Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(requestDto.getRoomId(), requestDto.getAdminId(), roomRepository);

        Request newMemberRequestFromDb = requestRepository.findByRequestableAndRequesterIdAndType(targetRoomFromDb, requestDto.getUserId(), RequestType.NEW_MEMBER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (newMemberRequestFromDb.getStatus().equals(RequestStatus.REJECTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already processed!");
        }

        User requesterFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        newMemberRequestFromDb.setStatus(RequestStatus.REJECTED);

        MessageEvent simpleMessageForRoom = messageCreator.createSimpleMessage(targetRoomFromDb, targetRoomFromDb, List.of(RecipientType.ADMIN), "Member request from user " + requesterFromDb.getUsername() + " has been rejected.");
        messageService.sendMessageForRoomAdmins(simpleMessageForRoom, targetRoomFromDb.getId());
        MessageEvent simpleMessageForUser = messageCreator.createSimpleMessage(targetRoomFromDb, requesterFromDb, "Your request to become member of room: '" + targetRoomFromDb.getName() + "' has changed status to: REJECTED");
        messageService.sendMessageForPrivateUser(simpleMessageForUser, requesterFromDb.getId());
    }

    private Request createRequest(final Room targetRoom, final Long requesterId, final String requesterName, final RequestType requestType) {
        return Request.builder()
                .requestable(targetRoom)
                .createdAt(LocalDateTime.now())
                .requesterId(requesterId)
                .requesterName(requesterName)
                .status(RequestStatus.PENDING)
                .type(requestType)
                .requestableType(targetRoom.getClass().getSimpleName())
                .build();
    }
}
