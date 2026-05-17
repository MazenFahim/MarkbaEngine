package com.markbaengine.dao;

import com.markbaengine.db.DBConnection;
import com.markbaengine.model.ReportDefinition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDao {

    public List<Map<String, Object>> runReport(ReportDefinition reportDefinition) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(reportDefinition.getSql())) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to run report: " + reportDefinition.getTitle(), ex);
        }
        return rows;
    }
}
