package common.entity;

import common.enums.BookingType;

import java.time.LocalDateTime;

public class OrderDetailRoom {
    private String orderDetailRoomId;
    private Order order;
    private Room room;
    private double roomFee;
    private LocalDateTime bookingDate;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BookingType bookingType;
    private String status;

    public OrderDetailRoom() {
    }

    public OrderDetailRoom(String orderDetailRoomId, Order order, Room room, double roomFee,
                           LocalDateTime bookingDate, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                           BookingType bookingType, String status) {
        this.orderDetailRoomId = orderDetailRoomId;
        this.order = order;
        this.room = room;
        this.roomFee = roomFee;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingType = bookingType;
        this.status = status;
    }

    public String getOrderDetailRoomId() {
        return orderDetailRoomId;
    }

    public void setOrderDetailRoomId(String orderDetailRoomId) {
        this.orderDetailRoomId = orderDetailRoomId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order không được null");
        }
        this.order = order;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room không được null");
        }
        this.room = room;
    }

    public double getRoomFee() {
        return roomFee;
    }

    public void setRoomFee(double roomFee) {
        this.roomFee = roomFee;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDateTime getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDateTime checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDateTime getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDateTime checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}