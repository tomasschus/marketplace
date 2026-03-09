package com.schusterapps.orderservice.services;

import com.schusterapps.orderservice.dtos.OrderRequest;
import com.schusterapps.orderservice.dtos.OrderResponse;
import com.schusterapps.orderservice.events.OrderEventPublisher;
import com.schusterapps.orderservice.models.IdempotencyKey;
import com.schusterapps.orderservice.models.Order;
import com.schusterapps.orderservice.repositories.IdempotencyKeyRepository;
import com.schusterapps.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock private OrderEventPublisher eventPublisher;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    private Order savedOrder;
    private OrderRequest request;

    @BeforeEach
    void setUp() {
        request = new OrderRequest();
        request.setUserId(1L);
        request.setTotal(new BigDecimal("99.99"));

        savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setTotal(new BigDecimal("99.99"));
        savedOrder.setStatus("PENDING");
        savedOrder.setCreatedAt(LocalDateTime.now());
    }

    // --- createOrder ---

    @Test
    void createOrder_withoutIdempotencyKey_createsOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(null, request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getTotal()).isEqualByComparingTo("99.99");
        verify(idempotencyKeyRepository, never()).existsById(anyString());
        verify(eventPublisher).publishOrderCreated(eq(1L), anyString());
    }

    @Test
    void createOrder_withBlankIdempotencyKey_treatsAsNoKey() {
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        orderService.createOrder("   ", request);

        verify(idempotencyKeyRepository, never()).existsById(anyString());
    }

    @Test
    void createOrder_withNewIdempotencyKey_savesKeyAndCreatesOrder() {
        when(idempotencyKeyRepository.existsById("key-abc")).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder("key-abc", request);

        assertThat(response.getId()).isEqualTo(1L);
        verify(idempotencyKeyRepository).save(any(IdempotencyKey.class));
        verify(eventPublisher).publishOrderCreated(eq(1L), anyString());
    }

    @Test
    void createOrder_withDuplicateIdempotencyKey_throwsConflict() {
        when(idempotencyKeyRepository.existsById("key-dup")).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder("key-dup", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key-dup");

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishOrderCreated(anyLong(), anyString());
    }

    @Test
    void createOrder_publishesCorrectPayload() {
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        orderService.createOrder(null, request);

        verify(eventPublisher).publishOrderCreated(eq(1L), contains("OrderCreated"));
    }

    // --- getOrderById ---

    @Test
    void getOrderById_found_returnsResponse() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // --- getAllOrders ---

    @Test
    void getAllOrders_returnsMappedPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(savedOrder), pageable, 1));

        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllOrders_emptyPage_returnsEmpty() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}
