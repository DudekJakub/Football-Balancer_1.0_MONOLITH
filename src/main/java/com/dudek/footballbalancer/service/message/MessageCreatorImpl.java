package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.entity.MessageParticipant;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.message.MessageEvent;
import com.dudek.footballbalancer.model.message.MessageType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageCreatorImpl implements MessageCreator {

    @Override
    public MessageEvent createSimpleMessage(final MessageParticipant messageSender, final MessageParticipant messageRecipient, final String message) {
        return MessageEvent.builder()
                .senderName(messageSender.getParticipantName())
                .senderId(messageSender.getParticipantId())
                .recipientId(messageRecipient.getParticipantId())
                .messageType(MessageType.SIMPLE_MESSAGE)
                .message(message)
                .sendTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageEvent createMessageFromRequest(final Request request, final String message) {
        return MessageEvent.builder()
                .senderName(request.getRequesterName())
                .senderId(request.getRequesterId())
                .recipientId(request.getRequestable().getId())
                .messageStatus(request.getStatus())
                .messageType(MessageType.REQUEST)
                .messageSubType(request.getType())
                .message(message)
                .sendTime(LocalDateTime.now())
                .build();
    }
}
