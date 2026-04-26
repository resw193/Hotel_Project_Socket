package server.core.service;

public interface IdGeneratorService {
    String generateEmployeeId(String employeeTypeName, String fullName);

    String generateAccountId();

    String generateCustomerId();

    String generatePromotionId();

    String generateRoomTypeId();

    String generateRoomId();

    String generateServiceId();

    String generateOrderDetailRoomId();

    String generateOrderDetailServiceId();

    String generateOrderId(String employeeTypeName, String employeeFullName);
}