package com.wfuertes.infra.events;

public record OrderCompleted(String orderId, Integer foodsTotal, Integer taxes) implements Order {
}
