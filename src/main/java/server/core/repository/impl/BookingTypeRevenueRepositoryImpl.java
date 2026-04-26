package server.core.repository.impl;

import common.dto.BookingTypeRevenueDTO;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.BookingTypeRevenueRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingTypeRevenueRepositoryImpl implements BookingTypeRevenueRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public BookingTypeRevenueRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<BookingTypeRevenueDTO> stats(LocalDateTime start, LocalDateTime end) {
        String query = """
                MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
                WHERE o.orderStatus = 'Thanh toán'
                  AND o.orderDate >= $start
                  AND o.orderDate <= $end
                RETURN odr.bookingType AS bookingType,
                       count(odr) AS soLuot,
                       coalesce(sum(odr.roomFee), 0) AS roomRevenue
                ORDER BY roomRevenue DESC
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("bookingType", record.get("bookingType").isNull() ? null : record.get("bookingType").asString());
                            data.put("soLuot", record.get("soLuot").asInt());
                            data.put("roomRevenue", record.get("roomRevenue").asDouble());
                            return mapper.toObject(data, BookingTypeRevenueDTO.class);
                        })
                        .collect(Collectors.toList());
            });
        }
    }
}