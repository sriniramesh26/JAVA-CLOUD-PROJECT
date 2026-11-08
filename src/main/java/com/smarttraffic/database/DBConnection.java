package com.smarttraffic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC Database Connection Manager.
 * Reads connection settings dynamically from environment variables (DB_URL, DB_USER, DB_PASSWORD).
 * Supports local MySQL 8.4 LTS as well as OCI Remote Database Host.
 */
public final class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    private DBConnection() {
        // Prevent instantiation
    }

    public static String getDbUrl() {
        String env = System.getenv("DB_URL");
        if (env != null && !env.isBlank()) return env.trim();
        String prop = System.getProperty("DB_URL");
        if (prop != null && !prop.isBlank()) return prop.trim();
        return DEFAULT_URL;
    }

    public static String getDbUser() {
        String env = System.getenv("DB_USER");
        if (env != null && !env.isBlank()) return env.trim();
        String prop = System.getProperty("DB_USER");
        if (prop != null && !prop.isBlank()) return prop.trim();
        return DEFAULT_USER;
    }

    public static String getDbPassword() {
        String env = System.getenv("DB_PASSWORD");
        if (env != null) return env;
        String prop = System.getProperty("DB_PASSWORD");
        if (prop != null) return prop;
        return DEFAULT_PASS;
    }

    /**
     * Establishes and returns a new JDBC Connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    /**
     * Non-throwing connection test for health checks.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Returns masked diagnostic connection information.
     */
    public static String getConnectionInfo() {
        String url = getDbUrl();
        String user = getDbUser();
        return String.format("URL: %s | User: %s", url, user);
    }
}
