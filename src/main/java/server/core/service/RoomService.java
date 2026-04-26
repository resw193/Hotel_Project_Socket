package server.core.service;

import common.dto.RecommendOptionDTO;
import common.dto.RecommendRequestDTO;
import common.dto.RoomDTO;

import java.util.List;

public interface RoomService {
    List<RoomDTO> getAll();

    List<RoomDTO> getByStatus(String status);

    List<RoomDTO> getByTypeName(String typeName);

    List<RoomDTO> getByOccupancy(boolean available);

    RoomDTO getRoomByID(String roomID);

    List<RoomDTO> searchAndFilter(String keyword, String filter);

    boolean addRoom(RoomDTO roomDTO);

    boolean updateRoomInformation(RoomDTO roomDTO);

    List<RecommendOptionDTO> recommendRooms(RecommendRequestDTO request);
}