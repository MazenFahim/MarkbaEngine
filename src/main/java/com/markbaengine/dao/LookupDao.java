package com.markbaengine.dao;

import com.markbaengine.db.DBConnection;
import com.markbaengine.model.LookupOption;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads dropdown lookup options for foreign-key fields.
 */
public class LookupDao {

    public List<LookupOption> getOptions(String tableName, String idColumn, String labelColumn) {
        String sql = "SELECT "
                + idColumn + " AS lookup_id, "
                + labelColumn + " AS lookup_label "
                + "FROM " + tableName + " "
                + "ORDER BY lookup_label";

        List<LookupOption> options = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                options.add(new LookupOption(
                        resultSet.getObject("lookup_id"),
                        resultSet.getString("lookup_label")
                ));
            }

        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load lookup options", ex);
        }

        return options;
    }
}