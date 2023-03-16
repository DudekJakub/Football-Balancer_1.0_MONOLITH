package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.message.AbstractMessageEvent;

public interface MessageService {

    void sendMessageForRoomAdmins(AbstractMessageEvent messageEvent, Long roomId);
    void sendMessageForRoomUsers(AbstractMessageEvent messageEvent, Long roomId);
    void sendMessageForPrivateUser(AbstractMessageEvent messageEvent, Long userId);
}
