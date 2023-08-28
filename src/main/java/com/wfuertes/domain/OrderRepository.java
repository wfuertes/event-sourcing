package com.wfuertes.domain;


import java.util.Optional;

public interface OrderRepository {

    void save(Order order);

    void update(Order order, long currentVersion);

    Optional<Order> findById(String orderId);
}
