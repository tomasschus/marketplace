package com.schusterapps.orderservice.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(Long orderId, String payloadJson) {
        log.info("Publishing OrderCreated event for orderId: {}", orderId);
        // Simple JSON string for now, could use Object or specific DTO
        kafkaTemplate.send("order-events", orderId.toString(), payloadJson);
    }
}
