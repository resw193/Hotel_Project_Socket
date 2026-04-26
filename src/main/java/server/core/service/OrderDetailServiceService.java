package server.core.service;

import common.dto.OrderDetailServiceDTO;

import java.util.List;

public interface OrderDetailServiceService {
    List<OrderDetailServiceDTO> getByOrderID(String orderID);
}