package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.entity.message.MessageEvent;
import com.dudek.footballbalancer.model.entity.message.RoomNewMemberRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQPublisherService implements MessageService {
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    private final Logger logger = LoggerFactory.logger(RabbitMQPublisherService.class);

    public RabbitMQPublisherService(final ObjectMapper objectMapper, final RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessageForTopic(final TopicExchange topicExchange, final String routingKey, final MessageEvent messageEvent) {
        String message;
        try {
            message = objectMapper.writeValueAsString(messageEvent);
        } catch (JsonProcessingException e) {
            logger.warn("MessageEvent (" + messageEvent.getClass().getSimpleName() + ") couldn't be parsed to JSON format: " + e.getMessage());
            return;
        }
        rabbitTemplate.convertAndSend(topicExchange.getName(), routingKey, message);
    }
}
