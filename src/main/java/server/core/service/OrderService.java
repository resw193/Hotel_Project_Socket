package server.core.service;

import common.dto.OrderDTO;
import common.dto.OrderDetailRoomDTO;
import common.dto.OrderDetailServiceDTO;
import common.dto.OrderPayDTO;
import common.dto.PromotionDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrdersByStatus(String status);

    OrderDTO getByID(String orderID);

    List<OrderDetailRoomDTO> getRoomLines(String orderID);

    List<OrderDetailServiceDTO> getServiceLines(String orderID);

    List<OrderDTO> searchOrders(String status, String keyword);

    List<OrderPayDTO> payOrder(String orderID);

    boolean updateOrderPromotion(String orderID, PromotionDTO promotionDTO);
}