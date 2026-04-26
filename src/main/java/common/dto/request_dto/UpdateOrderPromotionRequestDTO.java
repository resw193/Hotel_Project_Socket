package common.dto.request_dto;

import common.dto.PromotionDTO;

import java.io.Serial;
import java.io.Serializable;

public class UpdateOrderPromotionRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String orderId;
    private PromotionDTO promotionDTO;

    public UpdateOrderPromotionRequestDTO() {
    }

    public UpdateOrderPromotionRequestDTO(String orderId, PromotionDTO promotionDTO) {
        this.orderId = orderId;
        this.promotionDTO = promotionDTO;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PromotionDTO getPromotionDTO() {
        return promotionDTO;
    }

    public void setPromotionDTO(PromotionDTO promotionDTO) {
        this.promotionDTO = promotionDTO;
    }
}