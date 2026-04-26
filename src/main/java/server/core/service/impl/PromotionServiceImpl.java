package server.core.service.impl;

import common.dto.PromotionDTO;
import common.entity.Promotion;
import server.core.repository.PromotionRepository;
import server.core.service.IdGeneratorService;
import server.core.service.PromotionService;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final IdGeneratorService idGeneratorService;
    private final GenericDataMapper mapper;

    public PromotionServiceImpl(PromotionRepository promotionRepository, IdGeneratorService idGeneratorService, GenericDataMapper mapper) {
        this.promotionRepository = promotionRepository;
        this.idGeneratorService = idGeneratorService;
        this.mapper = mapper;
    }

    @Override
    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll()
                .stream()
                .map(promotion -> mapper.toObject(mapper.toMap(promotion), PromotionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public PromotionDTO getPromotionByID(String promotionID) {
        if (isBlank(promotionID)) {
            return null;
        }

        Promotion promotion = promotionRepository.findById(promotionID.trim());
        return promotion == null ? null : mapper.toObject(mapper.toMap(promotion), PromotionDTO.class);
    }

    @Override
    public boolean addPromotion(PromotionDTO promotionDTO) {
        validateForAdd(promotionDTO);

        if (isBlank(promotionDTO.getPromotionId())) {
            promotionDTO.setPromotionId(idGeneratorService.generatePromotionId());
        }

        Promotion promotion = mapper.toObject(mapper.toMap(promotionDTO), Promotion.class);
        return promotionRepository.add(promotion);
    }

    @Override
    public boolean updatePromotion(PromotionDTO promotionDTO) {
        if (promotionDTO == null || isBlank(promotionDTO.getPromotionId())) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ.");
        }

        validateForUpdate(promotionDTO);

        Promotion promotion = mapper.toObject(mapper.toMap(promotionDTO), Promotion.class);
        return promotionRepository.update(promotion);
    }

    @Override
    public boolean deletePromotion(String promotionID) {
        if (isBlank(promotionID)) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ.");
        }

        return promotionRepository.deleteById(promotionID.trim());
    }

    private void validateCommon(PromotionDTO promotionDTO) {
        if (promotionDTO == null) {
            throw new IllegalArgumentException("Dữ liệu khuyến mãi trống.");
        }

        if (promotionDTO.getPromotionName() == null || promotionDTO.getPromotionName().trim().isEmpty() || promotionDTO.getPromotionName().trim().length() > 100) {
            throw new IllegalArgumentException("Tên khuyến mãi không được rỗng và không quá 100 kí tự.");
        }

        if (Double.isNaN(promotionDTO.getDiscount()) || promotionDTO.getDiscount() <= 0 || promotionDTO.getDiscount() > 100) {
            throw new IllegalArgumentException("Tỉ lệ giảm giá phải trong (0, 100].");
        }

        LocalDateTime startTime = promotionDTO.getStartTime();
        LocalDateTime endTime = promotionDTO.getEndTime();

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu/kết thúc không được rỗng.");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
    }

    private void validateForAdd(PromotionDTO promotionDTO) {
        validateCommon(promotionDTO);

        if (promotionDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0.");
        }
    }

    private void validateForUpdate(PromotionDTO promotionDTO) {
        validateCommon(promotionDTO);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}