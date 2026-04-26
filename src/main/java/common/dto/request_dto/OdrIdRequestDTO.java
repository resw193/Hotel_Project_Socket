package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class OdrIdRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String orderDetailRoomId;

    public OdrIdRequestDTO() {
    }

    public OdrIdRequestDTO(String orderDetailRoomId) {
        this.orderDetailRoomId = orderDetailRoomId;
    }

    public String getOrderDetailRoomId() {
        return orderDetailRoomId;
    }

    public void setOrderDetailRoomId(String orderDetailRoomId) {
        this.orderDetailRoomId = orderDetailRoomId;
    }
}