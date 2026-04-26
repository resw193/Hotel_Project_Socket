package server.core.repository.impl;

import common.entity.RoomType;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.RoomTypeRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomTypeRepositoryImpl implements RoomTypeRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public RoomTypeRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<RoomType> findAll() {
        String query = """
                MATCH (rt:RoomType)
                RETURN rt
                ORDER BY rt.roomTypeId
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class))
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public RoomType findById(String roomTypeId) {
        String query = """
                MATCH (rt:RoomType)
                WHERE rt.roomTypeId = $roomTypeId
                RETURN rt
                """;
        Map<String, Object> params = Map.of("roomTypeId", roomTypeId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<RoomType> roomTypes = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class))
                        .collect(Collectors.toList());

                return roomTypes.isEmpty() ? null : roomTypes.get(0);
            });
        }
    }

    @Override
    public RoomType findByTypeName(String typeName) {
        String query = """
                MATCH (rt:RoomType)
                WHERE rt.typeName = $typeName
                RETURN rt
                """;
        Map<String, Object> params = Map.of("typeName", typeName);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<RoomType> roomTypes = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("rt").asNode().asMap(), RoomType.class))
                        .collect(Collectors.toList());

                return roomTypes.isEmpty() ? null : roomTypes.get(0);
            });
        }
    }

    @Override
    public boolean update(RoomType roomType) {
        String query = """
                MATCH (rt:RoomType {roomTypeId: $roomTypeId})
                SET rt.typeName = $typeName,
                    rt.pricePerHour = $pricePerHour,
                    rt.pricePerNight = $pricePerNight,
                    rt.pricePerDay = $pricePerDay,
                    rt.lateFeePerHour = $lateFeePerHour,
                    rt.maxAdults = $maxAdults,
                    rt.maxChildren = $maxChildren,
                    rt.description = $description
                RETURN rt
                """;

        Map<String, Object> params = Map.of(
                "roomTypeId", roomType.getRoomTypeId(),
                "typeName", roomType.getTypeName(),
                "pricePerHour", roomType.getPricePerHour(),
                "pricePerNight", roomType.getPricePerNight(),
                "pricePerDay", roomType.getPricePerDay(),
                "lateFeePerHour", roomType.getLateFeePerHour(),
                "maxAdults", roomType.getMaxAdults(),
                "maxChildren", roomType.getMaxChildren(),
                "description", roomType.getDescription()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }
}