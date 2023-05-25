package com.dudek.footballbalancer.aspect;

import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.message.MessageEvent;
import com.dudek.footballbalancer.service.message.MessageCreator;
import com.dudek.footballbalancer.service.message.MessageService;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.dudek.footballbalancer.service.util.RequestContextHolderUtil.TARGET_ROOM;
import static com.dudek.footballbalancer.service.util.RequestContextHolderUtil.getContextAttribute;

@Aspect
@Component
public class RoomMessagingAspect {

    private final MessageService messageService;
    private final MessageCreator messageCreator;
    private final Logger logger = LoggerFactory.getLogger(RoomMessagingAspect.class);

    public RoomMessagingAspect(final MessageService messageService, final MessageCreator messageCreator) {
        this.messageService = messageService;
        this.messageCreator = messageCreator;
    }

    @After("execution(* com.dudek.footballbalancer.service.room.RoomBasicManagementService.updateRoom(..))")
    public void sendUpdateRoomMessage() {
        try {
            Room targetRoomFromDb = (Room) getContextAttribute(TARGET_ROOM);
            Long roomId = targetRoomFromDb.getId();

            MessageEvent message = messageCreator.createSimpleMessage(targetRoomFromDb, "Room basic information has been updated recently!");
            messageService.sendMessageForRoomUsers(message, roomId);
            messageService.sendMessageForRoomAdmins(message, roomId);
        } catch (NullPointerException e) {
            logger.error("Null pointer exception occurred: " + e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Cast class exception occurred: " + e.getMessage());
        }
    }

    @After("execution(* com.dudek.footballbalancer.service.room.RoomBasicManagementService.updateNextMatchDates(..))")
    public void sendUpdateNextMatchDatesMessage() {
        try {
            Room targetRoomFromDb = (Room) getContextAttribute(TARGET_ROOM);
            Long roomId = targetRoomFromDb.getId();

            MessageEvent message = messageCreator.createSimpleMessage(targetRoomFromDb, "Room next match dates have been updated recently!");
            messageService.sendMessageForRoomUsers(message, roomId);
            messageService.sendMessageForRoomAdmins(message, roomId);
        } catch (NullPointerException e) {
            logger.error("Null pointer exception occurred: " + e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Cast class exception occurred: " + e.getMessage());
        }
    }
}
