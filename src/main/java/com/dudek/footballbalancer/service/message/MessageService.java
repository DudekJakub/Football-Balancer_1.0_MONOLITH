package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.message.MessageEvent;

public interface MessageService {

    void sendMessageForRoomAdmins(MessageEvent messageEvent, Long roomId);
    void sendMessageForRoomUsers(MessageEvent messageEvent, Long roomId);
    void sendMessageForPrivateUser(MessageEvent messageEvent, Long userId);
}
