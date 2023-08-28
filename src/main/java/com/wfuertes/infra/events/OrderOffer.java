package com.wfuertes.infra.events;

public record OrderOffer(String orderId, Integer amount, String offerType) implements Order {
}
