package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardTopServiceDTO implements Serializable {
    private String serviceName;
    private int totalQuantity;

    public DashboardTopServiceDTO() {
    }

    public DashboardTopServiceDTO(String serviceName, int totalQuantity) {
        this.serviceName = serviceName;
        this.totalQuantity = totalQuantity;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}