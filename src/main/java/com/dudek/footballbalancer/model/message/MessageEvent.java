package com.dudek.footballbalancer.model.message;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class MessageEvent implements Serializable {

    protected String senderName;
    protected Long senderId;
    protected Long recipientId;
    protected List<RecipientType> recipientTypes;
    protected LocalDateTime sendTime;
    protected Enum<?> messageStatus;
    protected Enum<?> messageType;
    protected Enum<?> messageSubType;
    protected String message;
}
