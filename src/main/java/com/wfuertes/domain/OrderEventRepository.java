package com.wfuertes.domain;

import com.wfuertes.infra.events.OrderCompleted;
import com.wfuertes.infra.events.OrderCreated;
import com.wfuertes.infra.events.OrderDiscount;
import com.wfuertes.infra.events.OrderOffer;

public interface OrderEventRepository {

    void save(OrderCreated orderCreated);

    void save(OrderCompleted orderCompleted);

    void save(OrderOffer orderOffer);

    void save(OrderDiscount orderDiscount);
}
