package server.core.repository;

import common.entity.OrderDetailRoom;

import java.util.List;

public interface OrderDetailRoomRepository {
    List<OrderDetailRoom> findByOrderId(String orderId);
}