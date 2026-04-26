package server.core.repository;

import common.entity.Promotion;

import java.util.List;

public interface PromotionRepository {
    List<Promotion> findAll();

    Promotion findById(String promotionId);

    boolean add(Promotion promotion);

    boolean update(Promotion promotion);

    boolean deleteById(String promotionId);
}