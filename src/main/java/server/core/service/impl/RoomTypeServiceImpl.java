package server.core.service.impl;

import common.dto.RoomTypeDTO;
import common.entity.RoomType;
import server.core.repository.RoomTypeRepository;
import server.core.service.RoomTypeService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.stream.Collectors;

public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final GenericDataMapper mapper;

    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository, GenericDataMapper mapper) {
        this.roomTypeRepository = roomTypeRepository;
        this.mapper = mapper;
    }

    @Override
    public List<RoomTypeDTO> getAll() {
        return roomTypeRepository.findAll()
                .stream()
                .map(roomType -> mapper.toObject(mapper.toMap(roomType), RoomTypeDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeDTO getByID(String roomTypeID) {
        if (isBlank(roomTypeID)) return null;

        RoomType roomType = roomTypeRepository.findById(roomTypeID.trim());
        return roomType == null ? null : mapper.toObject(mapper.toMap(roomType), RoomTypeDTO.class);
    }

    @Override
    public RoomTypeDTO getByTypeName(String typeName) {
        if (isBlank(typeName)) return null;

        RoomType roomType = roomTypeRepository.findByTypeName(typeName.trim());
        return roomType == null ? null : mapper.toObject(mapper.toMap(roomType), RoomTypeDTO.class);
    }

    @Override
    public boolean checkExist(String typeName) {
        return getByTypeName(typeName) != null;
    }

    @Override
    public boolean updateRoomTypePricing(RoomTypeDTO roomTypeDTO) {
        validateRoomTypePricing(roomTypeDTO);

        RoomType current = roomTypeRepository.findById(roomTypeDTO.getRoomTypeId());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy loại phòng.");
        }

        RoomType roomType = mapper.toObject(mapper.toMap(roomTypeDTO), RoomType.class);
        return roomTypeRepository.update(roomType);
    }

    private void validateRoomTypePricing(RoomTypeDTO roomTypeDTO) {
        if (roomTypeDTO == null || isBlank(roomTypeDTO.getRoomTypeId())) {
            throw new IllegalArgumentException("Loại phòng không hợp lệ.");
        }

        if (isBlank(roomTypeDTO.getTypeName())) {
            throw new IllegalArgumentException("Tên loại phòng không được rỗng.");
        }

        if (roomTypeDTO.getPricePerHour() < 0 || roomTypeDTO.getPricePerNight() < 0 || roomTypeDTO.getPricePerDay() < 0 || roomTypeDTO.getLateFeePerHour() < 0) {
            throw new IllegalArgumentException("Giá phòng và phụ thu phải >= 0.");
        }

        if (roomTypeDTO.getMaxAdults() < 0 || roomTypeDTO.getMaxChildren() < 0) {
            throw new IllegalArgumentException("Sức chứa không hợp lệ.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}