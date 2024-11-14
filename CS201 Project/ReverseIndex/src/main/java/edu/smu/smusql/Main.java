package edu.smu.smusql;

import java.util.*;

public class Main {
    // Main method to run the simple database engine
    static Engine dbEngine = new Engine();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("smuSQL version 0.5.1 2024-09-20");
        System.out.println("sample implementation for reference only");

        while (true) {
            System.out.print("smusql> ");
            String query = scanner.nextLine();
            if (query.equalsIgnoreCase("exit")) {
                break;
            } else if (query.equalsIgnoreCase("evaluateCorrectness")) {
                long startTime = System.nanoTime();
                evaluateCorrectness();
                long stopTime = System.nanoTime();
                long elapsedTime = stopTime - startTime;
                double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                System.out.println("Time elapsed: " + elapsedTimeInSecond + " seconds");
                break;
            } else if (query.equalsIgnoreCase("evaluate")) {
                long startTime = System.nanoTime();
                DatabaseAutoEvaluator.autoEvaluate();
                long stopTime = System.nanoTime();
                long elapsedTime = stopTime - startTime;
                double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                System.out.println("Time elapsed: " + elapsedTimeInSecond + " seconds");
                break;
            }

            System.out.println(dbEngine.executeSQL(query));
        }
        scanner.close();
    }

    public static void evaluateCorrectness() {
        System.out.println("Evaluating correctness of the Engine...");
    
        // Reset the Engine's state
        dbEngine = new Engine();
    
        // Inner class to represent a test case
        class TestCase {
            String description;
            List<String> commands;
            List<String> expectedOutputs;
    
            public TestCase(String description, List<String> commands, List<String> expectedOutputs) {
                this.description = description;
                this.commands = commands;
                this.expectedOutputs = expectedOutputs;
            }
        }
    
        List<TestCase> testCases = new ArrayList<>();
    
        // Test Case 1: Create table and insert data
        testCases.add(new TestCase(
            "Test Case 1: Create table and insert data",
            Arrays.asList(
                "CREATE TABLE Employees (ID INTEGER, Name STRING, Age INTEGER, Department STRING)",
                "INSERT INTO Employees VALUES (1, 'Alice', 30, 'HR')",
                "INSERT INTO Employees VALUES (2, 'Bob', 25, 'Engineering')",
                "INSERT INTO Employees VALUES (3, 'Charlie', 35, 'HR')",
                "INSERT INTO Employees VALUES (4, 'David', 28, 'Engineering')",
                "INSERT INTO Employees VALUES (5, 'Eve', 45, 'Finance')",
                "SELECT * FROM Employees"
            ),
            Arrays.asList(
                "Table Employees created",
                "Row inserted into Employees",
                "Row inserted into Employees",
                "Row inserted into Employees",
                "Row inserted into Employees",
                "Row inserted into Employees",
                "ID\tName\tAge\tDepartment\n" +
                "1\tAlice\t30\tHR\n" +
                "2\tBob\t25\tEngineering\n" +
                "3\tCharlie\t35\tHR\n" +
                "4\tDavid\t28\tEngineering\n" +
                "5\tEve\t45\tFinance\n"
            )
        ));
    
        // Test Case 2: Select with WHERE clause using AND
        testCases.add(new TestCase(
            "Test Case 2: Select with WHERE clause using AND",
            Arrays.asList(
                "SELECT * FROM Employees WHERE Department = 'Engineering' AND Age > 26"
            ),
            Arrays.asList(
                "ID\tName\tAge\tDepartment\n" +
                "4\tDavid\t28\tEngineering\n"
            )
        ));
    
        // Test Case 3: Select with WHERE clause using OR
        testCases.add(new TestCase(
            "Test Case 3: Select with WHERE clause using OR",
            Arrays.asList(
                "SELECT * FROM Employees WHERE Department = 'HR' OR Age > 40"
            ),
            Arrays.asList(
                "ID\tName\tAge\tDepartment\n" +
                "1\tAlice\t30\tHR\n" +
                "3\tCharlie\t35\tHR\n" +
                "5\tEve\t45\tFinance\n"
            )
        ));
    
        // Test Case 4: Update a single row
        testCases.add(new TestCase(
            "Test Case 4: Update a single row",
            Arrays.asList(
                "UPDATE Employees SET Department = 'Management' WHERE Name = 'David'",
                "SELECT * FROM Employees WHERE Name = 'David'"
            ),
            Arrays.asList(
                "Table Employees updated. 1 rows affected.",
                "ID\tName\tAge\tDepartment\n" +
                "4\tDavid\t28\tManagement\n"
            )
        ));
    
        // Test Case 5: Update multiple rows
        testCases.add(new TestCase(
            "Test Case 5: Update multiple rows",
            Arrays.asList(
                "UPDATE Employees SET Department = 'Sales' WHERE Age > 30",
                "SELECT * FROM Employees WHERE Department = 'Sales'"
            ),
            Arrays.asList(
                "Table Employees updated. 2 rows affected.",
                "ID\tName\tAge\tDepartment\n" +
                "3\tCharlie\t35\tSales\n" +
                "5\tEve\t45\tSales\n"
            )
        ));
    
        // Test Case 6: Delete a single row
        testCases.add(new TestCase(
            "Test Case 6: Delete a single row",
            Arrays.asList(
                "DELETE FROM Employees WHERE Name = 'Bob'",
                "SELECT * FROM Employees"
            ),
            Arrays.asList(
                "Rows deleted from Employees. 1 rows affected.",
                "ID\tName\tAge\tDepartment\n" +
                "1\tAlice\t30\tHR\n" +
                "3\tCharlie\t35\tSales\n" +
                "4\tDavid\t28\tManagement\n" +
                "5\tEve\t45\tSales\n"
            )
        ));
    
        // Test Case 7: Delete multiple rows
        testCases.add(new TestCase(
            "Test Case 7: Delete multiple rows",
            Arrays.asList(
                "DELETE FROM Employees WHERE Department = 'Sales'",
                "SELECT * FROM Employees"
            ),
            Arrays.asList(
                "Rows deleted from Employees. 2 rows affected.",
                "ID\tName\tAge\tDepartment\n" +
                "1\tAlice\t30\tHR\n" +
                "4\tDavid\t28\tManagement\n"
            )
        ));
    
        // Now, for each test case, execute the commands and compare outputs
        for (TestCase testCase : testCases) {
            System.out.println("\nRunning " + testCase.description);
    
            for (int i = 0; i < testCase.commands.size(); i++) {
                String command = testCase.commands.get(i);
                String expectedOutput = testCase.expectedOutputs.get(i);
    
                String actualOutput = dbEngine.executeSQL(command);
    
                String normalizedExpected = expectedOutput.replaceAll("\\s+", " ").trim();
                String normalizedActual = actualOutput.replaceAll("\\s+", " ").trim();

                if (normalizedActual.equals(normalizedExpected)) {
                    System.out.println("Command: " + command);
                    System.out.println("Success: Output matches expected output.");
                } else {
                    System.out.println("Command: " + command);
                    System.out.println("Failure: Output does not match expected output.");
                    System.out.println("Expected Output:\n[" + normalizedExpected + "]");
                    System.out.println("Actual Output:\n[" + normalizedActual + "]");
                }

            }
        }
    
        System.out.println("\nEvaluation completed.");
    }
    
    public class DatabaseAutoEvaluator {

        private static final int NUMBER_OF_QUERIES = 40000;
        private static final String[] QUERY_TYPES = {"INSERT", "SELECT", "UPDATE", "DELETE"};
        private static final int QUERIES_PER_TYPE = NUMBER_OF_QUERIES / QUERY_TYPES.length;
        private static final Random RANDOM = new Random();

        private static final boolean TEST_WITH_COMPLEX = true;

        // Counters to track the number of rows in each table
        private static int userCount = 50;
        private static int productCount = 50;
        private static int orderCount = 50;
    
        public static void autoEvaluate() {
            initializeTables();
            prepopulateTables();
    
            for (int queryType = 0; queryType < QUERY_TYPES.length; queryType++) {
                long startTime = System.nanoTime();
    
                processQueries(queryType);
    
                double elapsedTimeInSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
                System.out.printf("Processed %d %s queries in %.2f seconds%n", QUERIES_PER_TYPE, QUERY_TYPES[queryType], elapsedTimeInSeconds);
            }
    
            System.out.printf("Finished processing %d queries in total.%n", NUMBER_OF_QUERIES);
        }
    
        private static void initializeTables() {
            dbEngine.executeSQL("CREATE TABLE users (id INTEGER, name STRING, age INTEGER, city STRING)");
            dbEngine.executeSQL("CREATE TABLE products (id INTEGER, name STRING, price DOUBLE, category STRING)");
            dbEngine.executeSQL("CREATE TABLE orders (id INTEGER, user_id STRING, product_id STRING, quantity INTEGER)");
        }
    
        private static void prepopulateTables() {
            System.out.println("Prepopulating tables...");
            for (int i = 0; i < 50; i++) {
                dbEngine.executeSQL(String.format("INSERT INTO users VALUES (%d, 'User%d', %d, '%s')", i, i, 20 + (i % 41), getRandomCity()));
                dbEngine.executeSQL(String.format("INSERT INTO products VALUES (%d, 'Product%d', %.2f, '%s')", i, i, 10 + (double)(i % 990), getRandomCategory()));
                dbEngine.executeSQL(String.format("INSERT INTO orders VALUES (%d, %d, %d, %d)", i, RANDOM.nextInt(50), RANDOM.nextInt(50), RANDOM.nextInt(99) + 1));
            }
        }
    
        private static void processQueries(int queryType) {
            for (int i = 0; i < QUERIES_PER_TYPE; i++) {
                executeQuery(queryType, i);
                if (i % 5000 == 0 && i != 0) {
                   System.out.printf("Processed %d %s queries so far...%n", i, QUERY_TYPES[queryType]);
                }
            }
        }
    
        private static void executeQuery(int queryType, int count) {
            switch (queryType) {
                case 0 -> insertRandomData(count);
                case 1 -> {
                    if(TEST_WITH_COMPLEX){
                        if (RANDOM.nextBoolean()) { 
                            executeSelectQuerySimple();
                        } else {
                            executeSelectQueryComplex();
                        }
                    } else {
                        executeSelectQuerySimple();
                    }
                }
                case 2 -> {
                    if(TEST_WITH_COMPLEX){
                        if (RANDOM.nextBoolean()) { 
                            executeUpdateQuerySimple();
                        } else {
                            executeUpdateQueryComplex();
                        }
                    } else {
                        executeUpdateQuerySimple();
                    }
                }
                case 3 -> deleteRandomData();
            }
        }
    
        private static void insertRandomData(int count) {
            int id = count / 3 + 1 + 50;
            String query;
            switch (count % 3) {
                case 0 -> {
                    query = String.format("INSERT INTO users VALUES (%d, 'User%d', %d, '%s')", id, id, RANDOM.nextInt(41) + 20, getRandomCity());
                    userCount++;
                }
                case 1 -> {
                    query = String.format("INSERT INTO products VALUES (%d, 'Product%d', %.2f, '%s')", id, id, 50 + (RANDOM.nextDouble() * 950), getRandomCategory());
                    productCount++;
                }
                case 2 -> {
                    query = String.format("INSERT INTO orders VALUES (%d, %d, %d, %d)", id, RANDOM.nextInt(userCount), RANDOM.nextInt(productCount), RANDOM.nextInt(10) + 1);
                    orderCount++;
                }
                default -> throw new IllegalArgumentException("Invalid query type");
            }
            dbEngine.executeSQL(query);
        }
    
        private static void executeSelectQuerySimple() {
            String query = RANDOM.nextBoolean() ? "SELECT * FROM users WHERE id = " +  RANDOM.nextInt(userCount)
            : "SELECT * FROM products WHERE id = " + RANDOM.nextInt(productCount);
            dbEngine.executeSQL(query);
        }
        
        private static void executeSelectQueryComplex() {
            String query = RANDOM.nextBoolean() ? "SELECT * FROM users WHERE age > " + RANDOM.nextInt(20) + " AND age < " + (RANDOM.nextInt(30) + 20)
                    : "SELECT * FROM products WHERE price > " + (RANDOM.nextDouble() * 200) + " AND price < " + ((RANDOM.nextDouble() * 500) + 50);
            dbEngine.executeSQL(query);
        }

        private static void executeUpdateQuerySimple() {
            String query = RANDOM.nextBoolean() ? "UPDATE users SET age = " + (RANDOM.nextInt(60) + 20) + " WHERE id = " + RANDOM.nextInt(userCount)
                    : "UPDATE products SET price = " + (RANDOM.nextDouble() * 1000 + 50) + " WHERE id = " + RANDOM.nextInt(productCount);
            dbEngine.executeSQL(query);
        }

        private static void executeUpdateQueryComplex() {
            String query = RANDOM.nextBoolean() ? "UPDATE users SET age = " + (RANDOM.nextInt(60) + 20) + " WHERE age > " + RANDOM.nextInt(20) + " AND age < " + (RANDOM.nextInt(30) + 20)
                    : "UPDATE products SET price = " + (RANDOM.nextDouble() * 1000 + 50) + " WHERE price > " + (RANDOM.nextDouble() * 200) + " AND price < " + ((RANDOM.nextDouble() * 500) + 50);
            dbEngine.executeSQL(query);
        }
    
        private static void deleteRandomData() {
            String query;
            switch (RANDOM.nextInt(3)) {
                case 0 -> query = "DELETE FROM users WHERE id = " + RANDOM.nextInt(userCount);
                case 1 -> query = "DELETE FROM products WHERE id = " + RANDOM.nextInt(productCount);
                case 2 -> query = "DELETE FROM orders WHERE id = " + RANDOM.nextInt(orderCount);
                default -> throw new IllegalArgumentException("Invalid delete operation");
            }
            dbEngine.executeSQL(query);
        }
    
        private static String getRandomCity() {
            return getRandomElement(new String[]{"New York", "Los Angeles", "Chicago", "Boston", "Miami", "Seattle", "Austin", "Dallas", "Atlanta", "Denver"});
        }
    
        private static String getRandomCategory() {
            return getRandomElement(new String[]{"Electronics", "Appliances", "Clothing", "Furniture", "Toys", "Sports", "Books", "Beauty", "Garden"});
        }
    
        private static String getRandomElement(String[] array) {
            return array[RANDOM.nextInt(array.length)];
        }
    }
    
    
}
