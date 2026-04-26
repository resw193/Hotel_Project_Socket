package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class ServiceIdRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String serviceId;

    public ServiceIdRequestDTO() {
    }

    public ServiceIdRequestDTO(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}