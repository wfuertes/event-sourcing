package com.wfuertes.infra;

import com.github.javafaker.Faker;
import com.wfuertes.domain.OrderService;
import com.wfuertes.infra.aws.SnsPublisher;
import com.wfuertes.infra.aws.SqsConsumer;
import com.wfuertes.infra.events.*;
import com.wfuertes.infra.json.JsonParser;
import com.wfuertes.infra.nosql.DynamoOrderEventRepository;
import com.wfuertes.infra.nosql.DynamoOrderRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventSimulator {
    private static final Faker FAKER = new Faker();

    private final SnsPublisher snsPublisher;
    private final SqsConsumer sqsConsumer;
    private final OrderService orderService;

    public EventSimulator(SnsPublisher snsPublisher, SqsConsumer sqsConsumer, OrderService orderService) {
        this.snsPublisher = snsPublisher;
        this.sqsConsumer = sqsConsumer;
        this.orderService = orderService;
    }

    public static void main(String[] args) {
        final var jsonParser = new JsonParser();
        final var snsPublisher = new SnsPublisher(jsonParser);
        final var sqsConsumer = new SqsConsumer(jsonParser);
        final var dynamoClient = DynamoDBConfig.createClient();
        final var orderService = new OrderService(
                new DynamoOrderRepository(dynamoClient),
                new DynamoOrderEventRepository(dynamoClient)
        );
        final var simulator = new EventSimulator(snsPublisher, sqsConsumer, orderService);

        simulator.startPublisher();
        simulator.consumeOrderCreated();
        simulator.consumeOrderCompleted();
        simulator.consumeOrderDiscount();
        simulator.consumeOrderOffer();
    }

    private void startPublisher() {
        final Runnable publisher = () -> {
            try {
                while (true) {
                    Set<Order> orderEvents = create();

                    for (Order order : orderEvents) {
                        if (order instanceof OrderCreated) {
                            snsPublisher.publish(SnsPublisher.ORDER_CREATED, order);
                        }

                        if (order instanceof OrderDiscount) {
                            snsPublisher.publish(SnsPublisher.ORDER_DISCOUNT, order);
                        }

                        if (order instanceof OrderOffer) {
                            snsPublisher.publish(SnsPublisher.ORDER_OFFER, order);
                        }

                        if (order instanceof OrderCompleted) {
                            snsPublisher.publish(SnsPublisher.ORDER_COMPLETED, order);
                        }

                        final var deplay = FAKER.number().numberBetween(200L, 500L);
                        Thread.sleep(deplay);
                    }
                }

            } catch (Throwable err) {
                throw new RuntimeException(err);
            }
        };
        Executors.newFixedThreadPool(4).execute(publisher);
    }

    private void consumeOrderCreated() {
        final Runnable consumer = () -> sqsConsumer.consume(SqsConsumer.ORDER_CREATED, orderService::handleOrderCreated, OrderCreated.class);
        Executors.newFixedThreadPool(1).execute(consumer);
    }

    private void consumeOrderDiscount() {
        final Runnable consumer = () -> sqsConsumer.consume(SqsConsumer.ORDER_DISCOUNT, orderService::handlerOrderDiscount, OrderDiscount.class);
        Executors.newFixedThreadPool(1).execute(consumer);
    }

    private void consumeOrderOffer() {
        final Runnable consumer = () -> sqsConsumer.consume(SqsConsumer.ORDER_OFFER, orderService::handlerOrderOffer, OrderOffer.class);
        Executors.newFixedThreadPool(1).execute(consumer);
    }

    private void consumeOrderCompleted() {
        final Runnable consumer = () -> sqsConsumer.consume(SqsConsumer.ORDER_COMPLETED, orderService::handleOrderCompleted, OrderCompleted.class);
        Executors.newFixedThreadPool(1).execute(consumer);
    }

    private static Set<Order> create() {
        final var orderCreated = new OrderCreated(
                UUID.randomUUID().toString(),
                FAKER.number().randomNumber(),
                FAKER.random().nextBoolean() ? "DELIVERY" : "PICKUP"
        );

        final var orderDiscount = new OrderDiscount(orderCreated.orderId(), FAKER.random().nextInt(0, 3000));

        final var orderOffer = new OrderOffer(orderCreated.orderId(), FAKER.random().nextInt(0, 3000), "SUPER_10");

        var foodsTotal = FAKER.random().nextInt(1000, 10000);
        var taxes = foodsTotal * FAKER.number().numberBetween(5, 13) / 100;
        final var orderCompleted = new OrderCompleted(orderCreated.orderId(), foodsTotal, taxes);

        final var orders = Stream
                .of(orderCreated, orderDiscount, orderOffer, orderCompleted)
                .collect(Collectors.toList());

        Collections.shuffle(orders);
        return new HashSet<>(orders);
    }
}
