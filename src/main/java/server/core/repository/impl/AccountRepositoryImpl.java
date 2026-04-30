package server.core.repository.impl;

import common.entity.Account;
import common.entity.Employee;
import common.entity.EmployeeType;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.AccountRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountRepositoryImpl implements AccountRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public AccountRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        String query = """
                MATCH (e:Employee)-[:HAS_ACCOUNT]->(a:Account)
                WHERE a.username = $username AND a.password = $password
                RETURN a
                """;
        Map<String, Object> params = Map.of("username", username, "password", password);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Account> accounts = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("a").asNode().asMap(), Account.class))
                        .collect(Collectors.toList());

                return !accounts.isEmpty();
            });
        }
    }

    @Override
    public Account findByUsername(String username) {
        String query = """
                MATCH (e:Employee)-[:HAS_ACCOUNT]->(a:Account)
                MATCH (e)-[:BELONGS_TO]->(et:EmployeeType)
                WHERE a.username = $username
                RETURN a, e, et
                """;
        Map<String, Object> params = Map.of("username", username);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Account> accounts = result.list()
                        .stream()
                        .map(record -> {
                            Account account = mapper.toObject(record.get("a").asNode().asMap(), Account.class);
                            Employee employee = mapper.toObject(record.get("e").asNode().asMap(), Employee.class);
                            EmployeeType employeeType = mapper.toObject(record.get("et").asNode().asMap(), EmployeeType.class);

                            employee.setEmployeeType(employeeType);
                            account.setEmployee(employee);

                            return account;
                        })
                        .collect(Collectors.toList());

                return accounts.isEmpty() ? null : accounts.get(0);
            });
        }
    }

    @Override
    public boolean updatePasswordByUsername(String username, String newPassword) {
        String query = """
                MATCH (a:Account)
                WHERE a.username = $username
                SET a.password = $newPassword
                RETURN a
                """;
        Map<String, Object> params = Map.of("username", username, "newPassword", newPassword);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean updatePasswordByEmployeeId(String employeeId, String newPassword) {
        String query = """
                MATCH (e:Employee)-[:HAS_ACCOUNT]->(a:Account)
                WHERE e.employeeId = $employeeId
                SET a.password = $newPassword
                RETURN a
                """;
        Map<String, Object> params = Map.of("employeeId", employeeId, "newPassword", newPassword);

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }

    @Override
    public boolean existsByEmployeeIdAndPassword(String employeeId, String password) {
        String query = """
            MATCH (e:Employee)-[:HAS_ACCOUNT]->(a:Account)
            WHERE e.employeeId = $employeeId AND a.password = $password
            RETURN a
            """;

        Map<String, Object> params = Map.of(
                "employeeId", employeeId,
                "password", password
        );

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);
                return result.hasNext();
            });
        }
    }
}