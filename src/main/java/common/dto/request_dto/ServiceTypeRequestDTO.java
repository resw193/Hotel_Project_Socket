package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class ServiceTypeRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String serviceType;

    public ServiceTypeRequestDTO() {
    }

    public ServiceTypeRequestDTO(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}