package com.tushar.orders.dto;

import com.tushar.orders.model.Order;
import com.tushar.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** What the API returns for an order. Built from the entity, never the raw entity. */
public record OrderResponse(
        Long id,
        String customerName,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {

    public record OrderItemResponse(
            Long id, String productName, int quantity, BigDecimal unitPrice) {
    }

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(), i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
