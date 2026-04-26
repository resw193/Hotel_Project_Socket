package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class IncreaseServiceQuantityRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String serviceId;
    private int addQuantity;

    public IncreaseServiceQuantityRequestDTO() {
    }

    public IncreaseServiceQuantityRequestDTO(String serviceId, int addQuantity) {
        this.serviceId = serviceId;
        this.addQuantity = addQuantity;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getAddQuantity() {
        return addQuantity;
    }

    public void setAddQuantity(int addQuantity) {
        this.addQuantity = addQuantity;
    }
}