package com.ashish.hackathon.ignitedroids.jdbc;

import com.ashish.hackathon.ignitedroids.model.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableOperations {

    private static final Logger logger = LoggerFactory.getLogger(TableOperations.class);
    private static final Connection connection = HackathonJDBCConnection.getConnection();
    private static final boolean isTableExists = TableCreator.isTableExists();

    public static void writeIntoDB(String toString) {
        if(isTableExists && connection != null) {
            String[] toStringArray = toString.split(",");
            long linesCh = Long.parseLong(toStringArray[1]);
            long linesAdd = Long.parseLong(toStringArray[2]);
            long linesDel = Long.parseLong(toStringArray[3]);
            int bugs = Integer.parseInt(toStringArray[5]);
            String query = "Insert into DATATABLE(past_release,files_changed,lines_added,lines_deleted,stage,bugs) values(?,?,?,?,?,?)";
            try {
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, toStringArray[0]);
                ps.setLong(2, linesCh);
                ps.setLong(3, linesAdd);
                ps.setLong(4, linesDel);
                ps.setString(5, toStringArray[4]);
                ps.setInt(6, bugs);

                ps.executeUpdate();
            }
            catch (SQLException e) {
                logger.error("Cannot insert a record into the database", e);
            }
        }
    }

    public static List<DataTable> getPersistedReleases() {
        List<DataTable> releaseList = new ArrayList<>();
        String query = "select * from DATATABLE";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while(rs.next()) {
                String release = rs.getString("last_release");
                long filesCh = rs.getInt("files_changed");
                long linesAdd = rs.getInt("lines_added");
                long linesDel = rs.getInt("lines_deleted");
                String stage = rs.getString("stage");
                int bugs = rs.getInt("bugs");

                DataTable tableObj = new DataTable();
                tableObj.setPast_release(release);
                tableObj.setFilesChanged(filesCh);
                tableObj.setLinesAdded(linesAdd);
                tableObj.setLinesDeleted(linesDel);
                tableObj.setStage(stage);
                tableObj.setBugs(bugs);

                releaseList.add(tableObj);

            }
        } catch (SQLException e) {
            logger.error("Cannot get the persisted releases from the database. Can cause performance issues");
        }
        return releaseList;
    }
}

