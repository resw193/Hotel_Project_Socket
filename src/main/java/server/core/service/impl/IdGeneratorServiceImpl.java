package server.core.service.impl;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.service.IdGeneratorService;
import server.infrastructure.db.Neo4jConnManager;

import java.util.Map;

public class IdGeneratorServiceImpl implements IdGeneratorService {

    private final Neo4jConnManager connManager;

    public IdGeneratorServiceImpl(Neo4jConnManager connManager) {
        this.connManager = connManager;
    }

    @Override
    public String generateEmployeeId(String employeeTypeName, String fullName) {
        long seq = nextValue("EMPLOYEE_SEQ");

        String prefix = getEmployeePrefix(employeeTypeName);
        String initials = getTwoInitials(fullName);

        return prefix + initials + String.format("%02d", seq);
    }

    @Override
    public String generateAccountId() {
        long seq = nextValue("ACCOUNT_SEQ");
        return "Acc" + String.format("%03d", seq);
    }

    @Override
    public String generateCustomerId() {
        long seq = nextValue("CUSTOMER_SEQ");
        return "Cus" + String.format("%06d", seq);
    }

    @Override
    public String generatePromotionId() {
        long seq = nextValue("PROMOTION_SEQ");
        return "Promo" + String.format("%02d", seq);
    }

    @Override
    public String generateRoomTypeId() {
        long seq = nextValue("ROOMTYPE_SEQ");
        return "RT" + String.format("%04d", seq);
    }

    @Override
    public String generateRoomId() {
        long seq = nextValue("ROOM_SEQ");
        return "Room" + String.format("%02d", seq);
    }

    @Override
    public String generateServiceId() {
        long seq = nextValue("SERVICE_SEQ");
        return "Serv" + String.format("%02d", seq);
    }

    @Override
    public String generateOrderDetailRoomId() {
        long seq = nextValue("ODR_SEQ");
        return "ODR" + String.format("%06d", seq);
    }

    @Override
    public String generateOrderDetailServiceId() {
        long seq = nextValue("ODS_SEQ");
        return "OD" + String.format("%06d", seq);
    }

    @Override
    public String generateOrderId(String employeeTypeName, String employeeFullName) {
        long seq = nextValue("ORDER_SEQ");

        String prefix = getEmployeePrefix(employeeTypeName);
        String initials = getTwoInitials(employeeFullName);

        return prefix + initials + String.format("%04d", seq);
    }

    private long nextValue(String counterName) {
        String query = """
                MATCH (c:Counter {name: $counterName})
                SET c.value = c.value + 1
                RETURN c.value AS seq
                """;
        Map<String, Object> params = Map.of("counterName", counterName);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.single().get("seq").asLong();
            });
        }
    }

    private String getEmployeePrefix(String employeeTypeName) {
        if (employeeTypeName == null) {
            return "NV";
        }

        return switch (employeeTypeName.trim()) {
            case "Lễ tân" -> "LT";
            case "Quản lý" -> "QL";
            default -> "NV";
        };
    }

    private String getTwoInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "XX";
        }

        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1).toUpperCase();
        String last = parts[parts.length - 1].substring(0, 1).toUpperCase();

        return first + last;
    }
}