package server.core.repository.impl;

import common.entity.*;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.OrderDetailServiceRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderDetailServiceRepositoryImpl implements OrderDetailServiceRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public OrderDetailServiceRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<OrderDetailService> findByOrderId(String orderId) {
        String query = """
                MATCH (o:Order {orderId: $orderId})-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)-[:FOR_SERVICE]->(s:Service)
                OPTIONAL MATCH (s)-[:HAS_UNIT]->(u:UnitPrice)
                OPTIONAL MATCH (ods)-[:USED_IN_ROOM]->(r:Room)
                RETURN o, ods, s, u, r
                ORDER BY s.serviceName
                """;
        Map<String, Object> params = Map.of("orderId", orderId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            OrderDetailService detail = mapper.toObject(record.get("ods").asNode().asMap(), OrderDetailService.class);

                            Order order = mapper.toObject(record.get("o").asNode().asMap(), Order.class);

                            Service service = mapper.toObject(record.get("s").asNode().asMap(), Service.class);
                            if (!record.get("u").isNull()) {
                                UnitPrice unitPrice = mapper.toObject(record.get("u").asNode().asMap(), UnitPrice.class);
                                service.setUnitPrice(unitPrice);
                            }

                            detail.setOrder(order);
                            detail.setService(service);

                            if (!record.get("r").isNull()) {
                                Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                                detail.setRoom(room);
                            }

                            return detail;
                        })
                        .collect(Collectors.toList());
            });
        }
    }
}