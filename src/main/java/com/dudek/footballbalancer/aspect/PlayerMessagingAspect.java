package com.dudek.footballbalancer.aspect;

import com.dudek.footballbalancer.model.entity.Player;
import com.dudek.footballbalancer.model.entity.Room;
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
public class PlayerMessagingAspect {

    private final MessageService messageService;
    private final MessageCreator messageCreator;
    private final Logger logger = LoggerFactory.getLogger(RoomMessagingAspect.class);

    public PlayerMessagingAspect(final MessageService messageService, final MessageCreator messageCreator) {
        this.messageService = messageService;
        this.messageCreator = messageCreator;
    }

    @After("execution(* com.dudek.footballbalancer.service.PlayerService.createPlayer(..))")
    public void sendPlayerCreationMessage() {
        try {
            Room targetRoomFromDb = (Room) getContextAttribute(TARGET_ROOM);
            Player newPlayer = (Player) getContextAttribute(TARGET_PLAYER);
            Long roomId = targetRoomFromDb.getId();

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("New player has been created: ");
            messageBuilder.append(newPlayer.getFirstName());
            messageBuilder.append(" ");
            messageBuilder.append(newPlayer.getLastName());

            if (newPlayer.getUser() != null) {
                messageBuilder.append(" (linked with user: ");
                messageBuilder.append(newPlayer.getUser().getUsername());
                messageBuilder.append(")");
            }
            String messageContent = messageBuilder.toString();

            MessageEvent messageFromRequest = messageCreator.createSimpleMessage(targetRoomFromDb, messageContent);
            messageService.sendMessageForRoomUsers(messageFromRequest, roomId);
        } catch (NullPointerException e) {
            logger.error("Null pointer exception occurred: " + e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Cast class exception occurred: " + e.getMessage());
        }
    }

    @After("execution(* com.dudek.footballbalancer.service.PlayerService.updatePlayer(..))")
    public void sendPlayerUpdateMessage() {
        try {
            Room targetRoomFromDb = (Room) getContextAttribute(TARGET_ROOM);
            Player updatedPlayer = (Player) getContextAttribute(TARGET_PLAYER);
            String firstNameBeforeUpdate = (String) getContextAttribute(PLAYER_FIRSTNAME_BEFORE_UPDATE);
            String lastNameBeforeUpdate = (String) getContextAttribute(PLAYER_LASTNAME_BEFORE_UPDATE);
            Long roomId = targetRoomFromDb.getId();

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Player (")
                    .append(firstNameBeforeUpdate)
                    .append(" ")
                    .append(lastNameBeforeUpdate)
                    .append(") has been updated: ")
                    .append(updatedPlayer.getFirstName())
                    .append(" ")
                    .append(updatedPlayer.getLastName());

            if (updatedPlayer.getUser() != null) {
                messageBuilder.append(" (linked with user: ")
                        .append(updatedPlayer.getUser().getUsername())
                        .append(")");
            }
            String messageContent = messageBuilder.toString();

            MessageEvent message = messageCreator.createSimpleMessage(targetRoomFromDb, messageContent);
            messageService.sendMessageForRoomAdmins(message, roomId);
        } catch (NullPointerException e) {
            logger.error("Null pointer exception occurred: " + e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Cast class exception occurred: " + e.getMessage());
        }
    }
}
