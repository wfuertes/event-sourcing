package com.wfuertes.infra.nosql;

import com.wfuertes.domain.Order;
import com.wfuertes.domain.OrderRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DynamoOrderRepository implements OrderRepository {
    private static final String ORDERS_APP_TABLE = "orders_app";

    private final DynamoDbClient dynamo;

    public DynamoOrderRepository(DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public void save(Order order) {
        final var attributes = serializeToSave(order);
        final var putItemRequest = PutItemRequest
                .builder()
                .tableName(ORDERS_APP_TABLE)
                .item(attributes)
                .conditionExpression("attribute_not_exists(pk)")
                .build();
        dynamo.putItem(putItemRequest);
    }

    @Override
    public void update(Order order, long currentVersion) {
        final var attributes = serializeToUpdate(order);
        final StringJoiner updateExpr = new StringJoiner(", ", "SET ", "");
        final Map<String, String> exprAttrNames = new HashMap<>();
        final Map<String, AttributeValue> exprAttrValues = new HashMap<>();

        for (var entry : attributes.entrySet()) {
            String attrKey = "#" + entry.getKey();
            String valueKey = ":" + entry.getKey();
            updateExpr.add(attrKey + " = " + valueKey);
            exprAttrNames.put(attrKey, entry.getKey());
            exprAttrValues.put(valueKey, entry.getValue().value());
        }

        final var request = UpdateItemRequest
                .builder()
                .tableName(ORDERS_APP_TABLE)
                .key(Map.of(
                        "pk", AttributeValue.fromS(order.id()),
                        "sk", AttributeValue.fromS("ORDER")))
                .updateExpression(updateExpr.toString())
                .expressionAttributeNames(exprAttrNames)
                .expressionAttributeValues(exprAttrValues)
                .build();
        dynamo.updateItem(request);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        final var query = QueryRequest
                .builder()
                .tableName(ORDERS_APP_TABLE)
                .keyConditionExpression("#pk = :pk AND begins_with(#sk, :prefix)")
                .expressionAttributeNames(
                        Map.of("#pk", "pk",
                                "#sk", "sk"))
                .expressionAttributeValues(
                        Map.of(":pk", AttributeValue.fromS(orderId),
                                ":prefix", AttributeValue.fromS("ORDER")))
                .build();

        final var response = dynamo.query(query);
        if (!response.hasItems()) {
            return Optional.empty();
        }
        return response.items().stream().findAny().map(this::deserialize);
    }

    private Order deserialize(Map<String, AttributeValue> item) {
        final var id = item.get("pk").s();
        final var number = Optional.ofNullable(item.get("number"))
                .map(AttributeValue::n)
                .map(Long::parseLong)
                .orElse(null);
        final var type = Optional.ofNullable(item.get("type"))
                .map(AttributeValue::s)
                .orElse(null);
        final var foodsTotal = Optional.ofNullable(item.get("foodsTotal"))
                .map(AttributeValue::n)
                .map(Integer::parseInt)
                .orElse(null);
        final var taxes = Optional.ofNullable(item.get("taxes"))
                .map(AttributeValue::n)
                .map(Integer::parseInt)
                .orElse(null);
        final var discountTotal = Optional.ofNullable(item.get("discountTotal"))
                .map(AttributeValue::n)
                .map(Integer::parseInt)
                .orElse(null);
        final var offerAmount = Optional.ofNullable(item.get("offerAmount"))
                .map(AttributeValue::n)
                .map(Integer::parseInt)
                .orElse(null);
        final var offerType = Optional.ofNullable(item.get("offerType"))
                .map(AttributeValue::s)
                .orElse(null);
        final var version = Long.parseLong(item.get("version").n());

        final var createdAt = LocalDateTime.parse(item.get("createdAt").s());
        final var updateAt = LocalDateTime.parse(item.get("updatedAt").s());

        return Order.builder()
                .id(id)
                .number(number)
                .type(type)
                .foodsTotal(foodsTotal)
                .discountAmount(discountTotal)
                .taxes(taxes)
                .offerAmount(offerAmount)
                .offerType(offerType)
                .version(version)
                .createdAt(createdAt)
                .updatedAt(updateAt)
                .build();
    }

    private Map<String, AttributeValue> serializeToSave(Order order) {
        final Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put("pk", AttributeValue.fromS(order.id()));
        attributes.put("sk", AttributeValue.fromS("ORDER"));
        order.number().map(String::valueOf).map(AttributeValue::fromS).ifPresent(value -> attributes.put("number", value));
        order.type().map(AttributeValue::fromS).ifPresent(value -> attributes.put("type", value));
        order.foodsTotal().map(String::valueOf).map(AttributeValue::fromN).ifPresent(value -> attributes.put("foodsTotal", value));
        order.taxes().map(String::valueOf).map(AttributeValue::fromN).ifPresent(value -> attributes.put("taxes", value));
        order.discountAmount().map(String::valueOf).map(AttributeValue::fromN).ifPresent(value -> attributes.put("discountAmount", value));
        order.offerAmount().map(String::valueOf).map(AttributeValue::fromN).ifPresent(value -> attributes.put("offerAmount", value));
        order.offerType().map(AttributeValue::fromS).ifPresent(value -> attributes.put("offerTpe", value));
        attributes.put("version", AttributeValue.fromN(String.valueOf(order.version())));
        attributes.put("createdAt", AttributeValue.fromS(order.createdAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        attributes.put("updatedAt", AttributeValue.fromS(order.updatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        return attributes;
    }

    private Map<String, AttributeValueUpdate> serializeToUpdate(Order order) {
        final var attributes = serializeToSave(order);
        return attributes
                .entrySet()
                .stream()
                .filter(e -> !Set.of("pk", "sk").contains(e.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> AttributeValueUpdate
                                .builder()
                                .value(entry.getValue())
                                .action(AttributeAction.PUT)
                                .build())
                );
    }
}
