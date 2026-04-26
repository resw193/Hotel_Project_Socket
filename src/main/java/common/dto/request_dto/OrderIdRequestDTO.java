package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class OrderIdRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String orderId;

    public OrderIdRequestDTO() {
    }

    public OrderIdRequestDTO(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}