package server.core.service.impl;

import common.dto.OrderDetailRoomDTO;
import common.entity.OrderDetailRoom;
import server.core.repository.OrderDetailRoomRepository;
import server.core.service.OrderDetailRoomService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.stream.Collectors;

public class OrderDetailRoomServiceImpl implements OrderDetailRoomService {

    private final OrderDetailRoomRepository orderDetailRoomRepository;
    private final GenericDataMapper mapper;

    public OrderDetailRoomServiceImpl(OrderDetailRoomRepository orderDetailRoomRepository, GenericDataMapper mapper) {
        this.orderDetailRoomRepository = orderDetailRoomRepository;
        this.mapper = mapper;
    }

    @Override
    public List<OrderDetailRoomDTO> getByOrderID(String orderID) {
        return orderDetailRoomRepository.findByOrderId(orderID)
                .stream()
                .map(detail -> {
                    OrderDetailRoomDTO dto = mapper.toObject(mapper.toMap(detail), OrderDetailRoomDTO.class);

                    if (detail.getOrder() != null) {
                        dto.setOrderId(detail.getOrder().getOrderId());
                    }

                    if (detail.getRoom() != null) {
                        dto.setRoomId(detail.getRoom().getRoomId());
                        dto.setRoomDescription(detail.getRoom().getDescription());

                        if (detail.getRoom().getRoomType() != null) {
                            dto.setRoomTypeName(detail.getRoom().getRoomType().getTypeName());
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}