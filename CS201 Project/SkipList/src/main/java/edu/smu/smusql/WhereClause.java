package edu.smu.smusql;

import java.util.List;

public class WhereClause {
    String type;  // "AND", "OR", "SINGLE"
    List<Condition> conditions;
} 