package server.core.service.impl;

import common.dto.OrderDetailServiceDTO;
import common.entity.OrderDetailService;
import server.core.repository.OrderDetailServiceRepository;
import server.core.service.OrderDetailServiceService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.stream.Collectors;

public class OrderDetailServiceServiceImpl implements OrderDetailServiceService {

    private final OrderDetailServiceRepository orderDetailServiceRepository;
    private final GenericDataMapper mapper;

    public OrderDetailServiceServiceImpl(OrderDetailServiceRepository orderDetailServiceRepository, GenericDataMapper mapper) {
        this.orderDetailServiceRepository = orderDetailServiceRepository;
        this.mapper = mapper;
    }

    @Override
    public List<OrderDetailServiceDTO> getByOrderID(String orderID) {
        return orderDetailServiceRepository.findByOrderId(orderID)
                .stream()
                .map(detail -> {
                    OrderDetailServiceDTO dto = mapper.toObject(mapper.toMap(detail), OrderDetailServiceDTO.class);

                    if (detail.getOrder() != null) {
                        dto.setOrderId(detail.getOrder().getOrderId());
                    }

                    if (detail.getService() != null) {
                        dto.setServiceId(detail.getService().getServiceId());
                        dto.setServiceName(detail.getService().getServiceName());
                    }

                    if (detail.getRoom() != null) {
                        dto.setRoomId(detail.getRoom().getRoomId());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}