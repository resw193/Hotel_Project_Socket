package server.core.repository;

import common.dto.OrderPayDTO;
import common.entity.Order;

import java.util.List;

public interface OrderRepository {
    List<Order> findAll();

    List<Order> findByStatus(String status);

    Order findById(String orderId);

    boolean updateOrderPromotion(String orderId, String promotionId);

    List<OrderPayDTO> payOrder(String orderId);
}