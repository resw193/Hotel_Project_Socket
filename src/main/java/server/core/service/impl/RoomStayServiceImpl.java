package server.core.service.impl;

import common.dto.CustomerDTO;
import common.dto.OdrInfoDTO;
import common.dto.RoomCalendarSlotDTO;
import common.entity.Customer;
import common.entity.OrderDetailRoom;
import server.core.repository.RoomStayRepository;
import server.core.service.RoomStayService;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RoomStayServiceImpl implements RoomStayService {

    private RoomStayRepository roomStayRepository;
    private GenericDataMapper mapper;

    public RoomStayServiceImpl(RoomStayRepository roomStayRepository, GenericDataMapper mapper) {
        this.roomStayRepository = roomStayRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean datPhong(CustomerDTO customerDTO, String roomID, String employeeID,
                            LocalDateTime bookingDate, LocalDateTime checkInDate, LocalDateTime checkOutDate, String bookingType) {
        if (customerDTO == null) {
            throw new IllegalArgumentException("Thiếu thông tin khách hàng.");
        }
        if (isBlank(roomID) || isBlank(employeeID) || isBlank(bookingType)) {
            throw new IllegalArgumentException("Thiếu dữ liệu bắt buộc.");
        }
        validateTimeRange(checkInDate, checkOutDate);

        LocalDateTime actualBookingDate = bookingDate == null ? LocalDateTime.now() : bookingDate;
        if (checkInDate.isBefore(actualBookingDate)) {
            throw new IllegalArgumentException("Thời gian check-in phải sau hoặc bằng thời gian đặt phòng.");
        }

        Customer customer = mapper.toObject(mapper.toMap(customerDTO), Customer.class);

        boolean ok = roomStayRepository.datPhong(customer, roomID.trim(), employeeID.trim(), actualBookingDate, checkInDate, checkOutDate, bookingType.trim());

        if (!ok) {
            throw new IllegalArgumentException("Phòng đã có lịch đặt hoặc đang check-in trong khoảng thời gian này.");
        }

        return true;
    }

    @Override
    public boolean huyDatPhong(String roomID) {
        if (isBlank(roomID)) {
            throw new IllegalArgumentException("Mã phòng không hợp lệ.");
        }
        return roomStayRepository.huyDatPhong(roomID.trim());
    }

    @Override
    public boolean huyDatPhongByOdrId(String orderDetailRoomId) {
        if (isBlank(orderDetailRoomId)) {
            throw new IllegalArgumentException("Mã chi tiết đặt phòng không hợp lệ.");
        }

        return roomStayRepository.huyDatPhongByOdrId(orderDetailRoomId.trim());
    }

    @Override
    public boolean checkIn(String roomID) {
        if (isBlank(roomID)) {
            throw new IllegalArgumentException("Mã phòng không hợp lệ.");
        }

        String normalizedRoomId = roomID.trim();

        OrderDetailRoom active = roomStayRepository.getActiveCheckInOfRoom(normalizedRoomId);
        if (active != null) {
            throw new IllegalArgumentException("Phòng đã có người check-in không thể check-in.");
        }

        boolean ok = roomStayRepository.checkIn(normalizedRoomId);
        if (!ok) {
            throw new IllegalArgumentException("Không tìm thấy booking chờ check-in hợp lệ.");
        }

        return true;
    }

    @Override
    public boolean checkInByOdrId(String orderDetailRoomId) {
        if (isBlank(orderDetailRoomId)) {
            throw new IllegalArgumentException("Mã chi tiết đặt phòng không hợp lệ.");
        }

        boolean ok = roomStayRepository.checkInByOdrId(orderDetailRoomId.trim());
        if (!ok) {
            throw new IllegalArgumentException("Phòng đã có người check-in không thể check-in.");
        }

        return true;
    }

    @Override
    public boolean checkOut(String roomID) {
        if (isBlank(roomID)) {
            throw new IllegalArgumentException("Mã phòng không hợp lệ.");
        }

        return roomStayRepository.checkOut(roomID.trim());
    }

    @Override
    public boolean giaHanPhong(String roomID, LocalDateTime newCheckOutDate) {
        if (isBlank(roomID)) {
            throw new IllegalArgumentException("Mã phòng không hợp lệ.");
        }
        if (newCheckOutDate == null) {
            throw new IllegalArgumentException("Chưa chọn thời gian check-out mới.");
        }

        LocalDateTime[] active = roomStayRepository.getActiveStayTimes(roomID.trim());
        LocalDateTime curIn = active != null ? active[0] : null;
        LocalDateTime curOut = active != null ? active[1] : null;

        if (curIn == null) {
            throw new IllegalArgumentException("Chỉ gia hạn được phòng đang Đặt / Check-in.");
        }
        if (!newCheckOutDate.isAfter(curIn)) {
            throw new IllegalArgumentException("Check-out mới phải sau check-in.");
        }
        if (curOut != null && !newCheckOutDate.isAfter(curOut)) {
            throw new IllegalArgumentException("Check-out mới phải sau check-out hiện tại.");
        }

        return roomStayRepository.giaHanPhong(roomID.trim(), newCheckOutDate);
    }

    @Override
    public boolean addServiceToRoom(String roomID, String serviceName, int quantity) {
        if (isBlank(roomID) || isBlank(serviceName)) {
            throw new IllegalArgumentException("Thiếu dữ liệu bắt buộc.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0.");
        }

        return roomStayRepository.capNhatDichVuChoPhong(roomID.trim(), serviceName.trim(), quantity);
    }

    @Override
    public OdrInfoDTO getActiveBookingInfo(String roomID) {
        return getPendingBooking(roomID);
    }

    @Override
    public OdrInfoDTO getPendingBooking(String roomID) {
        if (isBlank(roomID)) return null;
        OrderDetailRoom odr = roomStayRepository.getPendingBookingByRoomId(roomID.trim());
        return odr == null ? null : toOdrInfoDTO(odr);
    }

    @Override
    public List<OdrInfoDTO> getPendingBookingsOfRoom(String roomID) {
        if (roomID == null || roomID.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderDetailRoom> list = roomStayRepository.getPendingBookingsOfRoom(roomID.trim());
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream().map(this::toOdrInfoDTO).collect(Collectors.toList());
    }

    @Override
    public OdrInfoDTO getActiveCheckInInfo(String roomID) {
        if (roomID == null || roomID.trim().isEmpty()) {
            return null;
        }

        OrderDetailRoom odr = roomStayRepository.getActiveCheckInOfRoom(roomID.trim());
        return odr == null ? null : toOdrInfoDTO(odr);
    }

    @Override
    public Double calculateRoomFeeAgainWithNewRoom(String newRoomID, String bookingType, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (isBlank(newRoomID)) {
            throw new IllegalArgumentException("Thiếu mã phòng mới.");
        }
        if (isBlank(bookingType)) {
            throw new IllegalArgumentException("Thiếu loại đặt phòng.");
        }
        validateTimeRange(checkIn, checkOut);

        return roomStayRepository.calculateRoomFeeByRoomID(newRoomID.trim(), bookingType.trim(), checkIn, checkOut);
    }

    @Override
    public Double calculateFeeByRoom(String roomID, String bookingType, LocalDateTime from, LocalDateTime to) {
        if (isBlank(roomID)) {
            throw new IllegalArgumentException("Thiếu mã phòng.");
        }
        if (isBlank(bookingType)) {
            throw new IllegalArgumentException("Thiếu loại đặt phòng.");
        }
        validateTimeRange(from, to);

        return roomStayRepository.calculateRoomFeeByRoomID(roomID.trim(), bookingType.trim(), from, to);
    }

    @Override
    public boolean changeRoomBeforeCheckIn(String oldRoomID, String newRoomID, LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
        if (isBlank(oldRoomID) || isBlank(newRoomID)) {
            throw new IllegalArgumentException("Thiếu mã phòng.");
        }
        validateTimeRange(newCheckIn, newCheckOut);

        return roomStayRepository.changeRoomBeforeCheckIn(oldRoomID.trim(), newRoomID.trim(), newCheckIn, newCheckOut);
    }

    @Override
    public boolean changeRoomWhileCheckIn(String oldRoomID, String newRoomID, LocalDateTime changeTime) {
        if (isBlank(oldRoomID) || isBlank(newRoomID)) {
            throw new IllegalArgumentException("Thiếu mã phòng.");
        }
        if (changeTime == null) {
            throw new IllegalArgumentException("Thiếu thời điểm đổi phòng.");
        }

        OrderDetailRoom odr = roomStayRepository.getActiveCheckInOfRoom(oldRoomID.trim());
        OdrInfoDTO dto = odr == null ? null : toOdrInfoDTO(odr);

        if (dto == null || dto.getCheckIn() == null || dto.getCheckOut() == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên Check-in hợp lệ để đổi phòng.");
        }

        if (!(changeTime.isAfter(dto.getCheckIn()) && changeTime.isBefore(dto.getCheckOut()))) {
            throw new IllegalArgumentException("Thời điểm chuyển phải sau Check-in và trước Check-out hiện tại.");
        }

        return roomStayRepository.changeRoomWhileCheckIn(oldRoomID.trim(), newRoomID.trim(), changeTime);
    }

    @Override
    public List<RoomCalendarSlotDTO> getCalendar(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null || toDate.isBefore(fromDate)) {
            return Collections.emptyList();
        }
        return roomStayRepository.getRoomCalendar(fromDate, toDate);
    }



    @Override
    public LocalDateTime[] getActiveStayTimes(String roomID) {
        if (isBlank(roomID)) {
            return new LocalDateTime[]{null, null};
        }
        return roomStayRepository.getActiveStayTimes(roomID.trim());
    }

    private OdrInfoDTO toOdrInfoDTO(OrderDetailRoom odr) {
        OdrInfoDTO dto = new OdrInfoDTO();

        dto.setOrderDetailRoomId(odr.getOrderDetailRoomId());
        dto.setRoomFee(odr.getRoomFee());
        dto.setCheckIn(odr.getCheckInDate());
        dto.setCheckOut(odr.getCheckOutDate());
        dto.setBookingType(odr.getBookingType() == null ? null : odr.getBookingType().toString());

        if (odr.getOrder() != null) {
            dto.setOrderId(odr.getOrder().getOrderId());
            if (odr.getOrder().getCustomer() != null) {
                dto.setCustomerId(odr.getOrder().getCustomer().getCustomerId());
                dto.setFullName(odr.getOrder().getCustomer().getFullName());
                dto.setPhone(odr.getOrder().getCustomer().getPhone());
            }
        }
        return dto;
    }

    private void validateTimeRange(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Thiếu thời gian check-in/check-out.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out phải sau check-in.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}