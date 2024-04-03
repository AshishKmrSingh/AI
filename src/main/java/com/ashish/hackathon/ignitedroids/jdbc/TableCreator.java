package com.ashish.hackathon.ignitedroids.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableCreator {

    private static final Logger logger = LoggerFactory.getLogger(TableCreator.class);
    private static final Connection conn = HackathonJDBCConnection.getConnection();
    private static boolean isTableExists = false;

    public static boolean isTableExists() {
        return isTableExists;
    }

    public static void createTable() {
        logger.info("Checking to create data table");
        String sqlCreate = "CREATE TABLE IF NOT EXISTS DATATABLE"
                + " (past_release varchar(30),"
                + "files_changed bigint,"
                + "lines_added bigint,"
                + "lines_deleted bigint,"
                + "stage varchar(30),"
                + "bugs int(9))";

        logger.info("Checking to create data table with query {}", sqlCreate);

        try {
            if(conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sqlCreate);
                logger.info("Data table created");
                isTableExists = true;
            }
        }
        catch(SQLException sqlE) {
            logger.error("Could not create the data table", sqlE);
        }
    }
}
