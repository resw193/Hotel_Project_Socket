package server.core.repository.impl;

import common.entity.Service;
import common.entity.UnitPrice;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.summary.SummaryCounters;
import server.core.repository.ServiceRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceRepositoryImpl implements ServiceRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public ServiceRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<Service> findAll() {
        String query = """
                MATCH (s:Service)
                OPTIONAL MATCH (s)-[:HAS_UNIT]->(u:UnitPrice)
                RETURN s, u
                ORDER BY toInteger(right(s.serviceId, 2))
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> {
                            Service service = mapper.toObject(record.get("s").asNode().asMap(), Service.class);

                            if (!record.get("u").isNull()) {
                                UnitPrice unitPrice = mapper.toObject(record.get("u").asNode().asMap(), UnitPrice.class);
                                service.setUnitPrice(unitPrice);
                            }

                            return service;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Service> findByType(String serviceType) {
        String query = """
            MATCH (s:Service)
            WHERE toLower(trim(coalesce(s.serviceType, ""))) = toLower(trim($serviceType))
            OPTIONAL MATCH (s)-[:HAS_UNIT]->(u:UnitPrice)
            RETURN s, u
            ORDER BY toInteger(right(s.serviceId, 2))
            """;
        Map<String, Object> params = Map.of("serviceType", serviceType);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Service service = mapper.toObject(record.get("s").asNode().asMap(), Service.class);

                            if (!record.get("u").isNull()) {
                                UnitPrice unitPrice = mapper.toObject(record.get("u").asNode().asMap(), UnitPrice.class);
                                service.setUnitPrice(unitPrice);
                            }

                            return service;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public Service findById(String serviceId) {
        String query = """
                MATCH (s:Service)
                OPTIONAL MATCH (s)-[:HAS_UNIT]->(u:UnitPrice)
                WHERE s.serviceId = $serviceId
                RETURN s, u
                """;
        Map<String, Object> params = Map.of("serviceId", serviceId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Service> services = result.list()
                        .stream()
                        .map(record -> {
                            Service service = mapper.toObject(record.get("s").asNode().asMap(), Service.class);

                            if (!record.get("u").isNull()) {
                                UnitPrice unitPrice = mapper.toObject(record.get("u").asNode().asMap(), UnitPrice.class);
                                service.setUnitPrice(unitPrice);
                            }

                            return service;
                        })
                        .collect(Collectors.toList());

                return services.isEmpty() ? null : services.get(0);
            });
        }
    }

    @Override
    public Service findByName(String serviceName) {
        String query = """
                MATCH (s:Service)
                OPTIONAL MATCH (s)-[:HAS_UNIT]->(u:UnitPrice)
                WHERE s.serviceName = $serviceName
                RETURN s, u
                """;
        Map<String, Object> params = Map.of("serviceName", serviceName);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Service> services = result.list()
                        .stream()
                        .map(record -> {
                            Service service = mapper.toObject(record.get("s").asNode().asMap(), Service.class);

                            if (!record.get("u").isNull()) {
                                UnitPrice unitPrice = mapper.toObject(record.get("u").asNode().asMap(), UnitPrice.class);
                                service.setUnitPrice(unitPrice);
                            }

                            return service;
                        })
                        .collect(Collectors.toList());

                return services.isEmpty() ? null : services.get(0);
            });
        }
    }

    @Override
    public boolean add(Service service) {
        String query = """
                OPTIONAL MATCH (u1:UnitPrice {unitId: $unitId})
                OPTIONAL MATCH (u2:UnitPrice {unitName: $unitName})
                WITH coalesce(u1, u2) AS u
                CREATE (s:Service {
                    serviceId: $serviceId,
                    serviceName: $serviceName,
                    serviceType: $serviceType,
                    quantity: $quantity,
                    price: $price,
                    imgSource: $imgSource
                })
                FOREACH (_ IN CASE WHEN u IS NULL THEN [] ELSE [1] END |
                    MERGE (s)-[:HAS_UNIT]->(u)
                )
                RETURN s
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", service.getServiceId());
        params.put("serviceName", service.getServiceName());
        params.put("serviceType", service.getServiceType());
        params.put("quantity", service.getQuantity());
        params.put("price", service.getPrice());
        params.put("imgSource", service.getImgSource());
        params.put("unitId", service.getUnitPrice() != null ? service.getUnitPrice().getUnitId() : null);
        params.put("unitName", service.getUnitPrice() != null ? service.getUnitPrice().getUnitName() : null);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().nodesCreated() > 0;
            });
        }
    }

    @Override
    public boolean update(Service service) {
        String query = """
            MATCH (s:Service)
            WHERE s.serviceId = $serviceId
            OPTIONAL MATCH (s)-[r:HAS_UNIT]->(:UnitPrice)
            OPTIONAL MATCH (u1:UnitPrice {unitId: $unitId})
            OPTIONAL MATCH (u2:UnitPrice {unitName: $unitName})
            WITH s, r, coalesce(u1, u2) AS newUnit
            SET s.serviceName = $serviceName,
                s.serviceType = $serviceType,
                s.quantity = $quantity,
                s.price = $price,
                s.imgSource = $imgSource
            FOREACH (_ IN CASE WHEN r IS NULL THEN [] ELSE [1] END |
                DELETE r
            )
            FOREACH (_ IN CASE WHEN newUnit IS NULL THEN [] ELSE [1] END |
                MERGE (s)-[:HAS_UNIT]->(newUnit)
            )
            RETURN s
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", service.getServiceId());
        params.put("serviceName", service.getServiceName());
        params.put("serviceType", service.getServiceType());
        params.put("quantity", service.getQuantity());
        params.put("price", service.getPrice());
        params.put("imgSource", service.getImgSource());
        params.put("unitId", service.getUnitPrice() != null ? service.getUnitPrice().getUnitId() : null);
        params.put("unitName", service.getUnitPrice() != null ? service.getUnitPrice().getUnitName() : null);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                SummaryCounters counters = result.consume().counters();
                return counters.propertiesSet() > 0 || counters.relationshipsCreated() > 0 || counters.relationshipsDeleted() > 0;
            });
        }
    }

    @Override
    public boolean increaseQuantity(String serviceId, int add) {
        String query = """
            MATCH (s:Service)
            WHERE s.serviceId = $serviceId
            SET s.quantity = coalesce(s.quantity, 0) + $add
            RETURN s
            """;

        Map<String, Object> params = Map.of(
                "serviceId", serviceId,
                "add", add
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean updateQuantity(String serviceId, int quantity) {
        String query = """
                MATCH (s:Service)
                WHERE s.serviceId = $serviceId
                SET s.quantity = $quantity
                RETURN s
                """;
        Map<String, Object> params = Map.of("serviceId", serviceId, "quantity", quantity);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean deleteById(String serviceId) {
        String query = """
            MATCH (s:Service)
            WHERE s.serviceId = $serviceId
              AND NOT EXISTS {
                  MATCH (:OrderDetailService)-[:FOR_SERVICE]->(s)
              }
            DETACH DELETE s
            RETURN COUNT(s) AS deletedCount
            """;
        Map<String, Object> params = Map.of("serviceId", serviceId);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                if (!result.hasNext()) {
                    return false;
                }
                return result.next().get("deletedCount").asInt() > 0;
            });
        }
    }
}