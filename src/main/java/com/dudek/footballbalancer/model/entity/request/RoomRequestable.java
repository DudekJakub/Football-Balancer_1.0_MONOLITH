package com.dudek.footballbalancer.model.entity.request;

public interface RoomRequestable extends Requestable {
    void addUserRequest(Request request);
    void linkUserToPlayerRequest(Request request);
}
