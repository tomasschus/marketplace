package com.schusterapps.orderservice.controllers;

import com.schusterapps.orderservice.dtos.OrderResponse;
import com.schusterapps.orderservice.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock OrderService orderService;
    @InjectMocks OrderController orderController;

    MockMvc mockMvc;
    OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setValidator(validator)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();

        orderResponse = new OrderResponse();
        orderResponse.setId(1L);
        orderResponse.setUserId(1L);
        orderResponse.setStatus("PENDING");
        orderResponse.setTotal(new BigDecimal("99.99"));
        orderResponse.setCreatedAt(LocalDateTime.now());
    }

    // --- POST /orders ---

    @Test
    void postOrder_validRequest_returns201() throws Exception {
        when(orderService.createOrder(isNull(), any())).thenReturn(orderResponse);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1, "total": 99.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(99.99));
    }

    @Test
    void postOrder_withIdempotencyKey_returns201() throws Exception {
        when(orderService.createOrder(eq("key-123"), any())).thenReturn(orderResponse);

        mockMvc.perform(post("/orders")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1, "total": 99.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void postOrder_duplicateIdempotencyKey_returns409() throws Exception {
        when(orderService.createOrder(eq("key-dup"), any()))
                .thenThrow(new IllegalArgumentException("Order already processed for idempotency key: key-dup"));

        mockMvc.perform(post("/orders")
                        .header("Idempotency-Key", "key-dup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1, "total": 99.99}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void postOrder_missingUserId_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"total": 99.99}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postOrder_missingTotal_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postOrder_zeroTotal_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1, "total": 0.00}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postOrder_negativeTotal_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1, "total": -10.00}
                                """))
                .andExpect(status().isBadRequest());
    }

    // --- GET /orders/{id} ---

    @Test
    void getOrder_found_returns200() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new IllegalArgumentException("Order not found: 99"));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound());
    }

    // --- GET /orders ---

    @Test
    void getAllOrders_returns200WithResults() {
        PageRequest pageable = PageRequest.of(0, 20);
        PageImpl<OrderResponse> page = new PageImpl<>(List.of(orderResponse), pageable, 1);
        when(orderService.getAllOrders(pageable)).thenReturn(page);

        var result = orderController.getAllOrders(pageable);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getTotalElements()).isEqualTo(1);
        assertThat(result.getBody().getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllOrders_emptyPage_returns200() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(orderService.getAllOrders(pageable)).thenReturn(new PageImpl<>(List.of()));

        var result = orderController.getAllOrders(pageable);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getContent()).isEmpty();
    }
}
