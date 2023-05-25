package com.dudek.footballbalancer.aspect;

import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.message.MessageEvent;
import com.dudek.footballbalancer.service.message.MessageCreator;
import com.dudek.footballbalancer.service.message.MessageService;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.dudek.footballbalancer.service.util.RequestContextHolderUtil.*;

@Aspect
@Component
public class RequestMessagingAspect {

    private final MessageService messageService;
    private final MessageCreator messageCreator;
    private final Logger logger = LoggerFactory.getLogger(RoomMessagingAspect.class);

    public RequestMessagingAspect(final MessageService messageService, final MessageCreator messageCreator) {
        this.messageService = messageService;
        this.messageCreator = messageCreator;
    }

    @After("execution(* com.dudek.footballbalancer.service.request.RoomRequestService.sendAddUserRequest(..))")
    public void sendAddUserRequestMessage() {
        try {
            Room targetRoomFromDb = (Room) getContextAttribute(TARGET_ROOM);
            User requesterFromDb = (User) getContextAttribute(TARGET_USER);
            Request request = (Request) getContextAttribute(NEW_MEMBER_REQUEST);
            Long roomId = targetRoomFromDb.getId();

            MessageEvent messageFromRequest = messageCreator.createMessageFromRequest(request, "New member request from user: " + requesterFromDb.getUsername());
            messageService.sendMessageForRoomAdmins(messageFromRequest, roomId);
        } catch (NullPointerException e) {
            logger.error("Null pointer exception occurred: " + e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Cast class exception occurred: " + e.getMessage());
        }
    }

    @After("execution(* com.dudek.footballbalancer.service.request.RoomRequestService.acceptAddUserRequest(..))")
    public void sendAcceptUserMessage() {
        try {
            Room targetRoomFromDb = (Room) getContextAttribute(TARGET_ROOM);
            User requesterFromDb = (User) getContextAttribute(TARGET_USER);
            Long roomId = targetRoomFromDb.getId();

            MessageEvent simpleMessageForRoom = messageCreator.createSimpleMessage(targetRoomFromDb, "Room has gain new member: " + requesterFromDb.getUsername());
            messageService.sendMessageForRoomUsers(simpleMessageForRoom, targetRoomFromDb.getId());
            MessageEvent simpleMessageForUser = messageCreator.createSimpleMessage(targetRoomFromDb, requesterFromDb, "Your request to become member of room: '" + targetRoomFromDb.getName() + "' has changed status to: ACCEPTED");
            messageService.sendMessageForPrivateUser(simpleMessageForUser, roomId);
        } catch (NullPointerException e) {
            logger.error("Null pointer exception occurred: " + e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Cast class exception occurred: " + e.getMessage());
        }
    }

    @After("execution(* com.dudek.footballbalancer.service.request.RoomRequestService.rejectAddUserRequest(..))")
    public void sendRejectUserMessage() {
        try {
            Room targetRoomFromDb = (Room) getContextAttribute(TARGET_ROOM);
            User requesterFromDb = (User) getContextAttribute(TARGET_USER);
            Long roomId = targetRoomFromDb.getId();

            MessageEvent simpleMessageForRoom = messageCreator.createSimpleMessage(targetRoomFromDb, "Member request from user " + requesterFromDb.getUsername() + " has been rejected.");
            messageService.sendMessageForRoomAdmins(simpleMessageForRoom, targetRoomFromDb.getId());
            MessageEvent simpleMessageForUser = messageCreator.createSimpleMessage(targetRoomFromDb, requesterFromDb, "Your request to become member of room: '" + targetRoomFromDb.getName() + "' has changed status to: REJECTED");
            messageService.sendMessageForPrivateUser(simpleMessageForUser, roomId);
        } catch (NullPointerException e) {
            logger.error("Null pointer exception occurred: " + e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Cast class exception occurred: " + e.getMessage());
        }
    }
}
