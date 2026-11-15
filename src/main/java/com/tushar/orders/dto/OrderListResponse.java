package com.tushar.orders.dto;

import com.tushar.orders.model.Order;
import java.util.List;
import org.springframework.data.domain.Page;

/** Stable JSON shape for the paginated list endpoint.
 * Returning Spring Data's Page directly serializes its internal
 * Pageable state and draws a framework warning, so the controller
 * maps it to this record instead. */
public record OrderListResponse(
        List<OrderResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static OrderListResponse from(Page<Order> page) {
        return new OrderListResponse(
                page.getContent().stream().map(OrderResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
