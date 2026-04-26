package server.network.socket;

import common.dto.*;
import common.dto.request_dto.*;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import server.core.repository.*;
import server.core.repository.impl.*;
import server.core.service.*;
import server.core.service.impl.*;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;
import server.infrastructure.mapper.impl.JacksonDataMapper;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// Phân loại request theo CommandType --> Gọi đúng service cần thiết để xử lý yêu cầu --> Sau đó service gọi tiếp repository
public class RequestDispatcher {

    private final AccountService accountService;
    private final EmployeeService employeeService;
    private final EmployeeTypeService employeeTypeService;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final CustomerService customerService;
    private final RoomStayService roomStayService;
    private final ServiceService serviceService;
    private final OrderService orderService;
    private final PromotionService promotionService;
    private final ServiceRankingService serviceRankingService;
    private final BookingTypeRevenueService bookingTypeRevenueService;
    private final OrderStatisticsService orderStatisticsService;
    private final DashboardService dashboardService;

    public RequestDispatcher() {
        Neo4jConnManager connManager = new Neo4jConnManager();
        GenericDataMapper mapper = new JacksonDataMapper();
        IdGeneratorService idGeneratorService = new IdGeneratorServiceImpl(connManager);

        AccountRepository accountRepository = new AccountRepositoryImpl(connManager, mapper);
        EmployeeTypeRepository employeeTypeRepository = new EmployeeTypeRepositoryImpl(connManager, mapper);
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl(connManager, mapper);

        RoomTypeRepository roomTypeRepository = new RoomTypeRepositoryImpl(connManager, mapper);
        RoomRepository roomRepository = new RoomRepositoryImpl(connManager, mapper);
        CustomerRepository customerRepository = new CustomerRepositoryImpl(connManager, mapper);
        ServiceRepository serviceRepository = new ServiceRepositoryImpl(connManager, mapper);
        RoomStayRepository roomStayRepository = new RoomStayRepositoryImpl(connManager, mapper);
        OrderRepository orderRepository = new OrderRepositoryImpl(connManager, mapper);
        OrderDetailRoomRepository orderDetailRoomRepository = new OrderDetailRoomRepositoryImpl(connManager, mapper);
        OrderDetailServiceRepository orderDetailServiceRepository = new OrderDetailServiceRepositoryImpl(connManager, mapper);
        PromotionRepository promotionRepository = new PromotionRepositoryImpl(connManager, mapper);
        ServiceRankingRepository serviceRankingRepository = new ServiceRankingRepositoryImpl(connManager, mapper);
        BookingTypeRevenueRepository bookingTypeRevenueRepository = new BookingTypeRevenueRepositoryImpl(connManager, mapper);
        OrderStatisticsRepository orderStatisticsRepository = new OrderStatisticsRepositoryImpl(connManager, mapper);
        DashboardRepository dashboardRepository = new DashboardRepositoryImpl(connManager, mapper);

        this.dashboardService = new DashboardServiceImpl(dashboardRepository);
        this.orderStatisticsService = new OrderStatisticsServiceImpl(orderStatisticsRepository);
        this.bookingTypeRevenueService = new BookingTypeRevenueServiceImpl(bookingTypeRevenueRepository);
        this.serviceRankingService = new ServiceRankingServiceImpl(serviceRankingRepository);
        this.orderService = new OrderServiceImpl(orderRepository, orderDetailRoomRepository, orderDetailServiceRepository, mapper);
        this.promotionService = new PromotionServiceImpl(promotionRepository, idGeneratorService, mapper);
        this.customerService = new CustomerServiceImpl(customerRepository, idGeneratorService, mapper);
        this.roomStayService = new RoomStayServiceImpl(roomStayRepository, mapper);
        this.serviceService = new ServiceServiceImpl(serviceRepository, idGeneratorService, mapper);
        this.accountService = new AccountServiceImpl(accountRepository, mapper);
        this.employeeService = new EmployeeServiceImpl(employeeRepository, employeeTypeRepository, accountRepository, idGeneratorService, mapper);
        this.employeeTypeService = new EmployeeTypeServiceImpl(employeeTypeRepository, mapper);
        this.roomTypeService = new RoomTypeServiceImpl(roomTypeRepository, mapper);
        this.roomService = new RoomServiceImpl(roomRepository, roomTypeRepository, idGeneratorService, mapper);
    }

    public BaseResponse dispatch(BaseRequest request) {
        if (request == null) {
            return BaseResponse.error("Request rỗng.");
        }

        CommandType commandType = request.getCommandType();
        if (commandType == null) {
            return BaseResponse.error("CommandType không hợp lệ.");
        }

        try {
            return switch (commandType) {
                // Test
                case PING -> handlePing(request);

                // Login
                case LOGIN -> handleLogin(request);
                case FORGOT_PASSWORD -> BaseResponse.error("Chưa cài đặt chức năng FORGOT_PASSWORD.");
                case CHANGE_PASSWORD -> BaseResponse.error("Chưa cài đặt chức năng CHANGE_PASSWORD.");

                // Quản lý phòng
                case GET_ALL_ROOMS -> handleGetAllRooms();
                case GET_ROOM_BY_ID -> handleGetRoomById(request);
                case SEARCH_ROOMS -> handleSearchRooms(request);
                case ADD_ROOM -> handleAddRoom(request);
                case UPDATE_ROOM_INFORMATION -> handleUpdateRoomInformation(request);

                case GET_ALL_ROOM_TYPES -> handleGetAllRoomTypes();
                case GET_ROOM_TYPE_BY_ID -> handleGetRoomTypeById(request);
                case UPDATE_ROOM_TYPE_PRICING -> handleUpdateRoomTypePricing(request);

                // Quản lý đặt phòng
                case BOOK_ROOM -> handleBookRoom(request);
                case BOOK_MULTI_ROOMS -> handleBookMultiRooms(request);

                case GET_PENDING_BOOKING -> handleGetPendingBooking(request);
                case GET_PENDING_BOOKINGS_OF_ROOM -> handleGetPendingBookingsOfRoom(request);
                case GET_ACTIVE_CHECKIN_INFO -> handleGetActiveCheckInInfo(request);
                case GET_ACTIVE_STAY_TIMES -> handleGetActiveStayTimes(request);

                case CHECK_IN -> handleCheckIn(request);
                case CHECK_IN_BY_ODR_ID -> handleCheckInByOdrId(request);
                case CHECK_OUT -> handleCheckOut(request);

                case CANCEL_BOOKING -> handleCancelBooking(request);
                case CANCEL_BOOKING_BY_ODR_ID -> handleCancelBookingByOdrId(request);

                case EXTEND_ROOM -> handleExtendRoom(request);
                case CHANGE_ROOM_BEFORE_CHECKIN -> handleChangeRoomBeforeCheckIn(request);
                case CHANGE_ROOM_WHILE_CHECKIN -> handleChangeRoomWhileCheckIn(request);

                case ADD_SERVICE_TO_ROOM -> handleAddServiceToRoom(request);
                case GET_ROOM_CALENDAR -> handleGetRoomCalendar(request);

                case GET_RECOMMEND_ROOMS -> handleGetRecommendRooms(request);
                case CALCULATE_ROOM_FEE -> handleCalculateRoomFee(request);
                case CALCULATE_ROOM_FEE_WITH_NEW_ROOM -> handleCalculateRoomFeeWithNewRoom(request);

                // Quản lý khách hàng
                case GET_ALL_CUSTOMERS -> handleGetAllCustomers();
                case GET_CUSTOMER_BY_ID -> handleGetCustomerById(request);
                case SEARCH_CUSTOMERS -> handleSearchCustomers(request);
                case ADD_CUSTOMER -> handleAddCustomer(request);
                case UPDATE_CUSTOMER -> handleUpdateCustomer(request);
                case GET_CUSTOMER_BY_PHONE -> handleGetCustomerByPhone(request);

                // Quản lý nhân viên
                case GET_ALL_EMPLOYEES -> handleGetAllEmployees();
                case GET_EMPLOYEE_BY_ID -> handleGetEmployeeById(request);
                case GET_EMPLOYEES_BY_TYPE -> handleGetEmployeesByType(request);
                case ADD_EMPLOYEE -> handleAddEmployee(request);
                case UPDATE_EMPLOYEE -> handleUpdateEmployee(request);
                case DELETE_EMPLOYEE -> handleDeleteEmployee(request);
                case GET_ALL_EMPLOYEE_TYPES -> handleGetAllEmployeeTypes();

                // Quản lý dịch vụ
                case GET_ALL_SERVICES -> handleGetAllServices();
                case GET_SERVICES_BY_TYPE -> handleGetServicesByType(request);
                case GET_SERVICE_BY_ID -> handleGetServiceById(request);
                case ADD_SERVICE -> handleAddService(request);
                case UPDATE_SERVICE -> handleUpdateService(request);
                case INCREASE_SERVICE_QUANTITY -> handleIncreaseServiceQuantity(request);
                case DELETE_SERVICE -> handleDeleteService(request);

                // Quản lý khuyến mãi
                case GET_ALL_PROMOTIONS -> handleGetAllPromotions();
                case GET_PROMOTION_BY_ID -> handleGetPromotionById(request);
                case ADD_PROMOTION -> handleAddPromotion(request);
                case UPDATE_PROMOTION -> handleUpdatePromotion(request);
                case DELETE_PROMOTION -> handleDeletePromotion(request);

                // Quản lý hóa đơn
                case GET_ALL_ORDERS -> handleGetAllOrders();
                case GET_ORDERS_BY_STATUS -> handleGetOrdersByStatus(request);
                case GET_ORDER_BY_ID -> handleGetOrderById(request);
                case GET_ORDER_ROOM_LINES -> handleGetOrderRoomLines(request);
                case GET_ORDER_SERVICE_LINES -> handleGetOrderServiceLines(request);
                case SEARCH_ORDERS -> handleSearchOrders(request);
                case UPDATE_ORDER_PROMOTION -> handleUpdateOrderPromotion(request);
                case PAY_ORDER -> handlePayOrder(request);

                // Thống kê
                case GET_SERVICE_RANKING -> handleGetServiceRanking(request);
                case GET_BOOKING_TYPE_REVENUE -> handleGetBookingTypeRevenue(request);
                case GET_ORDER_STATISTICS -> handleGetOrderStatistics(request);

                // Dashboard
                case GET_DASHBOARD_DATA -> BaseResponse.error("Chưa cài đặt chức năng GET_DASHBOARD_DATA.");
                case GET_DASHBOARD_OVERVIEW -> handleGetDashboardOverview(request);
                case SAVE_DASHBOARD_PLAN_NOTE -> handleSaveDashboardPlanNote(request);
                case SAVE_DASHBOARD_ALERT_NOTE -> handleSaveDashboardAlertNote(request);

                // Do nothing
                case UNKNOWN -> BaseResponse.error("Command UNKNOWN không được hỗ trợ.");
            };
        } catch (Exception e) {
            return BaseResponse.error("Lỗi xử lý request: " + e.getMessage());
        }
    }

    private BaseResponse handlePing(BaseRequest request) {
        return BaseResponse.success("PONG", "Kết nối client-server thành công.");
    }

    private BaseResponse handleLogin(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof LoginRequestDTO loginRequestDTO)) {
            return BaseResponse.error("Dữ liệu LOGIN không hợp lệ.");
        }

        String username = loginRequestDTO.getUsername() == null ? "" : loginRequestDTO.getUsername().trim();
        String password = loginRequestDTO.getPassword() == null ? "" : loginRequestDTO.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) {
            return BaseResponse.error("Username và password không được trống.");
        }

        boolean ok = accountService.authentication(username, password);
        if (!ok) {
            return BaseResponse.error("Sai tên đăng nhập hoặc mật khẩu.");
        }

        AccountDTO account = accountService.getAccountByUsername(username);
        if (account == null) {
            return BaseResponse.error("Đăng nhập thành công nhưng không lấy được tài khoản.");
        }

        EmployeeDTO employee = employeeService.getByUsername(username);
        if (employee == null) {
            employee = new EmployeeDTO();
            employee.setEmployeeId(account.getEmployeeId());
            employee.setFullName(account.getEmployeeName());
            employee.setEmployeeTypeId(null);
            employee.setEmployeeTypeName("");
            employee.setImgSource(null);
            employee.setGender(false);
        }

        LoginResultDTO result = new LoginResultDTO();
        result.setUsername(username);
        result.setEmployeeId(employee.getEmployeeId());
        result.setEmployeeName(employee.getFullName());
        result.setEmployeeTypeId(employee.getEmployeeTypeId());
        result.setEmployeeTypeName(employee.getEmployeeTypeName());
        result.setImgSource(employee.getImgSource());
        result.setGender(employee.isGender());

        return BaseResponse.success(result, "Đăng nhập thành công.");
    }

    private BaseResponse handleGetAllRooms() {
        List<RoomDTO> rooms = roomService.getAll();
        return BaseResponse.success(rooms, "Lấy danh sách phòng thành công.");
    }

    private BaseResponse handleGetRoomById(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String roomId)) {
            return BaseResponse.error("Dữ liệu GET_ROOM_BY_ID không hợp lệ.");
        }

        RoomDTO room = roomService.getRoomByID(roomId);
        return BaseResponse.success(room, "Lấy thông tin phòng thành công.");
    }

    private BaseResponse handleSearchRooms(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomSearchRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu SEARCH_ROOMS không hợp lệ.");
        }

        List<RoomDTO> rooms = roomService.searchAndFilter(
                dto.getKeyword() == null ? "" : dto.getKeyword().trim(),
                dto.getFilter() == null ? "All" : dto.getFilter().trim()
        );
        return BaseResponse.success(rooms, "Lọc danh sách phòng thành công.");
    }

    private BaseResponse handleAddRoom(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomDTO roomDTO)) {
            return BaseResponse.error("Dữ liệu ADD_ROOM không hợp lệ.");
        }

        boolean ok = roomService.addRoom(roomDTO);
        return ok
                ? BaseResponse.success(null, "Thêm phòng thành công.")
                : BaseResponse.error("Không thể thêm phòng.");
    }

    private BaseResponse handleUpdateRoomInformation(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomDTO roomDTO)) {
            return BaseResponse.error("Dữ liệu UPDATE_ROOM_INFORMATION không hợp lệ.");
        }

        boolean ok = roomService.updateRoomInformation(roomDTO);
        return ok
                ? BaseResponse.success(null, "Cập nhật thông tin phòng thành công.")
                : BaseResponse.error("Không thể cập nhật thông tin phòng.");
    }

    private BaseResponse handleGetAllRoomTypes() {
        List<RoomTypeDTO> roomTypes = roomTypeService.getAll();
        return BaseResponse.success(roomTypes, "Lấy danh sách loại phòng thành công.");
    }

    private BaseResponse handleGetRoomTypeById(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String roomTypeId)) {
            return BaseResponse.error("Dữ liệu GET_ROOM_TYPE_BY_ID không hợp lệ.");
        }

        RoomTypeDTO roomTypeDTO = roomTypeService.getByID(roomTypeId);
        if (roomTypeDTO == null) {
            return BaseResponse.error("Không tìm thấy loại phòng.");
        }

        return BaseResponse.success(roomTypeDTO, "Lấy loại phòng thành công.");
    }

    private BaseResponse handleUpdateRoomTypePricing(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomTypeDTO roomTypeDTO)) {
            return BaseResponse.error("Dữ liệu UPDATE_ROOM_TYPE_PRICING không hợp lệ.");
        }

        boolean ok = roomTypeService.updateRoomTypePricing(roomTypeDTO);
        return ok
                ? BaseResponse.success(null, "Cập nhật giá loại phòng thành công.")
                : BaseResponse.error("Không thể cập nhật giá loại phòng.");
    }

    private BaseResponse handleBookRoom(BaseRequest request) {
        System.out.println(">>> HANDLE BOOK_ROOM CALLED");

        Object data = request.getData();
        if (!(data instanceof BookRoomRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu BOOK_ROOM không hợp lệ.");
        }

        System.out.println("roomID = " + dto.getRoomID());
        System.out.println("employeeID = " + dto.getEmployeeID());
        System.out.println("customer phone = " + (dto.getCustomerDTO() == null ? null : dto.getCustomerDTO().getPhone()));

        boolean ok = roomStayService.datPhong(
                dto.getCustomerDTO(),
                dto.getRoomID(),
                dto.getEmployeeID(),
                dto.getBookingDate(),
                dto.getCheckInDate(),
                dto.getCheckOutDate(),
                dto.getBookingType()
        );

        System.out.println("BOOK_ROOM RESULT = " + ok);

        return ok
                ? BaseResponse.success(null, "Đặt phòng thành công.")
                : BaseResponse.error("Không thể đặt phòng.");
    }

    private BaseResponse handleBookMultiRooms(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof List<?> rawList)) {
            return BaseResponse.error("Dữ liệu BOOK_MULTI_ROOMS không hợp lệ.");
        }

        int okCount = 0;
        List<String> failed = new ArrayList<>();

        for (Object item : rawList) {
            if (!(item instanceof BookRoomRequestDTO dto)) {
                failed.add("1 booking không hợp lệ");
                continue;
            }

            boolean ok = roomStayService.datPhong(
                    dto.getCustomerDTO(),
                    dto.getRoomID(),
                    dto.getEmployeeID(),
                    dto.getBookingDate(),
                    dto.getCheckInDate(),
                    dto.getCheckOutDate(),
                    dto.getBookingType()
            );

            if (ok) okCount++;
            else failed.add(dto.getRoomID());
        }

        if (okCount == rawList.size()) {
            return BaseResponse.success(null, "Đặt nhiều phòng thành công.");
        }

        return BaseResponse.success(
                failed,
                "Đặt thành công " + okCount + "/" + rawList.size() +
                        (failed.isEmpty() ? "" : ". Thất bại: " + String.join(", ", failed))
        );
    }

    private BaseResponse handleGetPendingBooking(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_PENDING_BOOKING không hợp lệ.");
        }

        OdrInfoDTO odr = roomStayService.getPendingBooking(dto.getRoomID());
        return BaseResponse.success(odr, "Lấy booking chờ thành công.");
    }

    private BaseResponse handleGetPendingBookingsOfRoom(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_PENDING_BOOKINGS_OF_ROOM không hợp lệ.");
        }

        List<OdrInfoDTO> list = roomStayService.getPendingBookingsOfRoom(dto.getRoomID());
        return BaseResponse.success(list, "Lấy danh sách booking chờ thành công.");
    }

    private BaseResponse handleGetActiveCheckInInfo(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ACTIVE_CHECKIN_INFO không hợp lệ.");
        }

        OdrInfoDTO odr = roomStayService.getActiveCheckInInfo(dto.getRoomID());
        return BaseResponse.success(odr, "Lấy thông tin check-in hiện tại thành công.");
    }

    private BaseResponse handleCheckIn(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CHECK_IN không hợp lệ.");
        }

        boolean ok = roomStayService.checkIn(dto.getRoomID());
        return ok
                ? BaseResponse.success(null, "Check-in thành công.")
                : BaseResponse.error("Không thể check-in.");
    }

    private BaseResponse handleCheckInByOdrId(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OdrIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CHECK_IN_BY_ODR_ID không hợp lệ.");
        }

        boolean ok = roomStayService.checkInByOdrId(dto.getOrderDetailRoomId());
        return ok
                ? BaseResponse.success(null, "Check-in thành công.")
                : BaseResponse.error("Không thể check-in.");
    }

    private BaseResponse handleCheckOut(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CHECK_OUT không hợp lệ.");
        }

        boolean ok = roomStayService.checkOut(dto.getRoomID());
        return ok
                ? BaseResponse.success(null, "Check-out thành công.")
                : BaseResponse.error("Không thể check-out.");
    }

    private BaseResponse handleCancelBooking(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CANCEL_BOOKING không hợp lệ.");
        }

        boolean ok = roomStayService.huyDatPhong(dto.getRoomID());
        return ok
                ? BaseResponse.success(null, "Hủy đặt phòng thành công.")
                : BaseResponse.error("Không thể hủy đặt phòng.");
    }

    private BaseResponse handleCancelBookingByOdrId(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OdrIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CANCEL_BOOKING_BY_ODR_ID không hợp lệ.");
        }

        boolean ok = roomStayService.huyDatPhongByOdrId(dto.getOrderDetailRoomId());
        return ok
                ? BaseResponse.success(null, "Hủy booking thành công.")
                : BaseResponse.error("Không thể hủy booking.");
    }

    private BaseResponse handleExtendRoom(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ExtendRoomRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu EXTEND_ROOM không hợp lệ.");
        }

        boolean ok = roomStayService.giaHanPhong(dto.getRoomID(), dto.getNewCheckOutDate());
        return ok
                ? BaseResponse.success(null, "Gia hạn phòng thành công.")
                : BaseResponse.error("Không thể gia hạn phòng.");
    }

    private BaseResponse handleGetRoomCalendar(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomCalendarRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ROOM_CALENDAR không hợp lệ.");
        }

        List<RoomCalendarSlotDTO> list = roomStayService.getCalendar(dto.getFromDate(), dto.getToDate());
        return BaseResponse.success(list, "Lấy lịch phòng thành công.");
    }

    private BaseResponse handleAddServiceToRoom(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof AddServiceToRoomRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu ADD_SERVICE_TO_ROOM không hợp lệ.");
        }

        boolean ok = roomStayService.addServiceToRoom(dto.getRoomID(), dto.getServiceName(), dto.getQuantity());
        return ok
                ? BaseResponse.success(null, "Thêm dịch vụ vào phòng thành công.")
                : BaseResponse.error("Không thể thêm dịch vụ vào phòng.");
    }

    private BaseResponse handleGetActiveStayTimes(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RoomIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ACTIVE_STAY_TIMES không hợp lệ.");
        }

        LocalDateTime[] times = roomStayService.getActiveStayTimes(dto.getRoomID());
        return BaseResponse.success(times, "Lấy thời gian lưu trú hiện tại thành công.");
    }

    private BaseResponse handleGetRecommendRooms(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof RecommendRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_RECOMMEND_ROOMS không hợp lệ.");
        }

        List<RecommendOptionDTO> options = roomService.recommendRooms(dto);
        return BaseResponse.success(options, "Lấy gợi ý phòng thành công.");
    }

    private BaseResponse handleCalculateRoomFee(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof CalculateRoomFeeRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CALCULATE_ROOM_FEE không hợp lệ.");
        }

        Double fee = roomStayService.calculateFeeByRoom(
                dto.getRoomID(),
                dto.getBookingType(),
                dto.getFrom(),
                dto.getTo()
        );
        return BaseResponse.success(fee, "Tính tiền phòng thành công.");
    }

    private BaseResponse handleCalculateRoomFeeWithNewRoom(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof CalculateRoomFeeWithNewRoomRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CALCULATE_ROOM_FEE_WITH_NEW_ROOM không hợp lệ.");
        }

        Double fee = roomStayService.calculateRoomFeeAgainWithNewRoom(
                dto.getNewRoomID(),
                dto.getBookingType(),
                dto.getCheckIn(),
                dto.getCheckOut()
        );
        return BaseResponse.success(fee, "Tính lại tiền phòng thành công.");
    }

    private BaseResponse handleChangeRoomBeforeCheckIn(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ChangeRoomBeforeCheckInRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CHANGE_ROOM_BEFORE_CHECKIN không hợp lệ.");
        }

        boolean ok = roomStayService.changeRoomBeforeCheckIn(
                dto.getOldRoomID(),
                dto.getNewRoomID(),
                dto.getNewCheckIn(),
                dto.getNewCheckOut()
        );

        return ok
                ? BaseResponse.success(null, "Đổi phòng trước check-in thành công.")
                : BaseResponse.error("Không thể đổi phòng trước check-in.");
    }

    private BaseResponse handleChangeRoomWhileCheckIn(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ChangeRoomWhileCheckInRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu CHANGE_ROOM_WHILE_CHECKIN không hợp lệ.");
        }

        boolean ok = roomStayService.changeRoomWhileCheckIn(
                dto.getOldRoomID(),
                dto.getNewRoomID(),
                dto.getChangeTime()
        );

        return ok
                ? BaseResponse.success(null, "Đổi phòng khi đang check-in thành công.")
                : BaseResponse.error("Không thể đổi phòng khi đang check-in.");
    }

    // Dịch vụ
    private BaseResponse handleGetAllServices() {
        return BaseResponse.success(serviceService.getAll(), "Lấy danh sách dịch vụ thành công.");
    }

    private BaseResponse handleGetServicesByType(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ServiceTypeRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_SERVICES_BY_TYPE không hợp lệ.");
        }

        return BaseResponse.success(serviceService.getByType(dto.getServiceType()), "Lấy danh sách dịch vụ theo loại thành công.");
    }

    private BaseResponse handleGetServiceById(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ServiceIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_SERVICE_BY_ID không hợp lệ.");
        }

        return BaseResponse.success(serviceService.getByID(dto.getServiceId()), "Lấy thông tin dịch vụ thành công.");
    }

    private BaseResponse handleAddService(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ServiceDTO dto)) {
            return BaseResponse.error("Dữ liệu ADD_SERVICE không hợp lệ.");
        }

        boolean ok = serviceService.add(dto);
        return ok
                ? BaseResponse.success(null, "Thêm dịch vụ thành công.")
                : BaseResponse.error("Không thể thêm dịch vụ.");
    }

    private BaseResponse handleUpdateService(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ServiceDTO dto)) {
            return BaseResponse.error("Dữ liệu UPDATE_SERVICE không hợp lệ.");
        }

        boolean ok = serviceService.updateInfo(dto);
        return ok
                ? BaseResponse.success(null, "Cập nhật dịch vụ thành công.")
                : BaseResponse.error("Không thể cập nhật dịch vụ.");
    }

    private BaseResponse handleIncreaseServiceQuantity(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof IncreaseServiceQuantityRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu INCREASE_SERVICE_QUANTITY không hợp lệ.");
        }

        boolean ok = serviceService.increaseQuantity(dto.getServiceId(), dto.getAddQuantity());
        return ok
                ? BaseResponse.success(null, "Cập nhật số lượng dịch vụ thành công.")
                : BaseResponse.error("Không thể cập nhật số lượng dịch vụ.");
    }

    private BaseResponse handleDeleteService(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ServiceIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu DELETE_SERVICE không hợp lệ.");
        }

        boolean ok = serviceService.delete(dto.getServiceId());
        return ok
                ? BaseResponse.success(null, "Xóa dịch vụ thành công.")
                : BaseResponse.error("Không thể xóa dịch vụ.");
    }

    private BaseResponse handleGetCustomerByPhone(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String phone)) {
            return BaseResponse.error("Dữ liệu GET_CUSTOMER_BY_PHONE không hợp lệ.");
        }

        CustomerDTO customer = customerService.getByPhone(phone);
        return BaseResponse.success(customer, "Lấy khách hàng theo số điện thoại thành công.");
    }

    private BaseResponse handleGetAllOrders() {
        return BaseResponse.success(orderService.getAllOrders(), "Lấy danh sách hóa đơn thành công.");
    }

    private BaseResponse handleGetOrdersByStatus(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OrderStatusRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ORDERS_BY_STATUS không hợp lệ.");
        }

        return BaseResponse.success(
                orderService.getOrdersByStatus(dto.getStatus()),
                "Lấy danh sách hóa đơn theo trạng thái thành công."
        );
    }

    private BaseResponse handleGetOrderById(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OrderIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ORDER_BY_ID không hợp lệ.");
        }

        OrderDTO order = orderService.getByID(dto.getOrderId());
        return BaseResponse.success(order, "Lấy hóa đơn thành công.");
    }

    private BaseResponse handleGetOrderRoomLines(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OrderIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ORDER_ROOM_LINES không hợp lệ.");
        }

        return BaseResponse.success(
                orderService.getRoomLines(dto.getOrderId()),
                "Lấy chi tiết phòng của hóa đơn thành công."
        );
    }

    private BaseResponse handleGetOrderServiceLines(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OrderIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ORDER_SERVICE_LINES không hợp lệ.");
        }

        return BaseResponse.success(
                orderService.getServiceLines(dto.getOrderId()),
                "Lấy chi tiết dịch vụ của hóa đơn thành công."
        );
    }

    private BaseResponse handleUpdateOrderPromotion(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof UpdateOrderPromotionRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu UPDATE_ORDER_PROMOTION không hợp lệ.");
        }

        boolean ok = orderService.updateOrderPromotion(dto.getOrderId(), dto.getPromotionDTO());
        return ok
                ? BaseResponse.success(null, "Cập nhật khuyến mãi hóa đơn thành công.")
                : BaseResponse.error("Không thể cập nhật khuyến mãi hóa đơn.");
    }

    private BaseResponse handlePayOrder(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OrderIdRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu PAY_ORDER không hợp lệ.");
        }

        List<OrderPayDTO> rows = orderService.payOrder(dto.getOrderId());
        return BaseResponse.success(rows, "Thanh toán hóa đơn thành công.");
    }

    // Khuyến mãi
    private BaseResponse handleGetAllPromotions() {
        return BaseResponse.success(
                promotionService.getAllPromotions(),
                "Lấy danh sách khuyến mãi thành công."
        );
    }

    private BaseResponse handleGetPromotionById(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String promotionId)) {
            return BaseResponse.error("Dữ liệu GET_PROMOTION_BY_ID không hợp lệ.");
        }

        PromotionDTO promotion = promotionService.getPromotionByID(promotionId);
        return BaseResponse.success(promotion, "Lấy khuyến mãi thành công.");
    }


    private BaseResponse handleAddPromotion(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof PromotionDTO promotionDTO)) {
            return BaseResponse.error("Dữ liệu ADD_PROMOTION không hợp lệ.");
        }

        boolean ok = promotionService.addPromotion(promotionDTO);
        return ok
                ? BaseResponse.success(null, "Thêm khuyến mãi thành công.")
                : BaseResponse.error("Không thể thêm khuyến mãi.");
    }

    private BaseResponse handleUpdatePromotion(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof PromotionDTO promotionDTO)) {
            return BaseResponse.error("Dữ liệu UPDATE_PROMOTION không hợp lệ.");
        }

        boolean ok = promotionService.updatePromotion(promotionDTO);
        return ok
                ? BaseResponse.success(null, "Cập nhật khuyến mãi thành công.")
                : BaseResponse.error("Không thể cập nhật khuyến mãi.");
    }

    private BaseResponse handleDeletePromotion(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String promotionId)) {
            return BaseResponse.error("Dữ liệu DELETE_PROMOTION không hợp lệ.");
        }

        boolean ok = promotionService.deletePromotion(promotionId);
        return ok
                ? BaseResponse.success(null, "Xóa khuyến mãi thành công.")
                : BaseResponse.error("Không thể xóa khuyến mãi.");
    }


    private BaseResponse handleSearchOrders(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof SearchOrdersRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu SEARCH_ORDERS không hợp lệ.");
        }

        String status = dto.getStatus() == null ? "" : dto.getStatus().trim();
        String keyword = dto.getKeyword() == null ? "" : dto.getKeyword().trim();

        List<OrderDTO> orders;
        if (status.isEmpty() || "Tất cả".equalsIgnoreCase(status)) {
            orders = orderService.getAllOrders();
        }
        else {
            orders = orderService.getOrdersByStatus(status);
        }

        if (keyword.isEmpty()) {
            return BaseResponse.success(orders, "Tìm kiếm hóa đơn thành công.");
        }

        String kw = normalize(keyword);

        List<OrderDTO> filtered = orders.stream()
                .filter(order -> {
                    String orderId = normalize(order.getOrderId());
                    String customerId = normalize(order.getCustomerId());
                    String customerName = normalize(order.getCustomerName());

                    String stack = orderId + " " + customerId + " " + customerName;
                    return stack.contains(kw);
                })
                .toList();

        return BaseResponse.success(filtered, "Tìm kiếm hóa đơn thành công.");
    }


    private BaseResponse handleGetServiceRanking(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof ServiceRankingRangeRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_SERVICE_RANKING không hợp lệ.");
        }

        if (dto.getTopN() != null && dto.getTopN() > 0) {
            return BaseResponse.success(
                    serviceRankingService.getTopByRange(dto.getStart(), dto.getEnd(), dto.getTopN()),
                    "Lấy top dịch vụ thành công."
            );
        }

        return BaseResponse.success(
                serviceRankingService.getByRange(dto.getStart(), dto.getEnd()),
                "Lấy thống kê dịch vụ thành công."
        );
    }

    private BaseResponse handleGetBookingTypeRevenue(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof BookingTypeRevenueRangeRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_BOOKING_TYPE_REVENUE không hợp lệ.");
        }

        return BaseResponse.success(bookingTypeRevenueService.stats(dto.getStart(), dto.getEnd()), "Lấy thống kê kiểu đặt phòng thành công.");
    }

    private BaseResponse handleGetOrderStatistics(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof OrderStatisticsRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_ORDER_STATISTICS không hợp lệ.");
        }

        if (dto.getDate() == null) {
            return BaseResponse.error("Ngày thống kê không được rỗng.");
        }

        DailyDetailDTO detail = dto.isByMonth()
                ? orderStatisticsService.getMonthlyDetail(YearMonth.from(dto.getDate()))
                : orderStatisticsService.getDailyDetail(dto.getDate());

        return BaseResponse.success(detail, "Lấy thống kê hóa đơn thành công.");
    }

    private BaseResponse handleGetAllCustomers() {
        return BaseResponse.success(customerService.getAll(), "Lấy danh sách khách hàng thành công.");
    }

    private BaseResponse handleGetCustomerById(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String customerId)) {
            return BaseResponse.error("Dữ liệu GET_CUSTOMER_BY_ID không hợp lệ.");
        }

        CustomerDTO customer = customerService.getById(customerId);
        return BaseResponse.success(customer, "Lấy khách hàng theo mã thành công.");
    }

    private BaseResponse handleSearchCustomers(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof SearchCustomersRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu SEARCH_CUSTOMERS không hợp lệ.");
        }

        return BaseResponse.success(customerService.filterAndSearch(dto.getKeyword(), dto.getMinLoyalty()), "Tìm kiếm khách hàng thành công.");
    }

    private BaseResponse handleAddCustomer(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof CustomerDTO customerDTO)) {
            return BaseResponse.error("Dữ liệu ADD_CUSTOMER không hợp lệ.");
        }

        boolean ok = customerService.addCustomer(customerDTO);
        return ok
                ? BaseResponse.success(null, "Thêm khách hàng thành công.")
                : BaseResponse.error("Không thể thêm khách hàng.");
    }

    private BaseResponse handleUpdateCustomer(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof CustomerDTO customerDTO)) {
            return BaseResponse.error("Dữ liệu UPDATE_CUSTOMER không hợp lệ.");
        }

        boolean ok = customerService.updateCustomer(customerDTO);
        return ok
                ? BaseResponse.success(null, "Cập nhật khách hàng thành công.")
                : BaseResponse.error("Không thể cập nhật khách hàng.");
    }

    private BaseResponse handleGetAllEmployees() {
        return BaseResponse.success(
                employeeService.getAll(),
                "Lấy danh sách nhân viên thành công."
        );
    }

    private BaseResponse handleGetEmployeeById(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String employeeId)) {
            return BaseResponse.error("Dữ liệu GET_EMPLOYEE_BY_ID không hợp lệ.");
        }

        EmployeeDTO employee = employeeService.getByID(employeeId);
        return BaseResponse.success(employee, "Lấy thông tin nhân viên thành công.");
    }

    private BaseResponse handleGetEmployeesByType(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof EmployeeTypeFilterRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_EMPLOYEES_BY_TYPE không hợp lệ.");
        }

        return BaseResponse.success(
                employeeService.getByTypeName(dto.getTypeName()),
                "Lọc danh sách nhân viên thành công."
        );
    }

    private BaseResponse handleAddEmployee(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof EmployeeDTO employeeDTO)) {
            return BaseResponse.error("Dữ liệu ADD_EMPLOYEE không hợp lệ.");
        }

        boolean ok = employeeService.addEmployee(employeeDTO);
        return ok
                ? BaseResponse.success(null, "Thêm nhân viên thành công.")
                : BaseResponse.error("Không thể thêm nhân viên.");
    }

    private BaseResponse handleUpdateEmployee(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof EmployeeDTO employeeDTO)) {
            return BaseResponse.error("Dữ liệu UPDATE_EMPLOYEE không hợp lệ.");
        }

        boolean ok = employeeService.updateProfile(employeeDTO);
        return ok
                ? BaseResponse.success(null, "Cập nhật nhân viên thành công.")
                : BaseResponse.error("Không thể cập nhật nhân viên.");
    }

    private BaseResponse handleDeleteEmployee(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof String employeeId)) {
            return BaseResponse.error("Dữ liệu DELETE_EMPLOYEE không hợp lệ.");
        }

        boolean ok = employeeService.deleteEmployee(employeeId);
        return ok
                ? BaseResponse.success(null, "Xóa nhân viên thành công.")
                : BaseResponse.error("Không thể xóa nhân viên.");
    }

    private BaseResponse handleGetAllEmployeeTypes() {
        try {
            return BaseResponse.success(employeeTypeService.getAll(), "Lấy danh sách loại nhân viên thành công.");
        } catch (Exception e) {
            return BaseResponse.error("Không thể lấy danh sách loại nhân viên: " + e.getMessage());
        }
    }

    private BaseResponse handleGetDashboardOverview(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof DashboardOverviewRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu GET_DASHBOARD_OVERVIEW không hợp lệ.");
        }

        DashboardOverviewDTO overview = dashboardService.getDashboardOverview(
                dto.getToday(),
                dto.getDaysAhead(),
                dto.getTopN()
        );

        return BaseResponse.success(overview, "Lấy dashboard overview thành công.");
    }

    private BaseResponse handleSaveDashboardPlanNote(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof DashboardSaveNoteRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu SAVE_DASHBOARD_PLAN_NOTE không hợp lệ.");
        }

        boolean ok = dashboardService.saveManagerPlanText(dto.getContent(), dto.getEmployeeId());
        return ok
                ? BaseResponse.success(null, "Lưu kế hoạch dashboard thành công.")
                : BaseResponse.error("Không thể lưu kế hoạch dashboard.");
    }

    private BaseResponse handleSaveDashboardAlertNote(BaseRequest request) {
        Object data = request.getData();
        if (!(data instanceof DashboardSaveNoteRequestDTO dto)) {
            return BaseResponse.error("Dữ liệu SAVE_DASHBOARD_ALERT_NOTE không hợp lệ.");
        }

        boolean ok = dashboardService.saveManagerAlertText(dto.getContent(), dto.getEmployeeId());
        return ok
                ? BaseResponse.success(null, "Lưu cảnh báo dashboard thành công.")
                : BaseResponse.error("Không thể lưu cảnh báo dashboard.");
    }


    private String normalize(String s) {
        if (s == null) return "";
        String tmp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        tmp = tmp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return tmp.toLowerCase().trim();
    }
}