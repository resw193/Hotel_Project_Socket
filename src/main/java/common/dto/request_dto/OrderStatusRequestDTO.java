package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class OrderStatusRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String status;

    public OrderStatusRequestDTO() {
    }

    public OrderStatusRequestDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}