package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.message.MessageEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQPublisherService implements MessageService {
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange roomExchange;
    private final TopicExchange privateUserExchange;
    private final Logger logger = LoggerFactory.logger(RabbitMQPublisherService.class);

    @Autowired
    public RabbitMQPublisherService(final ObjectMapper objectMapper, final RabbitTemplate rabbitTemplate, final TopicExchange roomExchange, final TopicExchange privateUserExchange) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.roomExchange = roomExchange;
        this.privateUserExchange = privateUserExchange;
    }

    @Override
    public void sendMessageForRoomAdmins(final MessageEvent messageEvent, final Long roomId) {
        String mappedMessage = mapMessageEvent(messageEvent);

        if (mappedMessage != null) {
            rabbitTemplate.convertAndSend(roomExchange.getName(), "room.admins", mappedMessage, m -> {
                m.getMessageProperties().setContentType("text/plain");
                m.getMessageProperties().setHeader("roomId", roomId);
                return m;
            });
        }
    }

    @Override
    public void sendMessageForRoomUsers(final MessageEvent messageEvent, final Long roomId) {
        String mappedMessage = mapMessageEvent(messageEvent);

        if (mappedMessage != null) {
            rabbitTemplate.convertAndSend(roomExchange.getName(), "room.users", mappedMessage, m -> {
                m.getMessageProperties().setContentType("text/plain");
                m.getMessageProperties().setHeader("roomId", roomId);
                return m;
            });
        }
    }

    @Override
    public void sendMessageForPrivateUser(final MessageEvent messageEvent, final Long userId) {
        String mappedMessage = mapMessageEvent(messageEvent);

        if (mappedMessage != null) {
            rabbitTemplate.convertAndSend(privateUserExchange.getName(), "private.user", mappedMessage, m -> {
                m.getMessageProperties().setContentType("text/plain");
                m.getMessageProperties().setHeader("userId", userId);
                return m;
            });
        }
    }

    private String mapMessageEvent(final MessageEvent messageEvent) {
        String message;
        try {
            message = objectMapper.writeValueAsString(messageEvent);
        } catch (JsonProcessingException e) {
            logger.warn("MessageEvent (" + messageEvent.getClass().getSimpleName() + ") couldn't be parsed to JSON format: " + e.getMessage());
            return null;
        }
        return message;
    }
}
