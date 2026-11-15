package com.tushar.orders.exception;

/** Thrown when no order exists for the requested id. Mapped to 404. */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("order not found: " + id);
    }
}
