package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class OrderDetailServiceDTO implements Serializable {
    private String orderId;
    private String serviceId;
    private String serviceName;
    private String roomId;
    private int quantity;
    private double serviceFee;

    public OrderDetailServiceDTO() {
    }

    public OrderDetailServiceDTO(String orderId, String serviceId, String serviceName, String roomId, int quantity, double serviceFee) {
        this.orderId = orderId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.roomId = roomId;
        this.quantity = quantity;
        this.serviceFee = serviceFee;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }
}