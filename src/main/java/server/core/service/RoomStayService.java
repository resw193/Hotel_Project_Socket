package server.core.service;

import common.dto.CustomerDTO;
import common.dto.OdrInfoDTO;
import common.dto.RoomCalendarSlotDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RoomStayService {
    boolean datPhong(CustomerDTO customerDTO, String roomID, String employeeID, LocalDateTime bookingDate,
                     LocalDateTime checkInDate, LocalDateTime checkOutDate, String bookingType);

    boolean huyDatPhong(String roomID);

    boolean huyDatPhongByOdrId(String orderDetailRoomId);

    boolean checkIn(String roomID);

    boolean checkInByOdrId(String orderDetailRoomId);

    boolean checkOut(String roomID);

    boolean giaHanPhong(String roomID, LocalDateTime newCheckOutDate);

    boolean addServiceToRoom(String roomID, String serviceName, int quantity);

    OdrInfoDTO getActiveBookingInfo(String roomID);

    OdrInfoDTO getPendingBooking(String roomID);

    List<OdrInfoDTO> getPendingBookingsOfRoom(String roomID);

    OdrInfoDTO getActiveCheckInInfo(String roomID);

    Double calculateRoomFeeAgainWithNewRoom(String newRoomID, String bookingType, LocalDateTime checkIn, LocalDateTime checkOut);

    Double calculateFeeByRoom(String roomID, String bookingType, LocalDateTime from, LocalDateTime to);

    boolean changeRoomBeforeCheckIn(String oldRoomID, String newRoomID, LocalDateTime newCheckIn, LocalDateTime newCheckOut);

    boolean changeRoomWhileCheckIn(String oldRoomID, String newRoomID, LocalDateTime changeTime);

    List<RoomCalendarSlotDTO> getCalendar(LocalDate fromDate, LocalDate toDate);

    LocalDateTime[] getActiveStayTimes(String roomID);
}