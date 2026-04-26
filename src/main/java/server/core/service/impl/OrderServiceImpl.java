package server.core.service.impl;

import common.dto.OrderDTO;
import common.dto.OrderDetailRoomDTO;
import common.dto.OrderDetailServiceDTO;
import common.dto.OrderPayDTO;
import common.dto.PromotionDTO;
import common.entity.Order;
import common.entity.OrderDetailRoom;
import common.entity.OrderDetailService;
import server.core.repository.OrderDetailRoomRepository;
import server.core.repository.OrderDetailServiceRepository;
import server.core.repository.OrderRepository;
import server.core.service.OrderService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRoomRepository orderDetailRoomRepository;
    private final OrderDetailServiceRepository orderDetailServiceRepository;
    private final GenericDataMapper mapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderDetailRoomRepository orderDetailRoomRepository,
                            OrderDetailServiceRepository orderDetailServiceRepository, GenericDataMapper mapper) {
        this.orderRepository = orderRepository;
        this.orderDetailRoomRepository = orderDetailRoomRepository;
        this.orderDetailServiceRepository = orderDetailServiceRepository;
        this.mapper = mapper;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(order -> {
                    OrderDTO dto = mapper.toObject(mapper.toMap(order), OrderDTO.class);

                    if (order.getEmployee() != null) {
                        dto.setEmployeeId(order.getEmployee().getEmployeeId());
                        dto.setEmployeeName(order.getEmployee().getFullName());
                    }

                    if (order.getCustomer() != null) {
                        dto.setCustomerId(order.getCustomer().getCustomerId());
                        dto.setCustomerName(order.getCustomer().getFullName());
                        dto.setCustomerLoyaltyPoint(order.getCustomer().getLoyaltyPoint());
                    }

                    if (order.getPromotion() != null) {
                        dto.setPromotionId(order.getPromotion().getPromotionId());
                        dto.setPromotionName(order.getPromotion().getPromotionName());
                        dto.setPromotionDiscount(order.getPromotion().getDiscount());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(String status) {
        if (status == null || status.trim().isEmpty() || "Tất cả".equalsIgnoreCase(status.trim())) {
            return getAllOrders();
        }

        return orderRepository.findByStatus(status.trim())
                .stream()
                .map(order -> {
                    OrderDTO dto = mapper.toObject(mapper.toMap(order), OrderDTO.class);

                    if (order.getEmployee() != null) {
                        dto.setEmployeeId(order.getEmployee().getEmployeeId());
                        dto.setEmployeeName(order.getEmployee().getFullName());
                    }

                    if (order.getCustomer() != null) {
                        dto.setCustomerId(order.getCustomer().getCustomerId());
                        dto.setCustomerName(order.getCustomer().getFullName());
                        dto.setCustomerLoyaltyPoint(order.getCustomer().getLoyaltyPoint());

                    }

                    if (order.getPromotion() != null) {
                        dto.setPromotionId(order.getPromotion().getPromotionId());
                        dto.setPromotionName(order.getPromotion().getPromotionName());
                        dto.setPromotionDiscount(order.getPromotion().getDiscount());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getByID(String orderID) {
        if (isBlank(orderID)) {
            return null;
        }

        Order order = orderRepository.findById(orderID.trim());
        if (order == null) {
            return null;
        }

        OrderDTO dto = mapper.toObject(mapper.toMap(order), OrderDTO.class);

        if (order.getEmployee() != null) {
            dto.setEmployeeId(order.getEmployee().getEmployeeId());
            dto.setEmployeeName(order.getEmployee().getFullName());
        }

        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getCustomerId());
            dto.setCustomerName(order.getCustomer().getFullName());
            dto.setCustomerLoyaltyPoint(order.getCustomer().getLoyaltyPoint());

        }

        if (order.getPromotion() != null) {
            dto.setPromotionId(order.getPromotion().getPromotionId());
            dto.setPromotionName(order.getPromotion().getPromotionName());
            dto.setPromotionDiscount(order.getPromotion().getDiscount());
        }

        return dto;
    }

    @Override
    public List<OrderDetailRoomDTO> getRoomLines(String orderID) {
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

    @Override
    public List<OrderDetailServiceDTO> getServiceLines(String orderID) {
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

    @Override
    public List<OrderDTO> searchOrders(String status, String keyword) {
        List<OrderDTO> orders = getOrdersByStatus(status);

        if (keyword == null || keyword.isBlank()) {
            return orders;
        }

        String kw = normalize(keyword);

        return orders.stream()
                .filter(order -> {
                    String orderId = normalize(order.getOrderId());
                    String customerId = normalize(order.getCustomerId());
                    String customerName = normalize(order.getCustomerName());

                    String stack = orderId + " " + customerId + " " + customerName;
                    return stack.contains(kw);
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<OrderPayDTO> payOrder(String orderID) {
        if (isBlank(orderID)) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }

        return orderRepository.payOrder(orderID.trim());
    }

    @Override
    public boolean updateOrderPromotion(String orderID, PromotionDTO promotionDTO) {
        if (isBlank(orderID)) {
            throw new IllegalArgumentException("Mã hóa đơn trống.");
        }

        String promotionId = promotionDTO != null ? promotionDTO.getPromotionId() : null;
        return orderRepository.updateOrderPromotion(orderID.trim(), promotionId);
    }


    private String normalize(String s) {
        if (s == null) return "";
        String tmp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        tmp = tmp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return tmp.toLowerCase().trim();
    }


    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}