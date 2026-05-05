package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ChangeRoomBeforeCheckInRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String orderDetailRoomId;
    private String oldRoomID;
    private String newRoomID;
    private LocalDateTime newCheckIn;
    private LocalDateTime newCheckOut;

    public ChangeRoomBeforeCheckInRequestDTO() {
    }

    // Giữ constructor cũ để các chỗ gọi cũ chưa sửa không bị lỗi compile ngay
    public ChangeRoomBeforeCheckInRequestDTO(String oldRoomID, String newRoomID,
                                             LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
        this.oldRoomID = oldRoomID;
        this.newRoomID = newRoomID;
        this.newCheckIn = newCheckIn;
        this.newCheckOut = newCheckOut;
    }

    // Constructor mới: đổi đúng booking được chọn
    public ChangeRoomBeforeCheckInRequestDTO(String orderDetailRoomId, String oldRoomID, String newRoomID,
                                             LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
        this.orderDetailRoomId = orderDetailRoomId;
        this.oldRoomID = oldRoomID;
        this.newRoomID = newRoomID;
        this.newCheckIn = newCheckIn;
        this.newCheckOut = newCheckOut;
    }

    public String getOrderDetailRoomId() {
        return orderDetailRoomId;
    }

    public void setOrderDetailRoomId(String orderDetailRoomId) {
        this.orderDetailRoomId = orderDetailRoomId;
    }

    public String getOldRoomID() {
        return oldRoomID;
    }

    public void setOldRoomID(String oldRoomID) {
        this.oldRoomID = oldRoomID;
    }

    public String getNewRoomID() {
        return newRoomID;
    }

    public void setNewRoomID(String newRoomID) {
        this.newRoomID = newRoomID;
    }

    public LocalDateTime getNewCheckIn() {
        return newCheckIn;
    }

    public void setNewCheckIn(LocalDateTime newCheckIn) {
        this.newCheckIn = newCheckIn;
    }

    public LocalDateTime getNewCheckOut() {
        return newCheckOut;
    }

    public void setNewCheckOut(LocalDateTime newCheckOut) {
        this.newCheckOut = newCheckOut;
    }
}