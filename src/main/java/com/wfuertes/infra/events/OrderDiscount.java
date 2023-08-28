package com.wfuertes.infra.events;

public record OrderDiscount(String orderId, Integer amount) implements Order {
}
