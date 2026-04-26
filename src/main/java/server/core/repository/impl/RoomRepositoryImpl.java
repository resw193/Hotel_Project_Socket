package server.core.repository.impl;

import common.entity.Room;
import common.entity.RoomType;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.RoomRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomRepositoryImpl implements RoomRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public RoomRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<Room> findAll() {
        String query = """
                MATCH (r:Room)-[:HAS_TYPE]->(rt:RoomType)
                RETURN r, rt
                ORDER BY toInteger(right(r.roomId, 2))
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> {
                            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                            room.setAvailable(record.get("r").asNode().get("isAvailable").asBoolean());

                            RoomType roomType = mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class);
                            room.setRoomType(roomType);
                            return room;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Room> findByStatus(String status) {
        String query = """
                MATCH (odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)-[:HAS_TYPE]->(rt:RoomType)
                WHERE odr.status = $status
                RETURN DISTINCT r, rt
                ORDER BY toInteger(right(r.roomId, 2))
                """;
        Map<String, Object> params = Map.of("status", status);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                            room.setAvailable(record.get("r").asNode().get("isAvailable").asBoolean());

                            RoomType roomType = mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class);
                            room.setRoomType(roomType);
                            return room;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Room> findByTypeName(String typeName) {
        String query = """
                MATCH (r:Room)-[:HAS_TYPE]->(rt:RoomType)
                WHERE rt.typeName = $typeName
                RETURN r, rt
                ORDER BY toInteger(right(r.roomId, 2))
                """;
        Map<String, Object> params = Map.of("typeName", typeName);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                            room.setAvailable(record.get("r").asNode().get("isAvailable").asBoolean());

                            RoomType roomType = mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class);
                            room.setRoomType(roomType);
                            return room;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Room> findByOccupancy(boolean available) {
        String query = """
                MATCH (r:Room)-[:HAS_TYPE]->(rt:RoomType)
                WHERE r.isAvailable = $available
                RETURN r, rt
                ORDER BY toInteger(right(r.roomId, 2))
                """;
        Map<String, Object> params = Map.of("available", available);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                            room.setAvailable(record.get("r").asNode().get("isAvailable").asBoolean());

                            RoomType roomType = mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class);
                            room.setRoomType(roomType);
                            return room;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public Room findById(String roomId) {
        String query = """
                MATCH (r:Room)-[:HAS_TYPE]->(rt:RoomType)
                WHERE r.roomId = $roomId
                RETURN r, rt
                """;
        Map<String, Object> params = Map.of("roomId", roomId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Room> rooms = result.list()
                        .stream()
                        .map(record -> {
                            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                            room.setAvailable(record.get("r").asNode().get("isAvailable").asBoolean());

                            RoomType roomType = mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class);
                            room.setRoomType(roomType);
                            return room;
                        })
                        .collect(Collectors.toList());

                return rooms.isEmpty() ? null : rooms.get(0);
            });
        }
    }

    @Override
    public boolean add(Room room) {
        String query = """
                MATCH (rt:RoomType {roomTypeId: $roomTypeId})
                CREATE (r:Room {
                    roomId: $roomId,
                    description: $description,
                    isAvailable: $available,
                    imgRoomSource: $imgRoomSource,
                    view: $view
                })
                MERGE (r)-[:HAS_TYPE]->(rt)
                RETURN r
                """;
        Map<String, Object> params = Map.of(
                "roomId", room.getRoomId(),
                "description", room.getDescription(),
                "available", room.isAvailable(),
                "imgRoomSource", room.getImgRoomSource(),
                "view", room.getView(),
                "roomTypeId", room.getRoomType().getRoomTypeId()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().nodesCreated() > 0;
            });
        }
    }

    @Override
    public boolean update(Room room) {
        String query = """
                MATCH (r:Room)
                WHERE r.roomId = $roomId
                SET r.description = $description,
                    r.imgRoomSource = $imgRoomSource,
                    r.view = $view
                RETURN r
                """;
        Map<String, Object> params = Map.of(
                "roomId", room.getRoomId(),
                "description", room.getDescription(),
                "imgRoomSource", room.getImgRoomSource(),
                "view", room.getView()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public List<Room> findAvailableByTypeAndView(String roomTypeName, String view) {
        String query = """
                MATCH (r:Room)-[:HAS_TYPE]->(rt:RoomType)
                WHERE r.isAvailable = true
                  AND ($roomTypeName IS NULL OR rt.typeName = $roomTypeName)
                  AND ($view IS NULL OR r.view = $view)
                RETURN r, rt
                ORDER BY toInteger(right(r.roomId, 2))
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("roomTypeName", roomTypeName);
        params.put("view", view);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Room room = mapper.toObject(record.get("r").asNode().asMap(), Room.class);
                            room.setAvailable(record.get("r").asNode().get("isAvailable").asBoolean());

                            RoomType roomType = mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class);
                            room.setRoomType(roomType);
                            return room;
                        })
                        .collect(Collectors.toList());
            });
        }
    }
}