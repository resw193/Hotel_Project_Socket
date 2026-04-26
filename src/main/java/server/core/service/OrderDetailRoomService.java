package server.core.service;

import common.dto.OrderDetailRoomDTO;

import java.util.List;

public interface OrderDetailRoomService {
    List<OrderDetailRoomDTO> getByOrderID(String orderID);
}