package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.entity.MessageParticipant;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.message.MessageEvent;
import com.dudek.footballbalancer.model.message.RecipientType;

import java.util.List;

public interface MessageCreator {

    MessageEvent createMessageFromRequest(Request request, List<RecipientType> recipientTypes, String message);
    MessageEvent createSimpleMessage(MessageParticipant messageSender, MessageParticipant messageRecipient, String message);
    MessageEvent createSimpleMessage(final MessageParticipant messageSender, final MessageParticipant messageRecipient, List<RecipientType> recipientTypes, final String message);

}
