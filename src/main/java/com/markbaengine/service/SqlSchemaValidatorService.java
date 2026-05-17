package com.markbaengine.service;

import com.markbaengine.model.SchemaValidationResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates that an imported SQL script matches the Urban Fleet schema expected
 * by MarkbaEngine. This is intentionally schema-specific, not a general SQL parser.
 */
public class SqlSchemaValidatorService {

    private static final Map<String, List<String>> REQUIRED_SCHEMA = new LinkedHashMap<>();

    static {
        REQUIRED_SCHEMA.put("depot", List.of("depot_id", "depot_name", "location", "capacity"));
        REQUIRED_SCHEMA.put("vehicle_model", List.of("model_id", "model_name", "manufacturer", "vehicle_type", "fuel_type"));
        REQUIRED_SCHEMA.put("vehicle", List.of("vehicle_id", "vehicle_code", "plate_number", "vehicle_name", "model_id", "depot_id", "status", "manufacture_year", "passenger_capacity", "current_mileage"));
        REQUIRED_SCHEMA.put("mechanic", List.of("mechanic_id", "mechanic_code", "mechanic_name", "specialization", "phone", "depot_id", "status"));
        REQUIRED_SCHEMA.put("supplier", List.of("supplier_id", "supplier_name", "phone", "email"));
        REQUIRED_SCHEMA.put("spare_part", List.of("part_id", "part_code", "part_name", "unit_cost", "stock_quantity", "supplier_id"));
        REQUIRED_SCHEMA.put("maintenance_log", List.of("log_id", "vehicle_id", "mechanic_id", "open_date", "close_date", "description", "status"));
        REQUIRED_SCHEMA.put("part_usage", List.of("usage_id", "log_id", "part_id", "quantity", "unit_cost_at_time"));
    }

    public SchemaValidationResult validateFile(Path sqlFile) {
        if (sqlFile == null) {
            return new SchemaValidationResult(false, List.of("No SQL file was selected."));
        }
        if (!Files.exists(sqlFile)) {
            return new SchemaValidationResult(false, List.of("The selected SQL file does not exist."));
        }
        if (!sqlFile.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".sql")) {
            return new SchemaValidationResult(false, List.of("Please select a file ending with .sql."));
        }

        try {
            return validateSql(Files.readString(sqlFile, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            return new SchemaValidationResult(false, List.of("Could not read the selected SQL file: " + ex.getMessage()));
        }
    }

    public SchemaValidationResult validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return new SchemaValidationResult(false, List.of("The SQL file is empty."));
        }

        String normalized = normalize(sql);
        List<String> messages = new ArrayList<>();
        int detectedTables = 0;

        for (Map.Entry<String, List<String>> entry : REQUIRED_SCHEMA.entrySet()) {
            String tableName = entry.getKey();
            String tableBody = findCreateTableBody(normalized, tableName);
            if (tableBody == null) {
                messages.add("Missing table: " + tableName);
                continue;
            }
            detectedTables++;
            for (String column : entry.getValue()) {
                if (!containsWord(tableBody, column)) {
                    messages.add("Missing column in " + tableName + ": " + column);
                }
            }
        }

        if (messages.isEmpty()) {
            messages.add("Valid Urban Fleet schema detected. Tables found: " + detectedTables + "/" + REQUIRED_SCHEMA.size() + ".");
            messages.add("The file can be used as a MarkbaEngine workspace.");
            return new SchemaValidationResult(true, messages);
        }

        messages.add(0, "Invalid Urban Fleet schema. Tables found: " + detectedTables + "/" + REQUIRED_SCHEMA.size() + ".");
        return new SchemaValidationResult(false, messages);
    }

    public List<String> getRequiredTableNames() {
        return List.copyOf(REQUIRED_SCHEMA.keySet());
    }

    private String normalize(String sql) {
        String withoutBlockComments = sql.replaceAll("(?s)/\\*.*?\\*/", " ");
        String withoutLineComments = withoutBlockComments.replaceAll("(?m)--.*$", " ");
        return withoutLineComments
                .replace('[', ' ')
                .replace(']', ' ')
                .replace('`', ' ')
                .replace('"', ' ')
                .toLowerCase(Locale.ROOT);
    }

    private String findCreateTableBody(String normalizedSql, String tableName) {
        Pattern pattern = Pattern.compile("create\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?(?:dbo\\s*\\.\\s*)?" + Pattern.quote(tableName) + "\\s*\\(", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(normalizedSql);
        if (!matcher.find()) {
            return null;
        }

        int bodyStart = matcher.end();
        int depth = 1;
        for (int i = bodyStart; i < normalizedSql.length(); i++) {
            char ch = normalizedSql.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
                if (depth == 0) {
                    return normalizedSql.substring(bodyStart, i);
                }
            }
        }
        return null;
    }

    private boolean containsWord(String text, String word) {
        return Pattern.compile("(?<![a-z0-9_])" + Pattern.quote(word.toLowerCase(Locale.ROOT)) + "(?![a-z0-9_])").matcher(text).find();
    }
}
