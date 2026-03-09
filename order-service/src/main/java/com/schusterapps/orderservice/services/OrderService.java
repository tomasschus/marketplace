package com.schusterapps.orderservice.services;

import tools.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;
import com.schusterapps.orderservice.dtos.OrderRequest;
import com.schusterapps.orderservice.dtos.OrderResponse;
import com.schusterapps.orderservice.events.OrderEventPublisher;
import com.schusterapps.orderservice.models.IdempotencyKey;
import com.schusterapps.orderservice.models.Order;
import com.schusterapps.orderservice.repositories.IdempotencyKeyRepository;
import com.schusterapps.orderservice.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OrderEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            OrderEventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse createOrder(String idempotencyKey, OrderRequest request) {
        // Idempotency check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyKeyRepository.existsById(idempotencyKey)) {
                throw new IllegalArgumentException("Order already processed for idempotency key: " + idempotencyKey);
            }
            idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKey));
        }

        // Create Order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotal(request.getTotal());
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);

        // Publish OrderCreated event
        String jsonPayload = String.format("{\"orderId\":%d,\"userId\":%d,\"total\":%s,\"status\":\"%s\",\"eventType\":\"OrderCreated\"}",
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotal().toString(), savedOrder.getStatus());
        eventPublisher.publishOrderCreated(savedOrder.getId(), jsonPayload);

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return mapToResponse(order);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotal(order.getTotal());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}
