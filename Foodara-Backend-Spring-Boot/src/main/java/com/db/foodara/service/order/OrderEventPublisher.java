package com.db.foodara.service.order;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes domain events to RabbitMQ for Notification Service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "foodara.events";

    public void publish(Object event, String routingKey) {
        log.info("Publishing event to exchange={}, routingKey={}", EXCHANGE, routingKey);
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
    }
}
