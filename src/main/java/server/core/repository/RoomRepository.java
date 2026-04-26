package server.core.repository;

import common.entity.Room;

import java.util.List;

/*
Room: tách làm 2 nhóm

- Nhóm 1 là room catalog / filter / CRUD phòng --> RoomRepository

getAllRoom
getAllRoomByTypeName
getAllRoomByStatus
getAllRoomOccupancy
getRoomByID
addRoom
updateRoomInformation
getAllRoomByTypeAndView

- Nhóm 2 là booking / stay / room-operation --> RoomStayRepository

datPhong
huyDatPhong
checkIn
checkOut
giaHanPhong
capNhatDichVuChoPhong
getBookingPendingOfRoom
getActiveCheckInOfRoom
changeRoomBeforeCheckIn
changeRoomWhileCheckIn
getRoomCalendar
calculateRoomFeeByRoomID
getActiveStayTimes
 */
public interface RoomRepository {
    List<Room> findAll();

    List<Room> findByStatus(String status);

    List<Room> findByTypeName(String typeName);

    List<Room> findByOccupancy(boolean available);

    Room findById(String roomId);

    boolean add(Room room);

    boolean update(Room room);

    List<Room> findAvailableByTypeAndView(String roomTypeName, String view);
}