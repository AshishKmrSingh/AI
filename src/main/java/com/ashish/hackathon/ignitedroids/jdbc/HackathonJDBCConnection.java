package com.ashish.hackathon.ignitedroids.jdbc;

import com.ashish.hackathon.ignitedroids.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class HackathonJDBCConnection {

    private static final Logger logger = LoggerFactory.getLogger(HackathonJDBCConnection.class);

    private HackathonJDBCConnection(){
        throw new UnsupportedOperationException("Cannot create another instance of JDBC connection");
    }
    private static final String DB_URL = ConfigReader.getProjectConfig().getDatabase().getDbUrl();
    private static final String USER = ConfigReader.getProjectConfig().getDatabase().getUser();
    private static final String PASS = ConfigReader.getProjectConfig().getDatabase().getPassword();
    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static void createConnection() {
        // Open a connection
        try {
            logger.info("Creating JDBC connection");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            logger.info("Created JDBC connection");
        } catch (SQLException e) {
            logger.error("cannot create database connection to {} with user {}", DB_URL, USER, e);
        }
    }

    public static void closeConnection() {

        try {
            connection.close();
        }
        catch(Exception e) {
            logger.error("Could not close database connection", e);
        }
    }
}
