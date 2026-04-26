package common.entity;

import common.enums.OrderStatus;

import java.time.LocalDateTime;

public class Order {
    private String orderId;
    private LocalDateTime orderDate;
    private double total;
    private Employee employee;
    private Customer customer;
    private Promotion promotion;
    private OrderStatus orderStatus;

    public Order() {
    }

    public Order(String orderId, LocalDateTime orderDate, double total, Employee employee, Customer customer, Promotion promotion, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.total = total;
        this.employee = employee;
        this.customer = customer;
        this.promotion = promotion;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee không được null");
        }
        this.employee = employee;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer không được null");
        }
        this.customer = customer;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}