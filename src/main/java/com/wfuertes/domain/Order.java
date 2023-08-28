package com.wfuertes.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Optional;

@Value
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class Order {
    @NonNull String id;
    Long number;
    String type;
    Integer foodsTotal;
    Integer taxes;
    Integer discountAmount;
    Integer offerAmount;
    String offerType;
    long version;
    @NonNull LocalDateTime createdAt;
    @NonNull LocalDateTime updatedAt;

    public Optional<Long> number() {
        return Optional.ofNullable(number);
    }

    public Optional<String> type() {
        return Optional.ofNullable(type);
    }

    public Optional<Integer> foodsTotal() {
        return Optional.ofNullable(foodsTotal);
    }

    public Optional<Integer> taxes() {
        return Optional.ofNullable(taxes);
    }

    public Optional<Integer> discountAmount() {
        return Optional.ofNullable(discountAmount);
    }

    public Optional<Integer> offerAmount() {
        return Optional.ofNullable(offerAmount);
    }

    public Optional<String> offerType() {
        return Optional.ofNullable(offerType);
    }
}
