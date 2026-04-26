package server.core.repository.impl;

import common.entity.EmployeeType;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.EmployeeTypeRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeTypeRepositoryImpl implements EmployeeTypeRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public EmployeeTypeRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public EmployeeType findById(String typeId) {
        String query = """
                MATCH (et:EmployeeType)
                WHERE et.typeId = $typeId
                RETURN et
                """;
        Map<String, Object> params = Map.of("typeId", typeId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<EmployeeType> employeeTypes = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class))
                        .collect(Collectors.toList());

                return employeeTypes.isEmpty() ? null : employeeTypes.get(0);
            });
        }
    }

    @Override
    public EmployeeType findByTypeName(String typeName) {
        String query = """
                MATCH (et:EmployeeType)
                WHERE et.typeName = $typeName
                RETURN et
                """;
        Map<String, Object> params = Map.of("typeName", typeName);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<EmployeeType> employeeTypes = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class))
                        .collect(Collectors.toList());

                return employeeTypes.isEmpty() ? null : employeeTypes.get(0);
            });
        }
    }

    @Override
    public List<EmployeeType> findAll() {
        String query = """
                MATCH (et:EmployeeType)
                RETURN et
                ORDER BY et.typeId
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class))
                        .collect(Collectors.toList());
            });
        }
    }
}