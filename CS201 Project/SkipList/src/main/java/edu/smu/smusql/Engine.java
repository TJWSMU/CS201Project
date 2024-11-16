package edu.smu.smusql;

import java.util.*;
import java.util.stream.Collectors;

public class Engine {
    private Map<String, Table> tables;

    public Engine() {
        this.tables = new HashMap<>();
    }

    public String executeSQL(String query) {
        String[] tokens = parseSQLQuery(query);
        String command = tokens[0].toUpperCase();

        switch (command) {
            case "CREATE":
                return create(tokens);
            case "INSERT":
                return insert(tokens);
            case "SELECT":
                return select(query, tokens);
            case "UPDATE":
                return update(query, tokens);
            case "DELETE":
                return delete(query, tokens);
            default:
                return "ERROR: Unknown command";
        }
    }

    private String[] parseSQLQuery(String query) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : query.toCharArray()) {
            if (c == '\'' || c == '\"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        
        return tokens.toArray(new String[0]);
    }

    private String create(String[] tokens) {
        String tableName = tokens[2];
        String columnsStr = String.join(" ", Arrays.copyOfRange(tokens, 3, tokens.length))
                                .replaceAll("[()]", "");
        List<String> columns = Arrays.asList(columnsStr.split(",\\s*"));
        
        tables.put(tableName, new Table(tableName, columns));
        return "Table " + tableName + " created";
    }

    private String insert(String[] tokens) {
        String tableName = tokens[2];
        Table table = tables.get(tableName);
        
        String valuesStr = String.join(" ", Arrays.copyOfRange(tokens, 4, tokens.length))
                                .replaceAll("[()]", "")
                                .replaceAll("'", "");
        String[] values = valuesStr.split(",\\s*");
        
        Map<String, String> columnData = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            columnData.put(table.getColumns().get(i), values[i]);
        }
        
        table.getData().insert(columnData);
        return "Row inserted into " + tableName;
    }

    private String select(String query, String[] tokens) {
        String tableName = tokens[3];
        Table table = tables.get(tableName);
        
        List<Map<String, String>> results;
        if (query.toUpperCase().contains("WHERE")) {
            WhereClause whereClause = parseWhereClause(query);
            results = table.getData().search(whereClause);
        } else {
            results = table.getData().getAllRecords();
        }
        
        return formatResults(results, table.getColumns());
    }

    private String update(String query, String[] tokens) {
        String tableName = tokens[1];
        Table table = tables.get(tableName);
        
        WhereClause whereClause = parseWhereClause(query);
        String setClause = query.substring(query.indexOf("SET") + 4, query.indexOf("WHERE")).trim();
        String[] setParts = setClause.split("=");
        String column = setParts[0].trim();
        String value = setParts[1].trim().replaceAll("'", "");
        
        int rowsAffected = table.getData().update(column, value, whereClause);
        return "Table " + tableName + " updated. " + rowsAffected + " rows affected.";
    }

    private String delete(String query, String[] tokens) {
        String tableName = tokens[2];
        Table table = tables.get(tableName);
        
        WhereClause whereClause = parseWhereClause(query);
        int rowsAffected = table.getData().delete(whereClause);
        return "Rows deleted from " + tableName + ". " + rowsAffected + " rows affected.";
    }

    private WhereClause parseWhereClause(String query) {
        // Extract WHERE clause
        int whereIndex = query.toUpperCase().indexOf("WHERE");
        if (whereIndex == -1) return null;
        
        String whereStr = query.substring(whereIndex + 6);
        WhereClause clause = new WhereClause();
        
        // Handle AND/OR conditions
        if (whereStr.contains("AND")) {
            clause.type = "AND";
            String[] conditions = whereStr.split("AND");
            clause.conditions = Arrays.stream(conditions)
                                    .map(this::parseCondition)
                                    .collect(Collectors.toList());
        } else if (whereStr.contains("OR")) {
            clause.type = "OR";
            String[] conditions = whereStr.split("OR");
            clause.conditions = Arrays.stream(conditions)
                                    .map(this::parseCondition)
                                    .collect(Collectors.toList());
        } else {
            clause.type = "SINGLE";
            clause.conditions = Collections.singletonList(parseCondition(whereStr));
        }
        
        return clause;
    }

    private Condition parseCondition(String condStr) {
        condStr = condStr.trim();
        Condition cond = new Condition();
        
        if (condStr.contains(">=")) {
            cond.operator = ">=";
        } else if (condStr.contains("<=")) {
            cond.operator = "<=";
        } else if (condStr.contains(">")) {
            cond.operator = ">";
        } else if (condStr.contains("<")) {
            cond.operator = "<";
        } else if (condStr.contains("=")) {
            cond.operator = "=";
        }
        
        String[] parts = condStr.split(cond.operator);
        cond.column = parts[0].trim();
        cond.value = parts[1].trim().replaceAll("'", "");
        
        return cond;
    }

    private String formatResults(List<Map<String, String>> results, List<String> columns) {
        if (results.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(" ", columns));
        
        for (Map<String, String> row : results) {
            sb.append(" ");
            for (String column : columns) {
                sb.append(row.get(column)).append(" ");
            }
        }
        
        return sb.toString().trim();
    }
}
