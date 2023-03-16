package com.dudek.footballbalancer.model.entity.request;

public interface RoomRequestable extends Requestable {
    void addRequest(Request request);
    void removeRequest(Request request);
}
