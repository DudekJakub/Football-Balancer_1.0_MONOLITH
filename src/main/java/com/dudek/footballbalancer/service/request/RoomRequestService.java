package com.dudek.footballbalancer.service.request;

import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.message.RoomNewMemberRequestEvent;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.entity.request.RequestStatus;
import com.dudek.footballbalancer.model.entity.request.RequestType;
import com.dudek.footballbalancer.repository.RequestRepository;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.service.message.MessageService;
import org.springframework.amqp.core.TopicExchange;
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
    private final MessageService messageService;
    private final TopicExchange roomExchange;

    @Autowired
    public RoomRequestService(final RoomRepository roomRepository, final RequestRepository requestRepository, final MessageService messageService, final TopicExchange roomExchange) {
        this.roomRepository = roomRepository;
        this.requestRepository = requestRepository;
        this.messageService = messageService;
        this.roomExchange = roomExchange;
    }

    @Transactional
    public void sendAddUserRequest(final Long roomId, final Long requesterId) {
        Room targetRoomFromDb = roomRepository.findByIdFetchAdmins(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Request newRequest = createRequest(targetRoomFromDb, requesterId);
        newRequest.setType(RequestType.NEW_MEMBER);
        Request savedRequest = requestRepository.save(newRequest);

        RoomNewMemberRequestEvent roomNewMemberRequestEvent = new RoomNewMemberRequestEvent(roomId, requesterId);
        messageService.sendMessageForTopic(roomExchange, "myKey", roomNewMemberRequestEvent);

        targetRoomFromDb.addUserRequest(savedRequest);
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
