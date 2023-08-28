package com.wfuertes.infra.nosql;

import com.wfuertes.domain.OrderEventRepository;
import com.wfuertes.infra.events.OrderCompleted;
import com.wfuertes.infra.events.OrderCreated;
import com.wfuertes.infra.events.OrderDiscount;
import com.wfuertes.infra.events.OrderOffer;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DynamoOrderEventRepository implements OrderEventRepository {
    private static final String ORDERS_APP_TABLE = "orders_app";

    private final DynamoDbClient dynamo;

    public DynamoOrderEventRepository(DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public void save(OrderCreated orderCreated) {
        final var sk = "ORDER_EVENT#%s".formatted(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        final var eventContent = Map.of(
                "orderNumber", AttributeValue.fromN(String.valueOf(orderCreated.orderNumber())),
                "type", AttributeValue.fromS(orderCreated.type())
        );
        dynamo.putItem(PutItemRequest
                .builder()
                .tableName(ORDERS_APP_TABLE)
                .item(Map.ofEntries(
                        Map.entry("pk", AttributeValue.fromS(orderCreated.orderId())),
                        Map.entry("sk", AttributeValue.fromS(sk)),
                        Map.entry("eventType", AttributeValue.fromS("OrderCreated")),
                        Map.entry("eventContent", AttributeValue.fromM(eventContent))))
                .build());
    }

    @Override
    public void save(OrderCompleted orderCompleted) {
        final var sk = "ORDER_EVENT#%s".formatted(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        final var eventContent = Map.of(
                "foodsTotal", AttributeValue.fromN(String.valueOf(orderCompleted.foodsTotal())),
                "taxes", AttributeValue.fromN(String.valueOf(orderCompleted.foodsTotal()))
        );
        dynamo.putItem(PutItemRequest
                .builder()
                .tableName(ORDERS_APP_TABLE)
                .item(Map.ofEntries(
                        Map.entry("pk", AttributeValue.fromS(orderCompleted.orderId())),
                        Map.entry("sk", AttributeValue.fromS(sk)),
                        Map.entry("eventType", AttributeValue.fromS("OrderCompleted")),
                        Map.entry("eventContent", AttributeValue.fromM(eventContent))))
                .build());
    }

    @Override
    public void save(OrderOffer orderOffer) {
        final var sk = "ORDER_EVENT#%s".formatted(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        final var eventContent = Map.of(
                "amount", AttributeValue.fromN(String.valueOf(orderOffer.amount())),
                "offerType", AttributeValue.fromS(orderOffer.offerType())
        );
        dynamo.putItem(PutItemRequest
                .builder()
                .tableName(ORDERS_APP_TABLE)
                .item(Map.ofEntries(
                        Map.entry("pk", AttributeValue.fromS(orderOffer.orderId())),
                        Map.entry("sk", AttributeValue.fromS(sk)),
                        Map.entry("eventType", AttributeValue.fromS("OrderOffer")),
                        Map.entry("eventContent", AttributeValue.fromM(eventContent))))
                .build());
    }

    @Override
    public void save(OrderDiscount orderDiscount) {
        final var sk = "ORDER_EVENT#%s".formatted(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        final var eventContent = Map.of("amount", AttributeValue.fromN(String.valueOf(orderDiscount.amount())));
        dynamo.putItem(PutItemRequest
                .builder()
                .tableName(ORDERS_APP_TABLE)
                .item(Map.ofEntries(
                        Map.entry("pk", AttributeValue.fromS(orderDiscount.orderId())),
                        Map.entry("sk", AttributeValue.fromS(sk)),
                        Map.entry("eventType", AttributeValue.fromS("OrderDiscount")),
                        Map.entry("eventContent", AttributeValue.fromM(eventContent))))
                .build());
    }
}
