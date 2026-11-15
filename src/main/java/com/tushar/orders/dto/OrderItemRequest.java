package com.tushar.orders.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** One line item in a create-order request. */
public record OrderItemRequest(
        @NotBlank(message = "productName must not be blank") String productName,
        @Min(value = 1, message = "quantity must be at least 1") int quantity,
        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.01", message = "unitPrice must be positive") BigDecimal unitPrice) {
}
