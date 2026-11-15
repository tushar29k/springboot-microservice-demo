package com.tushar.orders.dto;

import com.tushar.orders.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/** Payload for PUT /api/orders/{id}/status. */
public record UpdateStatusRequest(
        @NotNull(message = "status is required") OrderStatus status) {
}
