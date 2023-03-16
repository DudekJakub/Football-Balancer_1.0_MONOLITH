package com.dudek.footballbalancer.model.message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AbstractMessageEvent implements Serializable {

    protected String senderName;
    protected String senderId;
    protected String recipientId;
    protected List<String> recipientTypes;
    protected String message;
    protected String messageStatus;
    protected String messageType;
    protected String messageSubType;

}
