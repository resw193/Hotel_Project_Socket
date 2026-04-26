package server.core.repository.impl;

import common.entity.Promotion;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.PromotionRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PromotionRepositoryImpl implements PromotionRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public PromotionRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<Promotion> findAll() {
        String query = """
                MATCH (p:Promotion)
                RETURN p
                ORDER BY p.promotionId
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("p").asNode().asMap(), Promotion.class))
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public Promotion findById(String promotionId) {
        String query = """
                MATCH (p:Promotion)
                WHERE p.promotionId = $promotionId
                RETURN p
                """;
        Map<String, Object> params = Map.of("promotionId", promotionId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Promotion> promotions = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("p").asNode().asMap(), Promotion.class))
                        .collect(Collectors.toList());

                return promotions.isEmpty() ? null : promotions.get(0);
            });
        }
    }

    @Override
    public boolean add(Promotion promotion) {
        String query = """
                CREATE (p:Promotion {
                    promotionId: $promotionId,
                    promotionName: $promotionName,
                    discount: $discount,
                    quantity: $quantity,
                    startTime: $startTime,
                    endTime: $endTime
                })
                RETURN p
                """;
        Map<String, Object> params = Map.of(
                "promotionId", promotion.getPromotionId(),
                "promotionName", promotion.getPromotionName(),
                "discount", promotion.getDiscount(),
                "quantity", promotion.getQuantity(),
                "startTime", promotion.getStartTime(),
                "endTime", promotion.getEndTime()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().nodesCreated() > 0;
            });
        }
    }

    @Override
    public boolean update(Promotion promotion) {
        String query = """
                MATCH (p:Promotion)
                WHERE p.promotionId = $promotionId
                SET p.promotionName = $promotionName,
                    p.discount = $discount,
                    p.quantity = $quantity,
                    p.startTime = $startTime,
                    p.endTime = $endTime
                RETURN p
                """;
        Map<String, Object> params = Map.of(
                "promotionId", promotion.getPromotionId(),
                "promotionName", promotion.getPromotionName(),
                "discount", promotion.getDiscount(),
                "quantity", promotion.getQuantity(),
                "startTime", promotion.getStartTime(),
                "endTime", promotion.getEndTime()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean deleteById(String promotionId) {
        String query = """
                MATCH (p:Promotion)
                WHERE p.promotionId = $promotionId
                  AND NOT (:Order)-[:APPLIES_PROMOTION]->(p)
                DELETE p
                RETURN p
                """;
        Map<String, Object> params = Map.of("promotionId", promotionId);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().nodesDeleted() > 0;
            });
        }
    }
}