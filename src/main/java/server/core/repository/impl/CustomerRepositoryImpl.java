package server.core.repository.impl;

import common.entity.Customer;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.CustomerRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomerRepositoryImpl implements CustomerRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public CustomerRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public List<Customer> findAll() {
        String query = """
                MATCH (c:Customer)
                RETURN c
                ORDER BY c.customerId
                """;

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);

                return result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("c").asNode().asMap(), Customer.class))
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Customer> findByMinLoyalty(int loyaltyPoints) {
        String query = """
                MATCH (c:Customer)
                WHERE c.loyaltyPoint >= $loyaltyPoints
                RETURN c
                ORDER BY c.customerId
                """;
        Map<String, Object> params = Map.of("loyaltyPoints", loyaltyPoints);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("c").asNode().asMap(), Customer.class))
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public List<Customer> findByNameContaining(String keyword) {
        String query = """
                MATCH (c:Customer)
                WHERE toLower(c.fullName) CONTAINS toLower($keyword)
                RETURN c
                ORDER BY c.customerId
                """;
        Map<String, Object> params = Map.of("keyword", keyword);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                return result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("c").asNode().asMap(), Customer.class))
                        .collect(Collectors.toList());
            });
        }
    }

    @Override
    public Customer findById(String customerId) {
        String query = """
                MATCH (c:Customer)
                WHERE c.customerId = $customerId
                RETURN c
                """;
        Map<String, Object> params = Map.of("customerId", customerId);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Customer> customers = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("c").asNode().asMap(), Customer.class))
                        .collect(Collectors.toList());

                return customers.isEmpty() ? null : customers.get(0);
            });
        }
    }

    @Override
    public Customer findByPhone(String phone) {
        String query = """
                MATCH (c:Customer)
                WHERE c.phone = $phone
                RETURN c
                """;
        Map<String, Object> params = Map.of("phone", phone);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Customer> customers = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("c").asNode().asMap(), Customer.class))
                        .collect(Collectors.toList());

                return customers.isEmpty() ? null : customers.get(0);
            });
        }
    }

    @Override
    public Customer findByEmail(String email) {
        String query = """
                MATCH (c:Customer)
                WHERE c.email = $email
                RETURN c
                """;
        Map<String, Object> params = Map.of("email", email);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Customer> customers = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("c").asNode().asMap(), Customer.class))
                        .collect(Collectors.toList());

                return customers.isEmpty() ? null : customers.get(0);
            });
        }
    }

    @Override
    public Customer findByIdCard(String idCard) {
        String query = """
                MATCH (c:Customer)
                WHERE c.idCard = $idCard
                RETURN c
                """;
        Map<String, Object> params = Map.of("idCard", idCard);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<Customer> customers = result.list()
                        .stream()
                        .map(record -> mapper.toObject(record.get("c").asNode().asMap(), Customer.class))
                        .collect(Collectors.toList());

                return customers.isEmpty() ? null : customers.get(0);
            });
        }
    }

    @Override
    public boolean add(Customer customer) {
        String query = """
                CREATE (c:Customer {
                    customerId: $customerId,
                    fullName: $fullName,
                    phone: $phone,
                    email: $email,
                    regisDate: $regisDate,
                    idCard: $idCard,
                    loyaltyPoint: $loyaltyPoint
                })
                RETURN c
                """;
        Map<String, Object> params = Map.of(
                "customerId", customer.getCustomerId(),
                "fullName", customer.getFullName(),
                "phone", customer.getPhone(),
                "email", customer.getEmail(),
                "regisDate", customer.getRegisDate(),
                "idCard", customer.getIdCard(),
                "loyaltyPoint", customer.getLoyaltyPoint()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().nodesCreated() > 0;
            });
        }
    }

    @Override
    public boolean update(Customer customer) {
        String query = """
                MATCH (c:Customer)
                WHERE c.customerId = $customerId
                SET c.fullName = $fullName,
                    c.phone = $phone,
                    c.email = $email
                RETURN c
                """;
        Map<String, Object> params = Map.of(
                "customerId", customer.getCustomerId(),
                "fullName", customer.getFullName(),
                "phone", customer.getPhone(),
                "email", customer.getEmail()
        );

        try (Session session = connManager.openSession()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(query, params);
                return result.consume().counters().propertiesSet() > 0;
            });
        }
    }
}