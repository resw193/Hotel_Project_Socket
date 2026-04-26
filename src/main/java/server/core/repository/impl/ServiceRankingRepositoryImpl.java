package server.core.repository.impl;

import common.dto.ServiceRankingDTO;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.ServiceRankingRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceRankingRepositoryImpl implements ServiceRankingRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public ServiceRankingRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<ServiceRankingDTO> getByRange(LocalDateTime start, LocalDateTime end) {
        String query = """
                MATCH (o:Order)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)-[:FOR_SERVICE]->(s:Service)
                WHERE o.orderStatus = 'Thanh toán'
                  AND o.orderDate >= $start
                  AND o.orderDate <= $end
                RETURN s.serviceName AS serviceName,
                       coalesce(sum(ods.quantity), 0) AS totalQuantity,
                       coalesce(sum(ods.serviceFee), 0) AS totalRevenue
                ORDER BY totalRevenue DESC
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
                            data.put("serviceName", record.get("serviceName").isNull() ? null : record.get("serviceName").asString());
                            data.put("totalQuantity", record.get("totalQuantity").asInt());
                            data.put("totalRevenue", record.get("totalRevenue").asDouble());
                            return mapper.toObject(data, ServiceRankingDTO.class);
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<ServiceRankingDTO> getTopByRange(LocalDateTime start, LocalDateTime end, int topN) {
        String query = """
                MATCH (o:Order)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)-[:FOR_SERVICE]->(s:Service)
                WHERE o.orderStatus = 'Thanh toán'
                  AND o.orderDate >= $start
                  AND o.orderDate <= $end
                RETURN s.serviceName AS serviceName,
                       coalesce(sum(ods.quantity), 0) AS totalQuantity,
                       coalesce(sum(ods.serviceFee), 0) AS totalRevenue
                ORDER BY totalQuantity DESC
                LIMIT $topN
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
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
                            data.put("totalRevenue", record.get("totalRevenue").asDouble());
                            return mapper.toObject(data, ServiceRankingDTO.class);
                        })
                        .collect(Collectors.toList());
            });
        }
    }
}