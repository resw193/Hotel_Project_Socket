package server.core.service.impl;

import common.dto.RecommendOptionDTO;
import common.dto.RecommendRequestDTO;
import common.dto.RoomDTO;
import common.entity.Room;
import common.entity.RoomType;
import server.core.repository.RoomRepository;
import server.core.repository.RoomTypeRepository;
import server.core.service.IdGeneratorService;
import server.core.service.RoomService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final IdGeneratorService idGeneratorService;
    private final GenericDataMapper mapper;

    public RoomServiceImpl(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, IdGeneratorService idGeneratorService, GenericDataMapper mapper) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.idGeneratorService = idGeneratorService;
        this.mapper = mapper;
    }

    @Override
    public List<RoomDTO> getAll() {
        return roomRepository.findAll()
                .stream()
                .map(this::toRoomDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getByStatus(String status) {
        if (isBlank(status)) return Collections.emptyList();

        return roomRepository.findByStatus(status.trim())
                .stream()
                .map(this::toRoomDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getByTypeName(String typeName) {
        if (isBlank(typeName)) return Collections.emptyList();

        return roomRepository.findByTypeName(typeName.trim())
                .stream()
                .map(this::toRoomDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getByOccupancy(boolean available) {
        return roomRepository.findByOccupancy(available)
                .stream()
                .map(this::toRoomDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomByID(String roomID) {
        if (isBlank(roomID)) return null;

        Room room = roomRepository.findById(roomID.trim());
        return room == null ? null : toRoomDTO(room);
    }

    @Override
    public List<RoomDTO> searchAndFilter(String keyword, String filter) {
        List<RoomDTO> rooms = "Check-in".equalsIgnoreCase(String.valueOf(filter))
                ? getByStatus("Check-in")
                : getAll();

        if (keyword == null || keyword.isBlank()) return rooms;

        String kw = keyword.trim().toLowerCase();
        return rooms.stream()
                .filter(r -> r.getRoomId() != null && r.getRoomId().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addRoom(RoomDTO roomDTO) {
        if (roomDTO == null) {
            throw new IllegalArgumentException("Dữ liệu phòng không hợp lệ.");
        }

        String validation = validateDescription(roomDTO.getDescription());
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }

        RoomType roomType = roomTypeRepository.findByTypeName(check(roomDTO.getRoomTypeName()));
        if (roomType == null && !isBlank(roomDTO.getRoomTypeId())) {
            roomType = roomTypeRepository.findById(roomDTO.getRoomTypeId());
        }

        if (roomType == null) {
            throw new IllegalArgumentException("Loại phòng không hợp lệ");
        }

        roomDTO.setRoomId(idGeneratorService.generateRoomId());
        roomDTO.setAvailable(true);

        Room room = mapper.toObject(mapper.toMap(roomDTO), Room.class);
        room.setRoomType(roomType);

        return roomRepository.add(room);
    }

    @Override
    public boolean updateRoomInformation(RoomDTO roomDTO) {
        if (roomDTO == null || isBlank(roomDTO.getRoomId())) {
            throw new IllegalArgumentException("Mã phòng không hợp lệ");
        }

        String validation = validateDescription(roomDTO.getDescription());
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }

        Room current = roomRepository.findById(roomDTO.getRoomId().trim());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng");
        }

        Room room = mapper.toObject(mapper.toMap(roomDTO), Room.class);
        room.setAvailable(current.isAvailable());
        room.setRoomType(current.getRoomType());

        return roomRepository.update(room);
    }

    @Override
    public List<RecommendOptionDTO> recommendRooms(RecommendRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu gợi ý không hợp lệ.");
        }

        if (request.getAdults() <= 0) {
            throw new IllegalArgumentException("Số người lớn phải > 0");
        }

        if (request.getChildren() < 0) {
            throw new IllegalArgumentException("Số trẻ em phải >= 0");
        }

        List<Room> singles = roomRepository.findAvailableByTypeAndView("Phòng đơn", request.getView());
        List<Room> doubles = roomRepository.findAvailableByTypeAndView("Phòng đôi", request.getView());

        int adultsSingle = 2;
        int childrenSingle = 1;
        int adultsDouble = 4;
        int childrenDouble = 2;

        List<RecommendOptionDTO> result = new ArrayList<>();

        int maxD = doubles.size();
        for (int doubleUse = 0; doubleUse <= Math.min(maxD, (int) Math.ceil(request.getAdults() / 4.0) + 1); doubleUse++) {
            int remA = Math.max(0, request.getAdults() - doubleUse * adultsDouble);
            int singleUse = (int) Math.ceil(remA / (double) adultsSingle);
            if (singleUse > singles.size()) continue;

            while (doubleUse * childrenDouble + singleUse * childrenSingle < request.getChildren()
                    && singleUse < singles.size()) {
                singleUse++;
            }

            if (doubleUse > doubles.size() || singleUse > singles.size()) continue;

            boolean okAdults = (doubleUse * adultsDouble + singleUse * adultsSingle) >= request.getAdults();
            boolean okChildren = (doubleUse * childrenDouble + singleUse * childrenSingle) >= request.getChildren();
            if (!okAdults || !okChildren) continue;

            List<String> doubleIds = doubles.stream()
                    .limit(doubleUse)
                    .map(Room::getRoomId)
                    .collect(Collectors.toList());

            List<String> singleIds = singles.stream()
                    .limit(singleUse)
                    .map(Room::getRoomId)
                    .collect(Collectors.toList());

            RecommendOptionDTO option = new RecommendOptionDTO();
            option.setSingles(singleUse);
            option.setDoubles(doubleUse);
            option.setSingleRoomIDs(singleIds);
            option.setDoubleRoomIDs(doubleIds);

            result.add(option);
        }

        result.sort((o1, o2) -> {
            int r1 = o1.getSingles() + o1.getDoubles();
            int r2 = o2.getSingles() + o2.getDoubles();
            if (r1 != r2) return Integer.compare(r1, r2);
            if (o1.getDoubles() != o2.getDoubles()) return Integer.compare(o2.getDoubles(), o1.getDoubles());

            int extraAdultsA = (o1.getDoubles() * 4 + o1.getSingles() * 2) - request.getAdults();
            int extraAdultsB = (o2.getDoubles() * 4 + o2.getSingles() * 2) - request.getAdults();
            return Integer.compare(extraAdultsA, extraAdultsB);
        });

        int topK = request.getTopK() <= 0 ? result.size() : request.getTopK();
        return result.stream().limit(topK).collect(Collectors.toList());
    }

    private RoomDTO toRoomDTO(Room room) {
        if (room == null) return null;

        RoomDTO dto = new RoomDTO();
        dto.setRoomId(room.getRoomId());
        dto.setDescription(room.getDescription());
        dto.setAvailable(room.isAvailable());
        dto.setImgRoomSource(room.getImgRoomSource());
        dto.setView(room.getView());

        if (room.getRoomType() != null) {
            dto.setRoomTypeId(room.getRoomType().getRoomTypeId());
            dto.setRoomTypeName(room.getRoomType().getTypeName());
        }

        return dto;
    }

    private String validateDescription(String description) {
        if (description == null || description.trim().isEmpty() || description.length() > 200) {
            return "Mô tả phòng phải khác rỗng và không quá 200 kí tự";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String check(String value) {
        return value == null ? "" : value.trim();
    }
}