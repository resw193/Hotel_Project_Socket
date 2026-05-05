package server.core.repository;

import common.dto.RoomCalendarSlotDTO;
import common.entity.Customer;
import common.entity.OrderDetailRoom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RoomStayRepository {

    boolean datPhong(Customer customer, String roomID, String employeeID, LocalDateTime bookingDate,
                     LocalDateTime checkInDate, LocalDateTime checkOutDate, String bookingType);

    boolean huyDatPhong(String roomID);

    boolean huyDatPhongByOdrId(String orderDetailRoomId);

    boolean checkIn(String roomID);

    boolean checkInByOdrId(String orderDetailRoomId);

    boolean checkOut(String roomID);

    boolean giaHanPhong(String roomID, LocalDateTime newCheckOutDate);

    boolean giaHanPhongByOdrId(String orderDetailRoomId, LocalDateTime newCheckOutDate);

    boolean capNhatDichVuChoPhong(String roomID, String serviceName, int quantity);

    OrderDetailRoom getPendingBookingByRoomId(String roomID);

    List<OrderDetailRoom> getPendingBookingsOfRoom(String roomID);

    OrderDetailRoom getActiveCheckInOfRoom(String roomID);

    boolean changeRoomBeforeCheckIn(String orderDetailRoomId, String oldRoomID, String newRoomID, LocalDateTime newCheckIn, LocalDateTime newCheckOut);

    boolean changeRoomWhileCheckIn(String oldRoomID, String newRoomID, LocalDateTime changeTime);

    List<RoomCalendarSlotDTO> getRoomCalendar(LocalDate fromDate, LocalDate toDate);

    Double calculateRoomFeeByRoomID(String roomID, String bookingType, LocalDateTime checkIn, LocalDateTime checkOut);

    LocalDateTime[] getActiveStayTimes(String roomID);
}