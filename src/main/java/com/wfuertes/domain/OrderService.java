package com.wfuertes.domain;

import com.wfuertes.infra.events.OrderCompleted;
import com.wfuertes.infra.events.OrderCreated;
import com.wfuertes.infra.events.OrderDiscount;
import com.wfuertes.infra.events.OrderOffer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventRepository eventRepository;

    public void handleOrderCreated(OrderCreated orderCreated) {
        try {
            orderRepository
                    .findById(orderCreated.orderId())
                    .ifPresentOrElse(
                            order -> orderRepository.update(
                                    order.toBuilder()
                                            .number(orderCreated.orderNumber())
                                            .type(orderCreated.type())
                                            .version(order.version() + 1L)
                                            .updatedAt(DateUtils.utcLocalDateTime())
                                            .build(),
                                    order.version()
                            ),
                            () -> orderRepository.save(Order.builder()
                                    .id(orderCreated.orderId())
                                    .number(orderCreated.orderNumber())
                                    .type(orderCreated.type())
                                    .version(1L)
                                    .createdAt(DateUtils.utcLocalDateTime())
                                    .updatedAt(DateUtils.utcLocalDateTime())
                                    .build())
                    );
            eventRepository.save(orderCreated);
        } catch (Throwable err) {
            throw new UnableHandlingEvent(orderCreated, err);
        }
    }

    public void handlerOrderDiscount(OrderDiscount orderDiscount) {
        try {
            orderRepository
                    .findById(orderDiscount.orderId())
                    .ifPresentOrElse(
                            order -> orderRepository.update(
                                    order.toBuilder()
                                            .discountAmount(orderDiscount.amount())
                                            .version(order.version() + 1L)
                                            .updatedAt(DateUtils.utcLocalDateTime())
                                            .build(),
                                    order.version()
                            ),
                            () -> orderRepository.save(Order.builder()
                                    .id(orderDiscount.orderId())
                                    .discountAmount(orderDiscount.amount())
                                    .version(1L)
                                    .createdAt(DateUtils.utcLocalDateTime())
                                    .updatedAt(DateUtils.utcLocalDateTime())
                                    .build())
                    );
            eventRepository.save(orderDiscount);
        } catch (Throwable err) {
            throw new UnableHandlingEvent(orderDiscount, err);
        }
    }

    public void handlerOrderOffer(OrderOffer orderOffer) {
        try {
            orderRepository
                    .findById(orderOffer.orderId())
                    .ifPresentOrElse(
                            order -> orderRepository.update(
                                    order.toBuilder()
                                            .offerAmount(orderOffer.amount())
                                            .offerType(orderOffer.offerType())
                                            .version(order.version() + 1L)
                                            .updatedAt(DateUtils.utcLocalDateTime())
                                            .build(),
                                    order.version()
                            ),
                            () -> orderRepository.save(Order.builder()
                                    .id(orderOffer.orderId())
                                    .offerAmount(orderOffer.amount())
                                    .offerType(orderOffer.offerType())
                                    .version(1L)
                                    .createdAt(DateUtils.utcLocalDateTime())
                                    .updatedAt(DateUtils.utcLocalDateTime())
                                    .build())
                    );
            eventRepository.save(orderOffer);
        } catch (Throwable err) {
            throw new UnableHandlingEvent(orderOffer, err);
        }
    }

    public void handleOrderCompleted(OrderCompleted orderCompleted) {
        try {
            orderRepository
                    .findById(orderCompleted.orderId())
                    .ifPresentOrElse(
                            order -> orderRepository.update(
                                    order.toBuilder()
                                            .foodsTotal(orderCompleted.foodsTotal())
                                            .taxes(orderCompleted.taxes())
                                            .version(order.version() + 1L)
                                            .updatedAt(DateUtils.utcLocalDateTime())
                                            .build(),
                                    order.version()
                            ),
                            () -> orderRepository.save(Order.builder()
                                    .id(orderCompleted.orderId())
                                    .foodsTotal(orderCompleted.foodsTotal())
                                    .taxes(orderCompleted.taxes())
                                    .version(1L)
                                    .createdAt(DateUtils.utcLocalDateTime())
                                    .updatedAt(DateUtils.utcLocalDateTime())
                                    .build())
                    );
            eventRepository.save(orderCompleted);
        } catch (Throwable err) {
            throw new UnableHandlingEvent(orderCompleted, err);
        }
    }
}
