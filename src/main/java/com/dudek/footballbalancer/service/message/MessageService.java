package com.dudek.footballbalancer.service.message;

import com.dudek.footballbalancer.model.entity.message.MessageEvent;
import org.springframework.amqp.core.TopicExchange;

public interface MessageService {

    void sendMessageForTopic(TopicExchange topicExchange, String routingKey, MessageEvent messageEvent);
}
