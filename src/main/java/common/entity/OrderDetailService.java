package common.entity;

public class OrderDetailService {
    private Order order;
    private int quantity;
    private double serviceFee;
    private Service service;
    private Room room;

    public OrderDetailService() {
    }

    public OrderDetailService(Order order, int quantity, double serviceFee, Service service, Room room) {
        this.order = order;
        this.quantity = quantity;
        this.serviceFee = serviceFee;
        this.service = service;
        this.room = room;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order không được null");
        }
        this.order = order;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity phải > 0");
        }
        this.quantity = quantity;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("Service không được null");
        }
        this.service = service;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}