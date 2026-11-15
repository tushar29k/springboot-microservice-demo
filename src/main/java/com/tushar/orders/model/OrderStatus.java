package com.tushar.orders.model;

/** Lifecycle of an order. Transitions are enforced by OrderService. */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
