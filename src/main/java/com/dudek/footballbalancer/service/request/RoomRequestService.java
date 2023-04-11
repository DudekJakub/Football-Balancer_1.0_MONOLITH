package com.dudek.footballbalancer.service.request;

import com.dudek.footballbalancer.model.dto.room.RoomAddOrRemoveUserRequestDto;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.entity.request.RequestStatus;
import com.dudek.footballbalancer.model.entity.request.RequestType;
import com.dudek.footballbalancer.model.entity.request.Requestable;
import com.dudek.footballbalancer.model.message.MessageEvent;
import com.dudek.footballbalancer.repository.RequestRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.dudek.footballbalancer.service.message.MessageCreator;
import com.dudek.footballbalancer.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
public class RoomRequestService {

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

        requestRepository.findByRequestableAndRequesterIdAndType(targetRoomFromDb, requesterId, RequestType.NEW_MEMBER).ifPresentOrElse(request -> {
            if (!request.getStatus().equals(RequestStatus.REJECTED)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already sent or processed successfully!");
            } else {
                request.setStatus(RequestStatus.PENDING);
                MessageEvent messageFromRequest = messageCreator.createMessageFromRequest(request, "New member request from user: " + requesterFromDb.getUsername());
                messageService.sendMessageForRoomAdmins(messageFromRequest, targetRoomFromDb.getId());
            }
        }, () -> {
            Request newMemberRequest = createRequest(targetRoomFromDb, requesterFromDb.getId(), requesterFromDb.getUsername(), RequestType.NEW_MEMBER);
            Request savedNewMemberRequest = requestRepository.save(newMemberRequest);
            targetRoomFromDb.addRequest(savedNewMemberRequest);

            MessageEvent messageFromRequest = messageCreator.createMessageFromRequest(savedNewMemberRequest, "New member request from user: " + requesterFromDb.getUsername());
            messageService.sendMessageForRoomAdmins(messageFromRequest, targetRoomFromDb.getId());
        });
    }

    @Transactional
    public void acceptAddUserRequest(final RoomAddOrRemoveUserRequestDto requestDto) {
        Room targetRoomFromDb = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Request newMemberRequestFromDb = findRequestAndCheckStatus(targetRoomFromDb, requestDto.getUserId());

        User requesterFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        newMemberRequestFromDb.setStatus(RequestStatus.ACCEPTED);
        targetRoomFromDb.getUsersInRoom().add(requesterFromDb);

        MessageEvent simpleMessageForRoom = messageCreator.createSimpleMessage(targetRoomFromDb, targetRoomFromDb, "Room has gain new member: " + requesterFromDb.getUsername());
        messageService.sendMessageForRoomUsers(simpleMessageForRoom, targetRoomFromDb.getId());
        MessageEvent simpleMessageForUser = messageCreator.createSimpleMessage(targetRoomFromDb, requesterFromDb, "Your request to become member of room: '" + targetRoomFromDb.getName() + "' has changed status to: ACCEPTED");
        messageService.sendMessageForPrivateUser(simpleMessageForUser, requesterFromDb.getId());
    }

    @Transactional
    public void rejectAddUserRequest(final RoomAddOrRemoveUserRequestDto requestDto) {
        Room targetRoomFromDb = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Request newMemberRequestFromDb = findRequestAndCheckStatus(targetRoomFromDb, requestDto.getUserId());

        User requesterFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        newMemberRequestFromDb.setStatus(RequestStatus.REJECTED);

        MessageEvent simpleMessageForRoom = messageCreator.createSimpleMessage(targetRoomFromDb, targetRoomFromDb, "Member request from user " + requesterFromDb.getUsername() + " has been rejected.");
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

    private Request findRequestAndCheckStatus(final Requestable requestable, final Long requesterId) {
        Request newMemberRequestFromDb = requestRepository.findByRequestableAndRequesterIdAndType(requestable, requesterId, RequestType.NEW_MEMBER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!newMemberRequestFromDb.getStatus().equals(RequestStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already processed!");
        }
        return newMemberRequestFromDb;
    }
}
