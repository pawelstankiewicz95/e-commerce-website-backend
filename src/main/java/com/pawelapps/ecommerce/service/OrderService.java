package com.pawelapps.ecommerce.service;

import com.pawelapps.ecommerce.dto.OrderDto;
import com.pawelapps.ecommerce.entity.Order;

import java.util.List;

public interface OrderService {
    Order saveOrder(OrderDto orderDto);

    List<OrderDto> getAllOrders();

    List<OrderDto> findByCustomerEmail(String customerEmail);

    List<OrderDto> findByUserEmail(String userEmail);

    OrderDto findById(Long id);
}
