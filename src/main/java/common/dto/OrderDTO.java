package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import common.enums.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
@JsonIgnoreProperties(ignoreUnknown = true)

public class OrderDTO implements Serializable {
    private String orderId;
    private LocalDateTime orderDate;
    private double total;
    private String employeeId;
    private String employeeName;
    private String customerId;
    private String customerName;
    private int customerLoyaltyPoint;
    private String promotionId;
    private String promotionName;
    private double promotionDiscount;
    private OrderStatus orderStatus;

    public OrderDTO() {
    }

    public OrderDTO(String orderId, LocalDateTime orderDate, double total, String employeeId, String employeeName, String customerId, String customerName,
                    int customerLoyaltyPoint, String promotionId, String promotionName, double promotionDiscount, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.total = total;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerLoyaltyPoint = customerLoyaltyPoint;
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.promotionDiscount = promotionDiscount;
        this.orderStatus = orderStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getPromotionDiscount() {
        return promotionDiscount;
    }

    public void setPromotionDiscount(double promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
    }

    public int getCustomerLoyaltyPoint() {
        return customerLoyaltyPoint;
    }

    public void setCustomerLoyaltyPoint(int customerLoyaltyPoint) {
        this.customerLoyaltyPoint = customerLoyaltyPoint;
    }
}