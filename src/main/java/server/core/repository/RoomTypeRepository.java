package server.core.repository;

import common.entity.RoomType;

import java.util.List;

public interface RoomTypeRepository {
    List<RoomType> findAll();

    RoomType findById(String roomTypeId);

    RoomType findByTypeName(String typeName);

    boolean update(RoomType roomType);
}