package com.wfuertes.infra.events;

public record OrderCreated(String orderId, Long orderNumber, String type) implements Order {
}
