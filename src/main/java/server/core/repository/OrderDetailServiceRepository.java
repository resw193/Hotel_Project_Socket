package server.core.repository;

import common.entity.OrderDetailService;

import java.util.List;

public interface OrderDetailServiceRepository {
    List<OrderDetailService> findByOrderId(String orderId);
}