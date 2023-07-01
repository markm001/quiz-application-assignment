package main.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class DatabaseConnector {
    private static String url;
    private static String username;
    private static String password;

    private static Connection connection;

    private static final DatabaseConnector INSTANCE = new DatabaseConnector();
    private static boolean autoCommit = true;

    private DatabaseConnector() {
        try{
            HashMap<String, String> propertiesMap = ReaderUtil.readProperties("resources/application.config");
            url = propertiesMap.get("URL");
            username = propertiesMap.get("USERNAME");
            password = propertiesMap.get("PASSWORD");

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the autoCommit Mode when acquiring a connection
     * @param autoCommit enabled or disabled value
     */
    public static void setAutoCommit(boolean autoCommit) {
        DatabaseConnector.autoCommit = autoCommit;
    }

    /**
     * Establish a database connection via DriverManager
     * @return A valid connection
     * @throws SQLException if connection to database was not established due to faulty credentials
     */
    public static Connection getConnection() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    /**
     * Closes and open connection. Not required when getConnection() auto-closable is used.
     * @throws SQLException If no valid connection is currently open.
     */
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    /**
     * Rolls back any changes done to the database before a transaction occurs.
     * @throws SQLException If the database connection was not established properly.
     */
    public static void rollbackChanges() throws SQLException {
        connection.rollback();
    }

    public static DatabaseConnector getInstance() {
        return INSTANCE;
    }
}
