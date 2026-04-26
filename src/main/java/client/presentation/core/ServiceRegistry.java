package client.presentation.core;

import server.core.service.*;

public final class ServiceRegistry {
    private ServiceRegistry() {
    }

    private static EmployeeService employeeService;
    private static EmployeeTypeService employeeTypeService;
    private static CustomerService customerService;
    private static ServiceService serviceService;
    private static RoomService roomService;
    private static RoomTypeService roomTypeService;
    private static PromotionService promotionService;
    private static RoomStayService roomStayService;
    private static OrderService orderService;
    private static ServiceRankingService serviceRankingService;
    private static BookingTypeRevenueService bookingTypeRevenueService;
    private static OrderStatisticsService orderStatisticsService;
    private static DashboardService dashboardService;

    public static void init(EmployeeService employeeService, EmployeeTypeService employeeTypeService, CustomerService customerService,
                            ServiceService serviceService, RoomService roomService, RoomTypeService roomTypeService, PromotionService promotionService,
                            RoomStayService roomStayService, OrderService orderService,
                            ServiceRankingService serviceRankingService, BookingTypeRevenueService bookingTypeRevenueService,
                            OrderStatisticsService orderStatisticsService, DashboardService dashboardService
    ) {
        ServiceRegistry.employeeService = employeeService;
        ServiceRegistry.employeeTypeService = employeeTypeService;
        ServiceRegistry.customerService = customerService;
        ServiceRegistry.serviceService = serviceService;
        ServiceRegistry.roomService = roomService;
        ServiceRegistry.roomTypeService = roomTypeService;
        ServiceRegistry.promotionService = promotionService;
        ServiceRegistry.roomStayService = roomStayService;
        ServiceRegistry.orderService = orderService;
        ServiceRegistry.serviceRankingService = serviceRankingService;
        ServiceRegistry.bookingTypeRevenueService = bookingTypeRevenueService;
        ServiceRegistry.orderStatisticsService = orderStatisticsService;
        ServiceRegistry.dashboardService = dashboardService;
    }

    public static EmployeeService getEmployeeService() {
        return employeeService;
    }

    public static EmployeeTypeService getEmployeeTypeService() {
        return employeeTypeService;
    }

    public static CustomerService getCustomerService() {
        return customerService;
    }

    public static ServiceService getServiceService() {
        return serviceService;
    }

    public static RoomService getRoomService() {
        return roomService;
    }

    public static RoomTypeService getRoomTypeService() {
        return roomTypeService;
    }

    public static PromotionService getPromotionService() {
        return promotionService;
    }

    public static RoomStayService getRoomStayService() {
        return roomStayService;
    }

    public static OrderService getOrderService() {
        return orderService;
    }

    public static ServiceRankingService getServiceRankingService() {
        return serviceRankingService;
    }

    public static BookingTypeRevenueService getBookingTypeRevenueService() {
        return bookingTypeRevenueService;
    }

    public static OrderStatisticsService getOrderStatisticsService() {
        return orderStatisticsService;
    }

    public static DashboardService getDashboardService() {
        return dashboardService;
    }
}