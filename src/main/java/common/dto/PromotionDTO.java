package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
@JsonIgnoreProperties(ignoreUnknown = true)

public class PromotionDTO implements Serializable {
    private String promotionId;
    private String promotionName;
    private double discount;
    private int quantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public PromotionDTO() {
    }

    public PromotionDTO(String promotionId, String promotionName, double discount,
                        int quantity, LocalDateTime startTime, LocalDateTime endTime) {
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
        this.promotionName = promotionName;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
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
        this.endTime = endTime;
    }
}