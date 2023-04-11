package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.entity.MessageParticipant;
import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.message.MessageEvent;

public interface MessageCreator {

    MessageEvent createMessageFromRequest(Request request, String message);
    MessageEvent createSimpleMessage(MessageParticipant messageSender, MessageParticipant messageRecipient, String message);

}
