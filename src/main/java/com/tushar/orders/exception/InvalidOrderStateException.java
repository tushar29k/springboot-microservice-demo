package com.tushar.orders.exception;

import com.tushar.orders.model.OrderStatus;

/** Thrown when a status change is not a legal transition. Mapped to 409. */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(OrderStatus from, OrderStatus to) {
        super("cannot move order from " + from + " to " + to);
    }
}
