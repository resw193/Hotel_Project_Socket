package server.core.repository.impl;

import common.dto.OdrInfoDTO;
import common.dto.RoomCalendarSlotDTO;
import common.entity.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.summary.SummaryCounters;
import server.core.repository.RoomStayRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomStayRepositoryImpl implements RoomStayRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public RoomStayRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public boolean datPhong(Customer customer, String roomID, String employeeID, LocalDateTime bookingDate,
                            LocalDateTime checkInDate, LocalDateTime checkOutDate, String bookingType) {

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                String roomQuery = """
                    MATCH (r:Room)-[:HAS_TYPE]->(rt:RoomType)
                    WHERE r.roomId = $roomID
                    RETURN r, rt
                """;
                Result roomResult = tx.run(roomQuery, Map.of("roomID", roomID));

                if (!roomResult.hasNext()) {
                    return false;
                }

                Record roomRecord = roomResult.next();
                RoomType roomType = mapper.toObject(roomRecord.get("rt").asNode().asMap(), RoomType.class);

                String overlapQuery = """
                MATCH (r:Room {roomId: $roomID})<-[:FOR_ROOM]-(odr:OrderDetailRoom)
                WHERE odr.status IN ['Đặt', 'Check-in']
                  AND odr.checkInDate < $newCheckOut
                  AND odr.checkOutDate > $newCheckIn
                RETURN odr
                LIMIT 1
                """;
                Map<String, Object> overlapParams = new HashMap<>();
                overlapParams.put("roomID", roomID);
                overlapParams.put("newCheckIn", checkInDate);
                overlapParams.put("newCheckOut", checkOutDate);

                Result overlapResult = tx.run(overlapQuery, overlapParams);
                if (overlapResult.hasNext()) {
                    return false;
                }

                String customerId = findOrCreateCustomer(tx, customer, bookingDate);
                if (customerId == null) return false;

                String orderId = findOrCreatePendingOrder(tx, customerId, employeeID);
                if (orderId == null) return false;

                double roomFee = calculateRoomFee(roomType, bookingType, checkInDate, checkOutDate);
                String odrId = "ODR" + String.format("%06d", nextValue(tx, "ODR_SEQ"));

                String createOdrQuery = """
                MATCH (o:Order {orderId: $orderId})
                MATCH (r:Room {roomId: $roomID})
                CREATE (odr:OrderDetailRoom {
                    orderDetailRoomId: $odrId,
                    roomFee: $roomFee,
                    bookingDate: $bookingDate,
                    checkInDate: $checkInDate,
                    checkOutDate: $checkOutDate,
                    bookingType: $bookingType,
                    status: 'Đặt'
                })
                MERGE (o)-[:HAS_ROOM_DETAIL]->(odr)
                MERGE (odr)-[:FOR_ROOM]->(r)
                RETURN odr
                """;

                Map<String, Object> createOdrParams = new HashMap<>();
                createOdrParams.put("orderId", orderId);
                createOdrParams.put("roomID", roomID);
                createOdrParams.put("odrId", odrId);
                createOdrParams.put("roomFee", roomFee);
                createOdrParams.put("bookingDate", bookingDate);
                createOdrParams.put("checkInDate", checkInDate);
                createOdrParams.put("checkOutDate", checkOutDate);
                createOdrParams.put("bookingType", bookingType);

                Result createOdrResult = tx.run(createOdrQuery, createOdrParams);
                SummaryCounters counters = createOdrResult.consume().counters();

                if (counters.nodesCreated() <= 0) {
                    return false;
                }

                recalcOrderPromotionByLoyalty(tx, orderId);
                return true;
            });
        }
    }

    @Override
    public boolean huyDatPhong(String roomID) {
        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                OrderDetailRoom pending = getPendingBookingTx(tx, roomID);

                if (pending == null) return false;
                return huyDatPhongByOdrIdTx(tx, pending.getOrderDetailRoomId());
            });
        }
    }

    @Override
    public boolean huyDatPhongByOdrId(String orderDetailRoomId) {
        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> huyDatPhongByOdrIdTx(tx, orderDetailRoomId));
        }
    }

    private boolean huyDatPhongByOdrIdTx(TransactionContext tx, String odrId) {
        String query = """
            MATCH (odr:OrderDetailRoom {orderDetailRoomId: $odrId, status:'Đặt'})-[:FOR_ROOM]->(r:Room)
            MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr)
            MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
            OPTIONAL MATCH (o)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)
            DETACH DELETE odr
            WITH r, o, c, ods
            OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(remaining:OrderDetailRoom)
            WITH r, o, c, ods, count(remaining) AS roomCount
            FOREACH (_ IN CASE WHEN roomCount = 0 AND ods IS NOT NULL THEN [1] ELSE [] END |
                DETACH DELETE ods
            )
            WITH r, o, c, roomCount
            FOREACH (_ IN CASE WHEN roomCount = 0 THEN [1] ELSE [] END |
                DETACH DELETE o
            )
            WITH r, c
            OPTIONAL MATCH (x:Order)-[:OF_CUSTOMER]->(c)
            WITH r, c, count(x) AS orderCount
            FOREACH (_ IN CASE WHEN orderCount = 0 THEN [1] ELSE [] END |
                DETACH DELETE c
            )
            WITH r
            OPTIONAL MATCH (r)<-[:FOR_ROOM]-(active:OrderDetailRoom {status:'Check-in'})
            WITH r, count(active) AS activeCount
            SET r.isAvailable = CASE WHEN activeCount > 0 THEN false ELSE true END
            RETURN 1 AS ok
            """;

        Result result = tx.run(query, Map.of("odrId", odrId));
        return !result.list().isEmpty();
    }

    @Override
    public boolean checkIn(String roomID) {
        String query = """
        MATCH (r:Room {roomId: $roomID})
        OPTIONAL MATCH (r)<-[:FOR_ROOM]-(active:OrderDetailRoom {status:'Check-in'})
        WITH r, count(active) AS activeCount
        WHERE activeCount = 0

        MATCH (r)<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Đặt'})
        WITH r, odr
        ORDER BY odr.checkInDate ASC, odr.orderDetailRoomId ASC
        LIMIT 1

        SET odr.status = 'Check-in',
            r.isAvailable = false
        RETURN odr
        """;

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, Map.of("roomID", roomID));
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean checkInByOdrId(String orderDetailRoomId) {
        String query = """
        MATCH (odr:OrderDetailRoom {orderDetailRoomId: $odrId, status:'Đặt'})-[:FOR_ROOM]->(r:Room)
        OPTIONAL MATCH (r)<-[:FOR_ROOM]-(active:OrderDetailRoom {status:'Check-in'})
        WITH odr, r, count(active) AS activeCount
        WHERE activeCount = 0

        SET odr.status = 'Check-in',
            r.isAvailable = false
        RETURN odr
        """;

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, Map.of("odrId", orderDetailRoomId));
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean checkOut(String roomID) {
        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                String query = """
                    MATCH (r:Room {roomId: $roomID})<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Check-in'})
                    MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr)
                    MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
                    WITH r, odr, o, c
                    ORDER BY odr.checkInDate ASC, odr.orderDetailRoomId ASC
                    LIMIT 1
                    SET odr.status = 'Hoàn tất',
                        o.total = coalesce(o.total, 0) + coalesce(odr.roomFee, 0),
                        c.loyaltyPoint = coalesce(c.loyaltyPoint, 0) + 2,
                        r.isAvailable = true
                    RETURN o.orderId AS orderId
                    """;

                Result result = tx.run(query, Map.of("roomID", roomID));
                List<Record> rows = result.list();
                if (rows.isEmpty()) {
                    return false;
                }

                String orderId = rows.get(0).get("orderId").asString();
                recalcOrderPromotionByLoyalty(tx, orderId);
                return true;
            });
        }
    }
    @Override
    public OrderDetailRoom getPendingBookingByRoomId(String roomID) {
        String query = """
            MATCH (odr:OrderDetailRoom {status:'Đặt'})-[:FOR_ROOM]->(r:Room {roomId: $roomID})
            MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr)
            MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
            RETURN odr, o, c, r
            ORDER BY odr.checkInDate ASC, odr.orderDetailRoomId ASC
            LIMIT 1
            """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, Map.of("roomID", roomID));
                List<OrderDetailRoom> list = result.list().stream().map(this::mapOrderDetailRoom).toList();
                return list.isEmpty() ? null : list.get(0);
            });
        }
    }

    @Override
    public List<OrderDetailRoom> getPendingBookingsOfRoom(String roomID) {
        String query = """
            MATCH (r:Room {roomId: $roomID})
            MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r)
            MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
            WHERE odr.status = 'Đặt'
            RETURN odr, o, c
            ORDER BY odr.checkInDate ASC
            """;

        Map<String, Object> params = Map.of("roomID", roomID);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list().stream().map(record -> {
                    OrderDetailRoom odr = mapper.toObject(record.get("odr").asNode().asMap(), OrderDetailRoom.class);
                    Order order = mapper.toObject(record.get("o").asNode().asMap(), Order.class);
                    Customer customer = mapper.toObject(record.get("c").asNode().asMap(), Customer.class);

                    order.setCustomer(customer);
                    odr.setOrder(order);

                    return odr;
                }).collect(Collectors.toList());
            });
        }
    }

    @Override
    public OrderDetailRoom getActiveCheckInOfRoom(String roomID) {
        String query = """
            MATCH (r:Room {roomId: $roomID})
            MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r)
            MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
            WHERE odr.status = 'Check-in'
            RETURN odr, o, c
            ORDER BY odr.checkInDate DESC
            LIMIT 1
            """;

        Map<String, Object> params = Map.of("roomID", roomID);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);
                if (!result.hasNext()) return null;

                var record = result.next();

                OrderDetailRoom odr = mapper.toObject(record.get("odr").asNode().asMap(), OrderDetailRoom.class);
                Order order = mapper.toObject(record.get("o").asNode().asMap(), Order.class);
                Customer customer = mapper.toObject(record.get("c").asNode().asMap(), Customer.class);

                order.setCustomer(customer);
                odr.setOrder(order);

                return odr;
            });
        }
    }

    @Override
    public boolean giaHanPhong(String roomID, LocalDateTime newCheckOutDate) {
        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                OrderDetailRoom active = findLatestActiveStay(tx, roomID);
                if (active == null || active.getCheckInDate() == null) {
                    return false;
                }

                RoomType roomType = getRoomTypeByRoomId(tx, roomID);
                if (roomType == null) {
                    return false;
                }

                double newFee = calculateRoomFee(
                        roomType,
                        active.getBookingType() == null ? null : active.getBookingType().toString(),
                        active.getCheckInDate(),
                        newCheckOutDate
                );

                String query = """
                    MATCH (odr:OrderDetailRoom {orderDetailRoomId: $odrId})
                    SET odr.checkOutDate = $newCheckOutDate,
                        odr.roomFee = $newFee
                    RETURN odr
                    """;
                Map<String, Object> params = new HashMap<>();
                params.put("odrId", active.getOrderDetailRoomId());
                params.put("newCheckOutDate", newCheckOutDate);
                params.put("newFee", newFee);

                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean capNhatDichVuChoPhong(String roomID, String serviceName, int quantity) {
        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                String query = """
                        MATCH (r:Room {roomId: $roomID})<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Check-in'})<-[:HAS_ROOM_DETAIL]-(o:Order)
                        MATCH (s:Service {serviceName: $serviceName})
                        WHERE coalesce(s.quantity, 0) >= $quantity
                        RETURN o.orderId AS orderId, s.serviceId AS serviceId, s.price AS price
                        """;
                Map<String, Object> params = new HashMap<>();
                params.put("roomID", roomID);
                params.put("serviceName", serviceName);
                params.put("quantity", quantity);

                Result preResult = tx.run(query, params);
                if (!preResult.hasNext()) {
                    return false;
                }

                Record pre = preResult.next();
                String orderId = pre.get("orderId").asString();
                String serviceId = pre.get("serviceId").asString();
                double price = pre.get("price").asDouble();

                String odsId = "OD" + String.format("%06d", nextValue(tx, "ODS_SEQ"));
                double serviceFee = quantity * price;

                String createQuery = """
                        MATCH (o:Order {orderId: $orderId})
                        MATCH (s:Service {serviceId: $serviceId})
                        MATCH (r:Room {roomId: $roomID})
                        CREATE (ods:OrderDetailService {
                            orderDetailId: $odsId,
                            quantity: $quantity,
                            serviceFee: $serviceFee
                        })
                        MERGE (o)-[:HAS_SERVICE_DETAIL]->(ods)
                        MERGE (ods)-[:FOR_SERVICE]->(s)
                        MERGE (ods)-[:USED_IN_ROOM]->(r)
                        SET o.total = coalesce(o.total, 0) + $serviceFee,
                            s.quantity = coalesce(s.quantity, 0) - $quantity
                        RETURN ods
                        """;

                Map<String, Object> createParams = new HashMap<>();
                createParams.put("orderId", orderId);
                createParams.put("serviceId", serviceId);
                createParams.put("roomID", roomID);
                createParams.put("odsId", odsId);
                createParams.put("quantity", quantity);
                createParams.put("serviceFee", serviceFee);

                Result result = tx.run(createQuery, createParams);
                SummaryCounters counters = result.consume().counters();

                return counters.nodesCreated() > 0;
            });
        }
    }

    @Override
    public boolean changeRoomBeforeCheckIn(String oldRoomID, String newRoomID, LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                OrderDetailRoom pending = getPendingBookingTx(tx, oldRoomID);
                if (pending == null) {
                    return false;
                }

                RoomType newRoomType = getRoomTypeByRoomId(tx, newRoomID);
                if (newRoomType == null || !isRoomAvailable(tx, newRoomID)) {
                    return false;
                }

                double newFee = calculateRoomFee(
                        newRoomType,
                        pending.getBookingType() == null ? null : pending.getBookingType().toString(),
                        newCheckIn,
                        newCheckOut
                );

                String query = """
                    MATCH (oldRoom:Room {roomId: $oldRoomID})
                    MATCH (newRoom:Room {roomId: $newRoomID})
                    MATCH (odr:OrderDetailRoom {orderDetailRoomId: $odrId})-[rel:FOR_ROOM]->(oldRoom)
                    DELETE rel
                    MERGE (odr)-[:FOR_ROOM]->(newRoom)
                    SET odr.checkInDate = $newCheckIn,
                        odr.checkOutDate = $newCheckOut,
                        odr.roomFee = $newFee,
                        odr.status = 'Đặt',
                        oldRoom.isAvailable = true
                    RETURN odr
                    """;

                Map<String, Object> params = new HashMap<>();
                params.put("oldRoomID", oldRoomID);
                params.put("newRoomID", newRoomID);
                params.put("odrId", pending.getOrderDetailRoomId());
                params.put("newCheckIn", newCheckIn);
                params.put("newCheckOut", newCheckOut);
                params.put("newFee", newFee);

                Result result = tx.run(query, params);
                SummaryCounters counters = result.consume().counters();

                return counters.propertiesSet() > 0 || counters.relationshipsCreated() > 0;
            });
        }
    }

    @Override
    public boolean changeRoomWhileCheckIn(String oldRoomID, String newRoomID, LocalDateTime changeTime) {
        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                OrderDetailRoom active = getActiveCheckInTx(tx, oldRoomID);

                if (active == null || active.getCheckInDate() == null || active.getCheckOutDate() == null) {
                    return false;
                }

                LocalDateTime oldCheckIn = active.getCheckInDate();
                LocalDateTime oldCheckOut = active.getCheckOutDate();

                if (!changeTime.isAfter(oldCheckIn) || !changeTime.isBefore(oldCheckOut)) {
                    return false;
                }

                RoomType oldRoomType = getRoomTypeByRoomId(tx, oldRoomID);
                RoomType newRoomType = getRoomTypeByRoomId(tx, newRoomID);

                if (oldRoomType == null || newRoomType == null) {
                    return false;
                }

                String overlapQuery = """
                    MATCH (newRoom:Room {roomId: $newRoomID})
                    OPTIONAL MATCH (newRoom)<-[:FOR_ROOM]-(odr:OrderDetailRoom)
                    WHERE odr.status IN ['Đặt', 'Check-in']
                      AND odr.checkInDate < $newCheckOut
                      AND odr.checkOutDate > $newCheckIn
                    RETURN count(odr) AS overlapCount
                    """;

                Map<String, Object> overlapParams = new HashMap<>();
                overlapParams.put("newRoomID", newRoomID);
                overlapParams.put("newCheckIn", changeTime);
                overlapParams.put("newCheckOut", oldCheckOut);

                Result overlapResult = tx.run(overlapQuery, overlapParams);
                int overlapCount = overlapResult.single().get("overlapCount").asInt();

                if (overlapCount > 0) {
                    return false;
                }

                String bookingType = active.getBookingType() == null
                        ? null
                        : active.getBookingType().getDisplayName();

                double oldFee = calculateRoomFee(oldRoomType, bookingType, oldCheckIn, changeTime);
                double newFee = calculateRoomFee(newRoomType, bookingType, changeTime, oldCheckOut);
                String newOdrId = "ODR" + String.format("%06d", nextValue(tx, "ODR_SEQ"));

                String query = """
                    MATCH (oldRoom:Room {roomId: $oldRoomID})
                    MATCH (newRoom:Room {roomId: $newRoomID})
                    MATCH (oldOdr:OrderDetailRoom {orderDetailRoomId: $oldOdrId})<-[:HAS_ROOM_DETAIL]-(o:Order)

                    SET oldOdr.checkOutDate = $changeTime,
                        oldOdr.roomFee = $oldFee,
                        oldOdr.status = 'Check-in',
                        oldRoom.isAvailable = false

                    CREATE (newOdr:OrderDetailRoom {
                        orderDetailRoomId: $newOdrId,
                        roomFee: $newFee,
                        bookingDate: coalesce(oldOdr.bookingDate, localdatetime()),
                        checkInDate: $changeTime,
                        checkOutDate: $oldCheckOut,
                        bookingType: oldOdr.bookingType,
                        status: 'Đặt'
                    })

                    MERGE (o)-[:HAS_ROOM_DETAIL]->(newOdr)
                    MERGE (newOdr)-[:FOR_ROOM]->(newRoom)

                    SET newRoom.isAvailable = false

                    RETURN newOdr
                    """;

                Map<String, Object> params = new HashMap<>();
                params.put("oldRoomID", oldRoomID);
                params.put("newRoomID", newRoomID);
                params.put("oldOdrId", active.getOrderDetailRoomId());
                params.put("changeTime", changeTime);
                params.put("oldFee", oldFee);
                params.put("newOdrId", newOdrId);
                params.put("newFee", newFee);
                params.put("oldCheckOut", oldCheckOut);

                Result result = tx.run(query, params);
                SummaryCounters counters = result.consume().counters();

                return counters.nodesCreated() > 0 || counters.propertiesSet() > 0 || counters.relationshipsCreated() > 0;
            });
        }
    }

    @Override
    public List<RoomCalendarSlotDTO> getRoomCalendar(LocalDate fromDate, LocalDate toDate) {
        String query = """
                MATCH (odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)
                MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr)
                MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
                WHERE odr.status IN ['Đặt', 'Check-in']
                  AND odr.checkOutDate >= $fromDateTime
                  AND odr.checkInDate <= $toDateTime
                RETURN r.roomId AS roomId,
                       odr.status AS status,
                       odr.bookingType AS bookingType,
                       odr.checkInDate AS checkIn,
                       odr.checkOutDate AS checkOut,
                       c.fullName AS customer,
                       c.phone AS phone
                ORDER BY r.roomId, odr.checkInDate
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("fromDateTime", fromDate.atStartOfDay());
        params.put("toDateTime", toDate.atTime(23, 59, 59));

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            RoomCalendarSlotDTO dto = new RoomCalendarSlotDTO();
                            dto.setRoomId(record.get("roomId").asString());
                            dto.setStatus(record.get("status").asString());
                            dto.setBookingType(record.get("bookingType").asString());
                            dto.setCheckIn(record.get("checkIn").isNull() ? null : record.get("checkIn").asLocalDateTime());
                            dto.setCheckOut(record.get("checkOut").isNull() ? null : record.get("checkOut").asLocalDateTime());
                            dto.setCustomer(record.get("customer").isNull() ? null : record.get("customer").asString());
                            dto.setPhone(record.get("phone").isNull() ? null : record.get("phone").asString());
                            return dto;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public Double calculateRoomFeeByRoomID(String roomID, String bookingType,
                                           LocalDateTime checkIn, LocalDateTime checkOut) {
        String query = """
            MATCH (r:Room {roomId: $roomID})-[:HAS_TYPE]->(rt:RoomType)
            RETURN rt
            LIMIT 1
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("roomID", roomID);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                if (!result.hasNext()) {
                    return 0.0;
                }

                Record record = result.next();
                RoomType roomType = mapper.toObject(
                        record.get("rt").asNode().asMap(),
                        RoomType.class
                );

                return calculateRoomFee(roomType, bookingType, checkIn, checkOut);
            });
        }
    }

    @Override
    public LocalDateTime[] getActiveStayTimes(String roomID) {
        String query = """
            MATCH (odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room {roomId: $roomID})
            WHERE odr.status IN ['Check-in', 'Đặt']
            RETURN odr.checkInDate AS checkInDate, odr.checkOutDate AS checkOutDate
            ORDER BY odr.checkInDate ASC, odr.orderDetailRoomId ASC
            LIMIT 1
            """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, Map.of("roomID", roomID));

                if (!result.hasNext()) {
                    return new LocalDateTime[]{null, null};
                }

                Record record = result.next();
                LocalDateTime checkIn = record.get("checkInDate").isNull() ? null : record.get("checkInDate").asLocalDateTime();
                LocalDateTime checkOut = record.get("checkOutDate").isNull() ? null : record.get("checkOutDate").asLocalDateTime();

                return new LocalDateTime[]{checkIn, checkOut};
            });
        }
    }

    // ---------- helpers ----------
    private OrderDetailRoom getPendingBookingTx(TransactionContext tx, String roomID) {
        String query = """
            MATCH (odr:OrderDetailRoom {status:'Đặt'})-[:FOR_ROOM]->(r:Room {roomId: $roomID})
            MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr)
            MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
            RETURN odr, o, c, r
            ORDER BY odr.checkInDate ASC, odr.orderDetailRoomId ASC
            LIMIT 1
            """;

        Result result = tx.run(query, Map.of("roomID", roomID));
        List<OrderDetailRoom> list = result.list().stream().map(this::mapOrderDetailRoom).toList();
        return list.isEmpty() ? null : list.get(0);
    }

    private OrderDetailRoom getActiveCheckInTx(TransactionContext tx, String roomID) {
        String query = """
            MATCH (odr:OrderDetailRoom {status:'Check-in'})-[:FOR_ROOM]->(r:Room {roomId: $roomID})
            MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr)
            MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
            RETURN odr, o, c, r
            ORDER BY odr.checkInDate ASC, odr.orderDetailRoomId ASC
            LIMIT 1
            """;

        Result result = tx.run(query, Map.of("roomID", roomID));
        List<OrderDetailRoom> list = result.list().stream().map(this::mapOrderDetailRoom).toList();
        return list.isEmpty() ? null : list.get(0);
    }

    private OrderDetailRoom findLatestActiveStay(TransactionContext tx, String roomID) {
        String query = """
            MATCH (odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room {roomId: $roomID})
            MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr)
            MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
            WHERE odr.status IN ['Check-in', 'Đặt']
            RETURN odr, o, c, r
            ORDER BY odr.checkInDate ASC, odr.orderDetailRoomId ASC
            LIMIT 1
            """;

        Result result = tx.run(query, Map.of("roomID", roomID));
        List<OrderDetailRoom> list = result.list().stream().map(this::mapOrderDetailRoom).toList();
        return list.isEmpty() ? null : list.get(0);
    }

    private OdrInfoDTO mapOdrInfo(Record record) {
        OdrInfoDTO dto = new OdrInfoDTO();
        dto.setOrderDetailRoomId(record.get("orderDetailRoomId").isNull() ? null : record.get("orderDetailRoomId").asString());
        dto.setOrderId(record.get("orderId").isNull() ? null : record.get("orderId").asString());
        dto.setBookingType(record.get("bookingType").isNull() ? null : record.get("bookingType").asString());
        dto.setCheckIn(record.get("checkIn").isNull() ? null : record.get("checkIn").asLocalDateTime());
        dto.setCheckOut(record.get("checkOut").isNull() ? null : record.get("checkOut").asLocalDateTime());
        dto.setRoomFee(record.get("roomFee").isNull() ? 0 : record.get("roomFee").asDouble());
        dto.setCustomerId(record.get("customerId").isNull() ? null : record.get("customerId").asString());
        dto.setFullName(record.get("fullName").isNull() ? null : record.get("fullName").asString());
        dto.setPhone(record.get("phone").isNull() ? null : record.get("phone").asString());
        return dto;
    }

    private String findOrCreateCustomer(TransactionContext tx, Customer customer, LocalDateTime regisDate) {
        String findQuery = """
                MATCH (c:Customer)
                WHERE c.phone = $phone
                RETURN c.customerId AS customerId
                LIMIT 1
                """;
        Map<String, Object> findParams = Map.of("phone", customer.getPhone());

        Result findResult = tx.run(findQuery, findParams);
        if (findResult.hasNext()) {
            return findResult.next().get("customerId").asString();
        }

        long seq = nextValue(tx, "CUSTOMER_SEQ");
        String customerId = "Cus" + String.format("%06d", seq);

        String createQuery = """
                CREATE (c:Customer {
                    customerId: $customerId,
                    fullName: $fullName,
                    phone: $phone,
                    email: $email,
                    regisDate: $regisDate,
                    idCard: $idCard,
                    loyaltyPoint: 0
                })
                RETURN c
                """;
        Map<String, Object> createParams = new HashMap<>();
        createParams.put("customerId", customerId);
        createParams.put("fullName", customer.getFullName());
        createParams.put("phone", customer.getPhone());
        createParams.put("email", customer.getEmail());
        createParams.put("regisDate", regisDate != null ? regisDate : LocalDateTime.now());
        createParams.put("idCard", customer.getIdCard());

        Result createResult = tx.run(createQuery, createParams);
        return createResult.consume().counters().nodesCreated() > 0 ? customerId : null;
    }

    private String findOrCreatePendingOrder(TransactionContext tx, String customerId, String employeeId) {
        String findQuery = """
                MATCH (o:Order)-[:OF_CUSTOMER]->(c:Customer {customerId: $customerId})
                WHERE o.orderStatus = 'Chưa thanh toán'
                RETURN o.orderId AS orderId
                ORDER BY o.orderDate DESC
                LIMIT 1
                """;
        Map<String, Object> findParams = Map.of("customerId", customerId);

        Result findResult = tx.run(findQuery, findParams);
        if (findResult.hasNext()) {
            return findResult.next().get("orderId").asString();
        }

        String employeeQuery = """
            MATCH (e:Employee {employeeId: $employeeId})-[:BELONGS_TO]->(et:EmployeeType)
            RETURN e.fullName AS fullName, et.typeName AS typeName
        """;
        Result employeeResult = tx.run(employeeQuery, Map.of("employeeId", employeeId));
        if (!employeeResult.hasNext()) {
            return null;
        }

        Record employeeRecord = employeeResult.next();
        String employeeFullName = employeeRecord.get("fullName").asString();
        String employeeTypeName = employeeRecord.get("typeName").asString();

        long seq = nextValue(tx, "ORDER_SEQ");
        String orderId = buildOrderId(employeeTypeName, employeeFullName, seq);

        String createQuery = """
                MATCH (e:Employee {employeeId: $employeeId})
                MATCH (c:Customer {customerId: $customerId})
                CREATE (o:Order {
                    orderId: $orderId,
                    orderDate: localdatetime(),
                    total: 0,
                    orderStatus: 'Chưa thanh toán'
                })
                MERGE (o)-[:CREATED_BY]->(e)
                MERGE (o)-[:OF_CUSTOMER]->(c)
                RETURN o
                """;

        Map<String, Object> createParams = new HashMap<>();
        createParams.put("employeeId", employeeId);
        createParams.put("customerId", customerId);
        createParams.put("orderId", orderId);

        Result createResult = tx.run(createQuery, createParams);
        return createResult.consume().counters().nodesCreated() > 0 ? orderId : null;
    }

    private void recalcOrderPromotionByLoyalty(TransactionContext tx, String orderId) {
        String query = """
                MATCH (o:Order {orderId: $orderId})-[:OF_CUSTOMER]->(c:Customer)
                WITH o, c,
                     CASE
                       WHEN c.loyaltyPoint >= 40 THEN 20
                       WHEN c.loyaltyPoint >= 20 THEN 15
                       WHEN c.loyaltyPoint >= 10 THEN 10
                       ELSE 0
                     END AS discountPercent
                OPTIONAL MATCH (p:Promotion)
                WHERE discountPercent > 0
                  AND p.discount = discountPercent
                  AND p.startTime <= localdatetime()
                  AND p.endTime >= localdatetime()
                  AND coalesce(p.quantity, 0) > 0
                WITH o, p
                ORDER BY p.startTime DESC
                WITH o, collect(p)[0] AS selectedPromotion
                OPTIONAL MATCH (o)-[old:APPLIES_PROMOTION]->(:Promotion)
                DELETE old
                FOREACH (_ IN CASE WHEN selectedPromotion IS NULL THEN [] ELSE [1] END |
                    MERGE (o)-[:APPLIES_PROMOTION]->(selectedPromotion)
                )
                RETURN o
                """;
        tx.run(query, Map.of("orderId", orderId)).consume();
    }

    private boolean isRoomAvailable(TransactionContext tx, String roomId) {
        String query = """
            MATCH (r:Room {roomId: $roomId})
            RETURN coalesce(r.isAvailable, true) AS available
            """;
        Result result = tx.run(query, Map.of("roomId", roomId));
        return result.hasNext() && result.next().get("available").asBoolean();
    }

    private RoomType getRoomTypeByRoomId(TransactionContext tx, String roomId) {
        String query = """
                MATCH (r:Room {roomId: $roomId})-[:HAS_TYPE]->(rt:RoomType)
                RETURN rt
                """;
        Result result = tx.run(query, Map.of("roomId", roomId));

        List<RoomType> list = result.list()
                .stream()
                .map(record -> mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class))
                .collect(Collectors.toList());

        return list.isEmpty() ? null : list.get(0);
    }

    private long nextValue(TransactionContext tx, String counterName) {
        String query = """
                MATCH (c:Counter {name: $counterName})
                SET c.value = c.value + 1
                RETURN c.value AS seq
                """;
        Map<String, Object> params = Map.of("counterName", counterName);

        Result result = tx.run(query, params);
        return result.single().get("seq").asLong();
    }

    private String buildOrderId(String employeeTypeName, String fullName, long seq) {
        String prefix = "NV";
        if (employeeTypeName != null) {
            switch (employeeTypeName.trim()) {
                case "Lễ tân" -> prefix = "LT";
                case "Quản lý" -> prefix = "QL";
            }
        }

        String initials = "XX";
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            initials = parts[0].substring(0, 1).toUpperCase()
                    + parts[parts.length - 1].substring(0, 1).toUpperCase();
        }

        return prefix + initials + String.format("%04d", seq);
    }

    private double calculateRoomFee(RoomType roomType, String bookingType, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (roomType == null || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 0;
        }

        String type = normalizeBookingType(bookingType);

        return switch (type) {
            case "GIO" -> {
                long minutes = Duration.between(checkIn, checkOut).toMinutes();
                long hours = (long) Math.ceil(minutes / 60.0);
                hours = Math.max(1, hours);
                yield hours * roomType.getPricePerHour();
            }

            case "NGAY" -> {
                long days = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());

                if (days <= 0) {
                    days = 1;
                }

                // Nếu có lẻ giờ sang ngày sau thì vẫn tính tròn ngày
                LocalDateTime expectedCheckout = checkIn.plusDays(days);
                if (checkOut.isAfter(expectedCheckout)) {
                    days++;
                }

                yield days * roomType.getPricePerDay();
            }

            case "DEM" -> {
                long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());

                if (nights <= 0) {
                    nights = 1;
                }

                yield nights * roomType.getPricePerNight();
            }

            default -> 0;
        };
    }

    private String normalizeBookingType(String bookingType) {
        if (bookingType == null) {
            return "";
        }

        String value = bookingType.trim();

        if (value.equalsIgnoreCase("Giờ") || value.equalsIgnoreCase("GIO") || value.equalsIgnoreCase("GIỜ")) {
            return "GIO";
        }

        if (value.equalsIgnoreCase("Ngày") || value.equalsIgnoreCase("NGAY") || value.equalsIgnoreCase("NGÀY")) {
            return "NGAY";
        }

        if (value.equalsIgnoreCase("Đêm") || value.equalsIgnoreCase("Dem") || value.equalsIgnoreCase("DEM") || value.equalsIgnoreCase("ĐÊM")) {
            return "DEM";
        }

        String upper = value.toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "GIO", "GIỜ" -> "GIO";
            case "NGAY", "NGÀY" -> "NGAY";
            case "DEM", "ĐÊM" -> "DEM";
            default -> upper;
        };
    }

    private OrderDetailRoom mapOrderDetailRoom(Record record) {
        OrderDetailRoom odr = mapper.toObject(record.get("odr").asNode().asMap(), OrderDetailRoom.class);

        if (record.get("odr").asNode().containsKey("orderDetailRoomId")) {
            odr.setOrderDetailRoomId(record.get("odr").asNode().get("orderDetailRoomId").asString());
        }

        Order order = mapper.toObject(record.get("o").asNode().asMap(), Order.class);
        Customer customer = mapper.toObject(record.get("c").asNode().asMap(), Customer.class);

        order.setCustomer(customer);
        odr.setOrder(order);

        if (record.containsKey("r") && !record.get("r").isNull()) {
            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
            odr.setRoom(room);
        }

        return odr;
    }
}