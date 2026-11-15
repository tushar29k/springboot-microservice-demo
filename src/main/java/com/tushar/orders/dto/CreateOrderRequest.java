package com.tushar.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Payload for POST /api/orders. The total is computed server-side. */
public record CreateOrderRequest(
        @NotBlank(message = "customerName must not be blank") String customerName,
        @NotEmpty(message = "an order needs at least one item")
        @Valid List<OrderItemRequest> items) {
}
