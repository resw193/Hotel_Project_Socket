package server.infrastructure.db;

import org.neo4j.driver.*;

public class Neo4jConnManager implements AutoCloseable {

    public static final String DEFAULT_URI = "neo4j://127.0.0.1:7687";
    public static final String DEFAULT_USERNAME = "neo4j";
    public static final String DEFAULT_PASSWORD = "19032323";
    public static final String DEFAULT_DB_NAME = "hotel";

    private final Driver driver;
    private final String dbName;

    public Neo4jConnManager() {
        this(DEFAULT_URI, DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_DB_NAME);
    }

    public Neo4jConnManager(String uri, String username, String password, String dbName) {
        this.dbName = dbName;
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
        this.driver.verifyConnectivity();
    }

    public Session openSession() {
        return this.driver.session(SessionConfig.forDatabase(dbName));
    }

    public String getDbName() {
        return dbName;
    }

    @Override
    public void close() {
        this.driver.close();
    }
}