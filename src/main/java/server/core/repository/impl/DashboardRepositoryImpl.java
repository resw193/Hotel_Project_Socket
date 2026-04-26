package server.core.repository.impl;

import common.dto.DashboardRoomTypeStatDTO;
import common.dto.DashboardTopServiceDTO;
import common.dto.DashboardUpcomingBookingDTO;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.DashboardRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardRepositoryImpl implements DashboardRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public DashboardRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public int totalRooms() {
        String query = """
                MATCH (r:Room)
                RETURN count(r) AS totalRooms
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);
                return result.single().get("totalRooms").asInt();
            });
        }
    }

    @Override
    public int occupiedNow() {
        String query = """
                MATCH (r:Room)
                WHERE coalesce(r.isAvailable, true) = false
                RETURN count(r) AS occupiedNow
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.single().get("occupiedNow").asInt();
            });
        }
    }

    @Override
    public double revenueOfMonth(YearMonth yearMonth) {
        String query = """
                MATCH (o:Order)
                WHERE o.orderStatus = 'Thanh toán'
                  AND o.orderDate >= $startDateTime
                  AND o.orderDate <= $endDateTime
                RETURN coalesce(sum(o.total), 0) AS revenue
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("startDateTime", yearMonth.atDay(1).atStartOfDay());
        params.put("endDateTime", yearMonth.atEndOfMonth().atTime(23, 59, 59));

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.single().get("revenue").asDouble();
            });
        }
    }

    @Override
    public int bookingsOn(LocalDate date) {
        String query = """
                MATCH (odr:OrderDetailRoom)
                WHERE date(odr.bookingDate) = $date
                RETURN count(odr) AS totalBookings
                """;

        Map<String, Object> params = Map.of("date", date);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.single().get("totalBookings").asInt();
            });
        }
    }

    @Override
    public int customersVisitOn(LocalDate date) {
        return customersVisitBetween(date, date);
    }

    @Override
    public int customersVisitBetween(LocalDate start, LocalDate end) {
        String query = """
                MATCH (o:Order)-[:OF_CUSTOMER]->(c:Customer)
                MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
                WHERE date(odr.checkInDate) >= $startDate
                  AND date(odr.checkInDate) <= $endDate
                RETURN count(DISTINCT c.customerId) AS totalCustomers
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", start);
        params.put("endDate", end);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.single().get("totalCustomers").asInt();
            });
        }
    }

    @Override
    public List<DashboardUpcomingBookingDTO> upcomingBookings(int daysAhead) {
        String query = """
                MATCH (o:Order)-[:OF_CUSTOMER]->(c:Customer)
                MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)
                WHERE date(odr.checkInDate) >= date()
                  AND date(odr.checkInDate) < date() + duration({days: $daysAhead})
                  AND o.orderStatus <> 'Thanh toán'
                RETURN o.orderId AS orderCode,
                       c.fullName AS customer,
                       r.roomId AS room,
                       date(odr.checkInDate) AS checkIn,
                       date(odr.checkOutDate) AS checkOut,
                       odr.status AS status
                ORDER BY odr.checkInDate ASC, r.roomId ASC
                """;

        Map<String, Object> params = Map.of("daysAhead", daysAhead);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            DashboardUpcomingBookingDTO dto = new DashboardUpcomingBookingDTO();
                            dto.setOrderCode(record.get("orderCode").isNull() ? null : record.get("orderCode").asString());
                            dto.setCustomer(record.get("customer").isNull() ? null : record.get("customer").asString());
                            dto.setRoom(record.get("room").isNull() ? null : record.get("room").asString());
                            dto.setCheckIn(record.get("checkIn").isNull() ? null : record.get("checkIn").asLocalDate());
                            dto.setCheckOut(record.get("checkOut").isNull() ? null : record.get("checkOut").asLocalDate());
                            dto.setStatus(record.get("status").isNull() ? null : record.get("status").asString());
                            return dto;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<DashboardTopServiceDTO> topServicesOfMonth(YearMonth yearMonth, int topN) {
        String query = """
                MATCH (o:Order)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)-[:FOR_SERVICE]->(s:Service)
                WHERE o.orderStatus = 'Thanh toán'
                  AND o.orderDate >= $startDateTime
                  AND o.orderDate <= $endDateTime
                RETURN s.serviceName AS serviceName,
                       coalesce(sum(ods.quantity), 0) AS totalQuantity
                ORDER BY totalQuantity DESC
                LIMIT $topN
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("startDateTime", yearMonth.atDay(1).atStartOfDay());
        params.put("endDateTime", yearMonth.atEndOfMonth().atTime(23, 59, 59));
        params.put("topN", topN);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("serviceName", record.get("serviceName").isNull() ? null : record.get("serviceName").asString());
                            data.put("totalQuantity", record.get("totalQuantity").asInt());

                            return mapper.toObject(data, DashboardTopServiceDTO.class);
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<DashboardRoomTypeStatDTO> roomTypeDistribution() {
        String query = """
                MATCH (r:Room)-[:HAS_TYPE]->(rt:RoomType)
                RETURN rt.typeName AS typeName,
                       count(r) AS roomCount
                ORDER BY roomCount DESC
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("typeName", record.get("typeName").isNull() ? null : record.get("typeName").asString());
                            data.put("roomCount", record.get("roomCount").asInt());

                            return mapper.toObject(data, DashboardRoomTypeStatDTO.class);
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public String getDashboardNote(String noteKey) {
        String query = """
                MATCH (n:DashboardNote {noteKey: $noteKey})
                RETURN coalesce(n.content, '') AS content
                """;

        Map<String, Object> params = Map.of("noteKey", noteKey);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);
                if (!result.hasNext()) return "";

                return result.single().get("content").asString();
            });
        }
    }

    @Override
    public boolean saveDashboardNote(String noteKey, String content, String employeeId) {
        String query = """
                MATCH (n:DashboardNote {noteKey: $noteKey})
                SET n.content = $content,
                    n.updatedBy = $employeeId,
                    n.updatedAt = localdatetime()
                RETURN n
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("noteKey", noteKey);
        params.put("content", content);
        params.put("employeeId", employeeId);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }
}