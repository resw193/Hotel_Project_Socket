package server.core.repository.impl;

import common.entity.Employee;
import common.entity.EmployeeType;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.EmployeeRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public EmployeeRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public Employee findById(String employeeId) {
        String query = """
                MATCH (e:Employee)-[:BELONGS_TO]->(et:EmployeeType)
                WHERE e.employeeId = $employeeId
                RETURN e, et
                """;
        Map<String, Object> params = Map.of("employeeId", employeeId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Employee> employees = result.list()
                        .stream()
                        .map(record -> {
                            Employee employee = mapper.toObject(record.get("e").asNode().asMap(), Employee.class);
                            EmployeeType employeeType = mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class);

                            employee.setEmployeeType(employeeType);
                            return employee;
                        })
                        .collect(Collectors.toList());

                return employees.isEmpty() ? null : employees.get(0);
            });
        }
    }

    @Override
    public Employee findByEmail(String email) {
        String query = """
                MATCH (e:Employee)-[:BELONGS_TO]->(et:EmployeeType)
                WHERE e.email = $email
                RETURN e, et
                """;
        Map<String, Object> params = Map.of("email", email);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Employee> employees = result.list()
                        .stream()
                        .map(record -> {
                            Employee employee = mapper.toObject(record.get("e").asNode().asMap(), Employee.class);
                            EmployeeType employeeType = mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class);

                            employee.setEmployeeType(employeeType);
                            return employee;
                        })
                        .collect(Collectors.toList());

                return employees.isEmpty() ? null : employees.get(0);
            });
        }
    }

    @Override
    public List<Employee> findAll() {
        String query = """
                MATCH (e:Employee)-[:BELONGS_TO]->(et:EmployeeType)
                RETURN e, et
                ORDER BY toInteger(right(e.employeeId, 2))
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> {
                            Employee employee = mapper.toObject(record.get("e").asNode().asMap(), Employee.class);
                            EmployeeType employeeType = mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class);

                            employee.setEmployeeType(employeeType);
                            return employee;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Employee> findByTypeName(String typeName) {
        String query = """
                MATCH (e:Employee)-[:BELONGS_TO]->(et:EmployeeType)
                WHERE et.typeName = $typeName
                RETURN e, et
                ORDER BY toInteger(right(e.employeeId, 2))
                """;
        Map<String, Object> params = Map.of("typeName", typeName);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> {
                            Employee employee = mapper.toObject(record.get("e").asNode().asMap(), Employee.class);
                            EmployeeType employeeType = mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class);

                            employee.setEmployeeType(employeeType);
                            return employee;
                        })
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public boolean add(Employee employee) {
        String query = """
                MATCH (et:EmployeeType {typeId: $typeId})
                CREATE (e:Employee {
                    employeeId: $employeeId,
                    fullName: $fullName,
                    phone: $phone,
                    email: $email,
                    imgSource: $imgSource,
                    gender: $gender
                })
                MERGE (e)-[:BELONGS_TO]->(et)
                RETURN e
                """;
        Map<String, Object> params = Map.of(
                "employeeId", employee.getEmployeeId(),
                "fullName", employee.getFullName(),
                "phone", employee.getPhone(),
                "email", employee.getEmail(),
                "imgSource", employee.getImgSource(),
                "gender", employee.isGender(),
                "typeId", employee.getEmployeeType().getTypeId()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().nodesCreated() > 0;
            });
        }
    }

    @Override
    public boolean updateProfile(Employee employee) {
        String query = """
                MATCH (e:Employee)-[r:BELONGS_TO]->(:EmployeeType)
                WHERE e.employeeId = $employeeId
                MATCH (newType:EmployeeType {typeId: $typeId})
                SET e.fullName = $fullName,
                    e.phone = $phone,
                    e.email = $email,
                    e.gender = $gender,
                    e.imgSource = $imgSource
                DELETE r
                MERGE (e)-[:BELONGS_TO]->(newType)
                RETURN e
                """;
        Map<String, Object> params = Map.of(
                "employeeId", employee.getEmployeeId(),
                "fullName", employee.getFullName(),
                "phone", employee.getPhone(),
                "email", employee.getEmail(),
                "gender", employee.isGender(),
                "imgSource", employee.getImgSource(),
                "typeId", employee.getEmployeeType().getTypeId()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean updateAvatar(String employeeId, String imgSource) {
        String query = """
                MATCH (e:Employee)
                WHERE e.employeeId = $employeeId
                SET e.imgSource = $imgSource
                RETURN e
                """;
        Map<String, Object> params = Map.of("employeeId", employeeId, "imgSource", imgSource);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean deleteById(String employeeId) {
        String query = """
                MATCH (e:Employee)-[r:BELONGS_TO]->(:EmployeeType)
                WHERE e.employeeId = $employeeId
                  AND NOT (e)-[:HAS_ACCOUNT]->(:Account)
                  AND NOT (:Order)-[:CREATED_BY]->(e)
                  AND NOT (:DashboardNote)-[:UPDATED_BY]->(e)
                DELETE r, e
                RETURN e
                """;
        Map<String, Object> params = Map.of("employeeId", employeeId);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);

                return result.consume().counters().nodesDeleted() > 0;
            });
        }
    }
}