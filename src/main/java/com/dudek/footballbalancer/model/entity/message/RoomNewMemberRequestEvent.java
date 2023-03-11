package com.dudek.footballbalancer.model.entity.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomNewMemberRequestEvent implements MessageEvent {

    private Long roomId;
    private Long requesterId;

    public RoomNewMemberRequestEvent(Long roomId, Long requesterId) {
        this.roomId = roomId;
        this.requesterId = requesterId;
    }
}

