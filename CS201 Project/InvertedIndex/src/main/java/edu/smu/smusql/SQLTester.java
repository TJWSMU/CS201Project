package edu.smu.smusql;

public class SQLTester {
    private static Engine dbEngine;
    
    public static void main(String[] args) {
        dbEngine = new Engine();
        runBasicTests();
        runComplexTests();
        //runPerformanceTests();
    }
    
    private static void runBasicTests() {
        System.out.println("=== Running Basic Tests ===");
        
        // Create tables
        executeAndPrint("CREATE TABLE Products (id INTEGER, name STRING, price DOUBLE, category STRING)");
        executeAndPrint("CREATE TABLE Users (id INTEGER, name STRING, age INTEGER, city STRING)");
        
        // Insert test data
        executeAndPrint("INSERT INTO Products VALUES (1, 'Laptop', 999.99, 'Electronics')");
        executeAndPrint("INSERT INTO Products VALUES (2, 'Smartphone', 599.99, 'Electronics')");
        executeAndPrint("INSERT INTO Products VALUES (3, 'Desk Chair', 199.99, 'Furniture')");
        executeAndPrint("INSERT INTO Products VALUES (4, 'Coffee Maker', 79.99, 'Appliances')");
        executeAndPrint("INSERT INTO Products VALUES (5, 'Headphones', 149.99, 'Electronics')");
        
        executeAndPrint("INSERT INTO Users VALUES (1, 'John', 25, 'New York')");
        executeAndPrint("INSERT INTO Users VALUES (2, 'Alice', 30, 'Los Angeles')");
        executeAndPrint("INSERT INTO Users VALUES (3, 'Bob', 35, 'Chicago')");
        executeAndPrint("INSERT INTO Users VALUES (4, 'Eve', 28, 'New York')");
        executeAndPrint("INSERT INTO Users VALUES (5, 'Charlie', 40, 'Boston')");
        
        // Basic SELECT tests
        executeAndPrint("SELECT * FROM Products");
        executeAndPrint("SELECT * FROM Products WHERE category = 'Electronics'");
        executeAndPrint("SELECT * FROM Users WHERE city = 'New York'");
    }
    
    private static void runComplexTests() {
        System.out.println("\n=== Running Complex Tests ===");
        
        // Complex SELECT tests with AND/OR
        executeAndPrint("SELECT * FROM Products WHERE price > 100 AND category = 'Electronics'");
        executeAndPrint("SELECT * FROM Users WHERE city = 'New York' OR age > 35");
        executeAndPrint("SELECT * FROM Products WHERE price >= 200 AND price <= 1000");
        
        // UPDATE tests
        executeAndPrint("UPDATE Products SET price = 649.99 WHERE id = 2");
        executeAndPrint("UPDATE Users SET city = 'Silicon Valley' WHERE age > 30");
        executeAndPrint("SELECT * FROM Products"); // Verify updates
        executeAndPrint("SELECT * FROM Users"); // Verify updates
        
        // Complex UPDATE with multiple conditions
        executeAndPrint("UPDATE Products SET price = 100000 WHERE category = 'Electronics' AND price < 500");
        executeAndPrint("SELECT * FROM Products"); // Verify updates
        
        // DELETE tests
        executeAndPrint("DELETE FROM Products WHERE price > 800");
        executeAndPrint("DELETE FROM Users WHERE city = 'New York' OR age > 35");
        executeAndPrint("SELECT * FROM Products"); // Verify deletions
        executeAndPrint("SELECT * FROM Users"); // Verify deletions
    }
    
    private static void runPerformanceTests() {
        System.out.println("\n=== Running Performance Tests ===");
        
        // Bulk insert test
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= 1000; i++) {
            String query = String.format("INSERT INTO Products VALUES (%d, 'Product%d', %f, 'Category%d')",
                    i, i, 100.0 + i, i % 5);
            dbEngine.executeSQL(query);
        }
        printExecutionTime("Bulk Insert (1000 rows)", startTime);
        
        // Range query performance
        startTime = System.currentTimeMillis();
        executeAndPrint("SELECT * FROM Products WHERE price > 200 AND price < 500");
        printExecutionTime("Range Query", startTime);
        
        // Complex update performance
        startTime = System.currentTimeMillis();
        executeAndPrint("UPDATE Products SET price = 100000 WHERE price > 200 AND price < 800");
        printExecutionTime("Complex Update", startTime);
        
        // OR condition performance
        startTime = System.currentTimeMillis();
        executeAndPrint("SELECT * FROM Products WHERE category = 'Category1' OR category = 'Category2' OR category = 'Category3'");
        printExecutionTime("OR Condition Query", startTime);
    }
    
    private static void executeAndPrint(String query) {
        System.out.println("\nExecuting: " + query);
        System.out.println("Result: " + dbEngine.executeSQL(query));
    }
    
    private static void printExecutionTime(String operation, long startTime) {
        long endTime = System.currentTimeMillis();
        System.out.printf("%s took %d ms%n", operation, endTime - startTime);
    }
}