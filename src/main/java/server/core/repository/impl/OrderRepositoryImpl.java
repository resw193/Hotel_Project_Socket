package server.core.repository.impl;

import common.dto.OrderPayDTO;
import common.entity.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.summary.SummaryCounters;
import server.core.repository.OrderRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderRepositoryImpl implements OrderRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public OrderRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<Order> findAll() {
        String query = """
                MATCH (o:Order)-[:CREATED_BY]->(e:Employee)-[:BELONGS_TO]->(et:EmployeeType)
                MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
                OPTIONAL MATCH (o)-[:APPLIES_PROMOTION]->(p:Promotion)
                RETURN o, e, et, c, p
                ORDER BY o.orderDate DESC
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(this::mapOrderRecord)
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Order> findByStatus(String status) {
        String query = """
                MATCH (o:Order)-[:CREATED_BY]->(e:Employee)-[:BELONGS_TO]->(et:EmployeeType)
                MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
                OPTIONAL MATCH (o)-[:APPLIES_PROMOTION]->(p:Promotion)
                WHERE o.orderStatus = $status
                RETURN o, e, et, c, p
                ORDER BY o.orderDate DESC
                """;
        Map<String, Object> params = Map.of("status", status);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(this::mapOrderRecord)
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public Order findById(String orderId) {
        String query = """
                MATCH (o:Order)-[:CREATED_BY]->(e:Employee)-[:BELONGS_TO]->(et:EmployeeType)
                MATCH (o)-[:OF_CUSTOMER]->(c:Customer)
                OPTIONAL MATCH (o)-[:APPLIES_PROMOTION]->(p:Promotion)
                WHERE o.orderId = $orderId
                RETURN o, e, et, c, p
                """;
        Map<String, Object> params = Map.of("orderId", orderId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Order> orders = result.list()
                        .stream()
                        .map(this::mapOrderRecord)
                        .collect(Collectors.toList());

                return orders.isEmpty() ? null : orders.get(0);
            });
        }
    }

    @Override
    public boolean updateOrderPromotion(String orderId, String promotionId) {
        String query = """
                MATCH (o:Order)
                WHERE o.orderId = $orderId
                OPTIONAL MATCH (o)-[old:APPLIES_PROMOTION]->(:Promotion)
                OPTIONAL MATCH (p:Promotion {promotionId: $promotionId})
                FOREACH (_ IN CASE WHEN old IS NULL THEN [] ELSE [1] END |
                    DELETE old
                )
                FOREACH (_ IN CASE WHEN p IS NULL THEN [] ELSE [1] END |
                    MERGE (o)-[:APPLIES_PROMOTION]->(p)
                )
                RETURN o
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("promotionId", promotionId);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                SummaryCounters counters = result.consume().counters();

                return counters.relationshipsCreated() > 0 || counters.relationshipsDeleted() > 0 || counters.containsUpdates();
            });
        }
    }

    @Override
    public List<OrderPayDTO> payOrder(String orderId) {
        String updateQuery = """
            MATCH (o:Order {orderId: $orderId})
            WHERE o.orderStatus = 'Chưa thanh toán'
    
            OPTIONAL MATCH (o)-[:APPLIES_PROMOTION]->(p:Promotion)
            WITH o, coalesce(p.discount, 0.0) AS discountPercent, p
    
            WITH o, p, coalesce(o.total, 0.0) AS baseTotal, discountPercent
            WITH o, p, baseTotal, discountPercent,
                 (baseTotal * discountPercent / 100.0) AS discountAmount
            WITH o, p, baseTotal, discountPercent, discountAmount,
                 (baseTotal - discountAmount) AS afterDiscount
            WITH o, p, baseTotal, discountPercent, discountAmount, afterDiscount,
                 (afterDiscount * 0.10) AS vatAmount,
                 (afterDiscount + afterDiscount * 0.10) AS finalTotal
    
            SET o.orderStatus = 'Thanh toán',
                o.orderDate = localdatetime(),
                o.total = finalTotal
    
            WITH o, p
            OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)
            SET odr.status = CASE
                                WHEN odr.status = 'Đặt' OR odr.status = 'Check-in'
                                THEN 'Hoàn tất'
                                ELSE odr.status
                             END,
                r.isAvailable = true
    
            WITH DISTINCT o, p
            FOREACH (_ IN CASE WHEN p IS NULL THEN [] ELSE [1] END |
                SET p.quantity = CASE WHEN p.quantity > 0 THEN p.quantity - 1 ELSE 0 END
            )
    
            RETURN o
        """;

        String infoQuery = """
                MATCH (o:Order {orderId: $orderId})
                OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)-[:HAS_TYPE]->(rt:RoomType)
                OPTIONAL MATCH (o)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)-[:USED_IN_ROOM]->(r)
                OPTIONAL MATCH (ods)-[:FOR_SERVICE]->(s:Service)
                RETURN o.orderId AS orderId,
                       r.description AS description,
                       rt.typeName AS roomTypeName,
                       odr.bookingDate AS bookingDate,
                       odr.checkInDate AS checkInDate,
                       odr.checkOutDate AS checkOutDate,
                       odr.bookingType AS bookingType,
                       s.serviceName AS serviceName,
                       coalesce(ods.quantity, 0) AS serviceQuantity
                ORDER BY description, serviceName
                """;

        Map<String, Object> params = Map.of("orderId", orderId);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result updateResult = tx.run(updateQuery, params);
                SummaryCounters counters = updateResult.consume().counters();

                if (!counters.containsUpdates()) {
                    return List.of();
                }

                Result result = tx.run(infoQuery, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            OrderPayDTO dto = new OrderPayDTO();
                            dto.setOrderId(record.get("orderId").isNull() ? null : record.get("orderId").asString());
                            dto.setDescription(record.get("description").isNull() ? null : record.get("description").asString());
                            dto.setRoomTypeName(record.get("roomTypeName").isNull() ? null : record.get("roomTypeName").asString());
                            dto.setBookingDate(record.get("bookingDate").isNull() ? null : record.get("bookingDate").asLocalDateTime());
                            dto.setCheckInDate(record.get("checkInDate").isNull() ? null : record.get("checkInDate").asLocalDateTime());
                            dto.setCheckOutDate(record.get("checkOutDate").isNull() ? null : record.get("checkOutDate").asLocalDateTime());
                            dto.setBookingType(record.get("bookingType").isNull() ? null : record.get("bookingType").asString());
                            dto.setServiceName(record.get("serviceName").isNull() ? null : record.get("serviceName").asString());
                            dto.setServiceQuantity(record.get("serviceQuantity").isNull() ? 0 : record.get("serviceQuantity").asInt());
                            return dto;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    private Order mapOrderRecord(Record record) {
        Order order = mapper.toObject(record.get("o").asNode().asMap(), Order.class);

        Employee employee = mapper.toObject(record.get("e").asNode().asMap(), Employee.class);
        EmployeeType employeeType = mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class);
        employee.setEmployeeType(employeeType);

        Customer customer = mapper.toObject(record.get("c").asNode().asMap(), Customer.class);

        order.setEmployee(employee);
        order.setCustomer(customer);

        if (!record.get("p").isNull()) {
            Promotion promotion = mapper.toObject(record.get("p").asNode().asMap(), Promotion.class);
            order.setPromotion(promotion);
        }

        return order;
    }
}