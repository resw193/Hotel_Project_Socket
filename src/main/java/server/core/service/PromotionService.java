package server.core.service;

import common.dto.PromotionDTO;

import java.util.List;

public interface PromotionService {
    List<PromotionDTO> getAllPromotions();

    PromotionDTO getPromotionByID(String promotionID);

    boolean addPromotion(PromotionDTO promotionDTO);

    boolean updatePromotion(PromotionDTO promotionDTO);

    boolean deletePromotion(String promotionID);
}