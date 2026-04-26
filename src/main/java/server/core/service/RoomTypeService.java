package server.core.service;

import common.dto.RoomTypeDTO;

import java.util.List;

public interface RoomTypeService {
    List<RoomTypeDTO> getAll();

    RoomTypeDTO getByID(String roomTypeID);

    RoomTypeDTO getByTypeName(String typeName);

    boolean checkExist(String typeName);

    boolean updateRoomTypePricing(RoomTypeDTO roomTypeDTO);

}