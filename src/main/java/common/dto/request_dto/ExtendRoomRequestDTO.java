package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ExtendRoomRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomID;
    private String orderDetailRoomId;
    private LocalDateTime newCheckOutDate;

    public ExtendRoomRequestDTO() {
    }

    public ExtendRoomRequestDTO(String roomID, LocalDateTime newCheckOutDate) {
        this.roomID = roomID;
        this.newCheckOutDate = newCheckOutDate;
    }

    public ExtendRoomRequestDTO(String roomID, String orderDetailRoomId, LocalDateTime newCheckOutDate) {
        this.roomID = roomID;
        this.orderDetailRoomId = orderDetailRoomId;
        this.newCheckOutDate = newCheckOutDate;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getOrderDetailRoomId() {
        return orderDetailRoomId;
    }

    public void setOrderDetailRoomId(String orderDetailRoomId) {
        this.orderDetailRoomId = orderDetailRoomId;
    }

    public LocalDateTime getNewCheckOutDate() {
        return newCheckOutDate;
    }

    public void setNewCheckOutDate(LocalDateTime newCheckOutDate) {
        this.newCheckOutDate = newCheckOutDate;
    }
}