package server.core.repository.impl;

import common.entity.Order;
import common.entity.OrderDetailRoom;
import common.entity.Room;
import common.entity.RoomType;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.OrderDetailRoomRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderDetailRoomRepositoryImpl implements OrderDetailRoomRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public OrderDetailRoomRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<OrderDetailRoom> findByOrderId(String orderId) {
        String query = """
                MATCH (o:Order {orderId: $orderId})-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)-[:HAS_TYPE]->(rt:RoomType)
                RETURN o, odr, r, rt
                ORDER BY odr.checkInDate
                """;
        Map<String, Object> params = Map.of("orderId", orderId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            OrderDetailRoom detail = mapper.toObject(record.get("odr").asNode().asMap(), OrderDetailRoom.class);

                            Order order = mapper.toObject(record.get("o").asNode().asMap(), Order.class);

                            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                            RoomType roomType = mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class);
                            room.setRoomType(roomType);

                            detail.setOrder(order);
                            detail.setRoom(room);

                            return detail;
                        })
                        .collect(Collectors.toList());
            });
        }
    }
}