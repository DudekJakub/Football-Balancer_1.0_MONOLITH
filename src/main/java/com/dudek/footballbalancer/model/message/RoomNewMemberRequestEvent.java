package com.dudek.footballbalancer.model.message;

import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.model.entity.request.RequestStatus;
import com.dudek.footballbalancer.model.entity.request.RequestType;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class RoomNewMemberRequestEvent extends AbstractMessageEvent {

    public RoomNewMemberRequestEvent(final Room targetRoom, final User requester, final List<RecipientType> recipientTypes, final RequestStatus requestStatus) {
        this.senderName = requester.getUsername();
        this.senderId = String.valueOf(requester.getId());
        this.recipientId = String.valueOf(targetRoom.getId());
        this.recipientTypes = recipientTypes.stream().map(RecipientType::name).collect(Collectors.toList());
        this.messageStatus = requestStatus.name();
        this.messageType = MessageType.REQUEST.name();
        this.messageSubType = RequestType.NEW_MEMBER.name();
        this.message = "New member request | user: " + senderName + " | status: " + messageStatus;
    }
}

