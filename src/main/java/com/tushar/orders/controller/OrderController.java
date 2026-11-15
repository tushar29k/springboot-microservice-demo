package com.tushar.orders.controller;

import com.tushar.orders.dto.CreateOrderRequest;
import com.tushar.orders.dto.OrderListResponse;
import com.tushar.orders.dto.OrderResponse;
import com.tushar.orders.dto.UpdateStatusRequest;
import com.tushar.orders.model.OrderStatus;
import com.tushar.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.from(service.createOrder(request)));
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return OrderResponse.from(service.getOrderById(id));
    }

    @GetMapping
    public OrderListResponse list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return OrderListResponse.from(service.listOrders(status, pageable));
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return OrderResponse.from(service.updateStatus(id, request.status()));
    }
}
