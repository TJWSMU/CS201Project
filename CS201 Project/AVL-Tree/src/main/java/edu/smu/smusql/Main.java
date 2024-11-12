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
                autoEvaluate();
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
                "CREATE TABLE Employees (ID, Name, Age, Department)",
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
    

    public static void autoEvaluate() {

        // Set the number of queries to execute
        int numberOfQueries = 100000;

        // Create tables
        dbEngine.executeSQL("CREATE TABLE users (id, name, age, city)");
        dbEngine.executeSQL("CREATE TABLE products (id, name, price, category)");
        dbEngine.executeSQL("CREATE TABLE orders (id, user_id, product_id, quantity)");

        // Random data generator
        Random random = new Random();

        prepopulateTables(random);

        // Loop to simulate millions of queries
        for (int i = 0; i < numberOfQueries; i++) {
            int queryType = random.nextInt(6);  // Randomly choose the type of query to execute

            switch (queryType) {
                case 0:  // INSERT query
                    insertRandomData(random);
                    break;
                case 1:  // SELECT query (simple)
                    selectRandomData(random);
                    break;
                case 2:  // UPDATE query
                    updateRandomData(random);
                    break;
                case 3:  // DELETE query
                    deleteRandomData(random);
                    break;
                case 4:  // Complex SELECT query with WHERE, AND, OR, >, <, LIKE
                    complexSelectQuery(random);
                    break;
                case 5:  // Complex UPDATE query with WHERE
                    complexUpdateQuery(random);
                    break;
            }

            // Print progress every 10,000 queries
            if (i % 10000 == 0 && i != 0) {
                System.out.println("Processed " + i + " queries...");
            }
        }

        System.out.println("Finished processing " + numberOfQueries + " queries.");
    }

    private static void prepopulateTables(Random random) {
        System.out.println("Prepopulating users");
        // Insert initial users
        for (int i = 0; i < 50; i++) {
            String name = "User" + i;
            int age = 20 + (i % 41); // Ages between 20 and 60
            String city = getRandomCity(random);
            String insertCommand = String.format("INSERT INTO users VALUES (%d, '%s', %d, '%s')", i, name, age, city);
            dbEngine.executeSQL(insertCommand);
        }
        System.out.println("Prepopulating products");
        // Insert initial products
        for (int i = 0; i < 50; i++) {
            String productName = "Product" + i;
            double price = 10 + (i % 990); // Prices between $10 and $1000
            String category = getRandomCategory(random);
            String insertCommand = String.format("INSERT INTO products VALUES (%d, '%s', %.2f, '%s')", i, productName, price, category);
            dbEngine.executeSQL(insertCommand);
        }
        System.out.println("Prepopulating orders");
        // Insert initial orders
        for (int i = 0; i < 50; i++) {
            int user_id = random.nextInt(9999);
            int product_id = random.nextInt(9999);
            int quantity = random.nextInt(99) + 1; // Quantity between 1 and 100
            String insertCommand = String.format("INSERT INTO orders VALUES (%d, %d, %d, %d)", i, user_id, product_id, quantity);
            dbEngine.executeSQL(insertCommand);
        }
    }

    // Helper method to insert random data into users, products, or orders table
    private static void insertRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Insert into users table
                int id = random.nextInt(10000) + 10000;
                String name = "User" + id;
                int age = random.nextInt(60) + 20;
                String city = getRandomCity(random);
                String insertUserQuery = "INSERT INTO users VALUES (" + id + ", '" + name + "', " + age + ", '" + city + "')";
                dbEngine.executeSQL(insertUserQuery);
                break;
            case 1: // Insert into products table
                int productId = random.nextInt(1000) + 10000;
                String productName = "Product" + productId;
                double price = 50 + (random.nextDouble() * 1000);
                String category = getRandomCategory(random);
                String insertProductQuery = "INSERT INTO products VALUES (" + productId + ", '" + productName + "', " + price + ", '" + category + "')";
                dbEngine.executeSQL(insertProductQuery);
                break;
            case 2: // Insert into orders table
                int orderId = random.nextInt(10000) + 1;
                int userId = random.nextInt(10000) + 1;
                int productIdRef = random.nextInt(1000) + 1;
                int quantity = random.nextInt(10) + 1;
                String insertOrderQuery = "INSERT INTO orders VALUES (" + orderId + ", " + userId + ", " + productIdRef + ", " + quantity + ")";
                dbEngine.executeSQL(insertOrderQuery);
                break;
        }
    }

    // Helper method to randomly select data from tables
    private static void selectRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        String selectQuery;
        switch (tableChoice) {
            case 0:
                selectQuery = "SELECT * FROM users";
                break;
            case 1:
                selectQuery = "SELECT * FROM products";
                break;
            case 2:
                selectQuery = "SELECT * FROM orders";
                break;
            default:
                selectQuery = "SELECT * FROM users";
        }
        dbEngine.executeSQL(selectQuery);
    }

    // Helper method to update random data in the tables
    private static void updateRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Update users table
                int id = random.nextInt(10000) + 1;
                int newAge = random.nextInt(60) + 20;
                String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE id = " + id;
                dbEngine.executeSQL(updateUserQuery);
                break;
            case 1: // Update products table
                int productId = random.nextInt(1000) + 1;
                double newPrice = 50 + (random.nextDouble() * 1000);
                String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE id = " + productId;
                dbEngine.executeSQL(updateProductQuery);
                break;
            case 2: // Update orders table
                int orderId = random.nextInt(10000) + 1;
                int newQuantity = random.nextInt(10) + 1;
                String updateOrderQuery = "UPDATE orders SET quantity = " + newQuantity + " WHERE id = " + orderId;
                dbEngine.executeSQL(updateOrderQuery);
                break;
        }
    }

    // Helper method to delete random data from tables
    private static void deleteRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Delete from users table
                int userId = random.nextInt(10000) + 1;
                String deleteUserQuery = "DELETE FROM users WHERE id = " + userId;
                dbEngine.executeSQL(deleteUserQuery);
                break;
            case 1: // Delete from products table
                int productId = random.nextInt(1000) + 1;
                String deleteProductQuery = "DELETE FROM products WHERE id = " + productId;
                dbEngine.executeSQL(deleteProductQuery);
                break;
            case 2: // Delete from orders table
                int orderId = random.nextInt(10000) + 1;
                String deleteOrderQuery = "DELETE FROM orders WHERE id = " + orderId;
                dbEngine.executeSQL(deleteOrderQuery);
                break;
        }
    }

    // Helper method to execute a complex SELECT query with WHERE, AND, OR, >, <
    private static void complexSelectQuery(Random random) {
        int tableChoice = random.nextInt(2);  // Complex queries only on users and products for now
        String complexSelectQuery;
        switch (tableChoice) {
            case 0: // Complex SELECT on users
                int minAge = random.nextInt(20) + 20;
                int maxAge = minAge + random.nextInt(30);
                complexSelectQuery = "SELECT * FROM users WHERE age > " + minAge + " AND age < " + maxAge;
                break;
            case 1: // Complex SELECT on products
                double minPrice = 50 + (random.nextDouble() * 200);
                double maxPrice = minPrice + random.nextDouble() * 500;
                complexSelectQuery = "SELECT * FROM products WHERE price > " + minPrice + " AND price < " + maxPrice;
                break;
            default:
                complexSelectQuery = "SELECT * FROM users";
        }
        dbEngine.executeSQL(complexSelectQuery);
    }

    // Helper method to execute a complex UPDATE query with WHERE
    private static void complexUpdateQuery(Random random) {
        int tableChoice = random.nextInt(2);  // Complex updates only on users and products for now
        switch (tableChoice) {
            case 0: // Complex UPDATE on users
                int newAge = random.nextInt(60) + 20;
                String city = getRandomCity(random);
                String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE city = '" + city + "'";
                dbEngine.executeSQL(updateUserQuery);
                break;
            case 1: // Complex UPDATE on products
                double newPrice = 50 + (random.nextDouble() * 1000);
                String category = getRandomCategory(random);
                String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE category = '" + category + "'";
                dbEngine.executeSQL(updateProductQuery);
                break;
        }
    }

    // Helper method to return a random city
    private static String getRandomCity(Random random) {
        String[] cities = {"New York", "Los Angeles", "Chicago", "Boston", "Miami", "Seattle", "Austin", "Dallas", "Atlanta", "Denver"};
        return cities[random.nextInt(cities.length)];
    }

    // Helper method to return a random category for products
    private static String getRandomCategory(Random random) {
        String[] categories = {"Electronics", "Appliances", "Clothing", "Furniture", "Toys", "Sports", "Books", "Beauty", "Garden"};
        return categories[random.nextInt(categories.length)];
    }
}
