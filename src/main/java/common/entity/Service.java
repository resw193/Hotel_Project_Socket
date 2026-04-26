package common.entity;

public class Service {
    private String serviceId;
    private String serviceName;
    private String serviceType;
    private int quantity;
    private double price;
    private String imgSource;
    private UnitPrice unitPrice;

    public Service() {

    }

    public Service(String serviceId, String serviceName, String serviceType, int quantity, double price, String imgSource, UnitPrice unitPrice) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.quantity = quantity;
        this.price = price;
        this.imgSource = imgSource;
        this.unitPrice = unitPrice;
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
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được rỗng");
        }
        this.serviceName = serviceName.trim();
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Giá dịch vụ phải > 0");
        }
        this.price = price;
    }

    public String getImgSource() {
        return imgSource;
    }

    public void setImgSource(String imgSource) {
        this.imgSource = imgSource;
    }

    public UnitPrice getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(UnitPrice unitPrice) {
        this.unitPrice = unitPrice;
    }
}