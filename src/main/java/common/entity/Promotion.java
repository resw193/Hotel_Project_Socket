package common.entity;

import java.time.LocalDateTime;

public class Promotion {
    private String promotionId;
    private String promotionName;
    private double discount;
    private int quantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Promotion() {
    }

    public Promotion(String promotionId, String promotionName, double discount, int quantity, LocalDateTime startTime, LocalDateTime endTime) {
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.discount = discount;
        this.quantity = quantity;
        this.startTime = startTime;
        this.endTime = endTime;
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
        if (promotionName == null || promotionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khuyến mãi không được rỗng");
        }
        this.promotionName = promotionName.trim();
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount phải thuộc [0,100]");
        }
        this.discount = discount;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time phải sau start time");
        }
        this.endTime = endTime;
    }
}