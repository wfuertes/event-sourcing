package com.wfuertes.domain;

import com.wfuertes.infra.events.Order;

public class UnableHandlingEvent extends RuntimeException {
    private final Order order;

    public UnableHandlingEvent(Order order,  Throwable err) {
        this.order = order;
        addSuppressed(err);
    }

    public Order getEvent() {
        return order;
    }
}
