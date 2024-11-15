// package edu.smu.smusql;

// import java.util.*;

// // @author ziyuanliu@smu.edu.sg

// public class Main {
//     /*
//      *  Main method for accessing the command line interface of the database engine.
//      *  MODIFICATION OF THIS FILE IS NOT RECOMMENDED!
//      */
//     static Engine dbEngine = new Engine();
//     public static void main(String[] args) {

//         Scanner scanner = new Scanner(System.in);

//         System.out.println("smuSQL Starter Code version 0.5");
//         System.out.println("Have fun, and good luck!");

//         while (true) {
//             System.out.print("smusql> ");
//             String query = scanner.nextLine();
//             if (query.equalsIgnoreCase("exit")) {
//                 break;
//             } else if (query.equalsIgnoreCase("evaluate")) {
//                 long startTime = System.nanoTime();
//                 autoEvaluate();
//                 long stopTime = System.nanoTime();
//                 long elapsedTime = stopTime - startTime;
//                 double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
//                 System.out.println("Time elapsed: " + elapsedTimeInSecond + " seconds");
//                 break;
//             }

//             System.out.println(dbEngine.executeSQL(query));
//         }
//         scanner.close();
//     }


//     /*
//      *  Below is the code for auto-evaluating your work.
//      *  DO NOT CHANGE ANYTHING BELOW THIS LINE!
//      */
//     public static void autoEvaluate() {

//         // Set the number of queries to execute
//         int numberOfQueries = 1000000;

//         // Create tables
//         dbEngine.executeSQL("CREATE TABLE users (id, name, age, city)");
//         dbEngine.executeSQL("CREATE TABLE products (id, name, price, category)");
//         dbEngine.executeSQL("CREATE TABLE orders (id, user_id, product_id, quantity)");

//         // Random data generator
//         Random random = new Random();

//         // Prepopulate the tables in preparation for evaluation
//         prepopulateTables(random);

//         // Loop to simulate millions of queries
//         for (int i = 0; i < numberOfQueries; i++) {
//             int queryType = random.nextInt(6);  // Randomly choose the type of query to execute

//             switch (queryType) {
//                 case 0:  // INSERT query
//                     insertRandomData(random);
//                     break;
//                 case 1:  // SELECT query (simple)
//                     selectRandomData(random);
//                     break;
//                 case 2:  // UPDATE query
//                     // updateRandomData(random);
//                     break;
//                 case 3:  // DELETE query
//                     // deleteRandomData(random);
//                     break;
//                 case 4:  // Complex SELECT query with WHERE, AND, OR, >, <, LIKE
//                     complexSelectQuery(random);
//                     break;
//                 case 5:  // Complex UPDATE query with WHERE
//                     // complexUpdateQuery(random);
//                     break;
//             }

//             // Print progress every 100,000 queries
//             if (i % 10000 == 0){
//                 System.out.println("Processed " + i + " queries...");
//             }
//         }

//         System.out.println("Finished processing " + numberOfQueries + " queries.");
//     }

//     private static void prepopulateTables(Random random) {
//         System.out.println("Prepopulating users");
//         // Insert initial users
//         for (int i = 0; i < 50; i++) {
//             String name = "User" + i;
//             int age = 20 + (i % 41); // Ages between 20 and 60
//             String city = getRandomCity(random);
//             String insertCommand = String.format("INSERT INTO users VALUES (%d, '%s', %d, '%s')", i, name, age, city);
//             dbEngine.executeSQL(insertCommand);
//         }
//         System.out.println("Prepopulating products");
//         // Insert initial products
//         for (int i = 0; i < 50; i++) {
//             String productName = "Product" + i;
//             double price = 10 + (i % 990); // Prices between $10 and $1000
//             String category = getRandomCategory(random);
//             String insertCommand = String.format("INSERT INTO products VALUES (%d, '%s', %.2f, '%s')", i, productName, price, category);
//             dbEngine.executeSQL(insertCommand);
//         }
//         System.out.println("Prepopulating orders");
//         // Insert initial orders
//         for (int i = 0; i < 50; i++) {
//             int user_id = random.nextInt(9999);
//             int product_id = random.nextInt(9999);
//             int quantity = random.nextInt(1, 100);
//             String category = getRandomCategory(random);
//             String insertCommand = String.format("INSERT INTO orders VALUES (%d, %d, %d, %d)", i, user_id, product_id, quantity);
//             dbEngine.executeSQL(insertCommand);
//         }
//     }

//     // Helper method to insert random data into users, products, or orders table
//     private static void insertRandomData(Random random) {
//         int tableChoice = random.nextInt(3);
//         switch (tableChoice) {
//             case 0: // Insert into users table
//                 int id = random.nextInt(10000) + 10000;
//                 String name = "User" + id;
//                 int age = random.nextInt(60) + 20;
//                 String city = getRandomCity(random);
//                 String insertUserQuery = "INSERT INTO users VALUES (" + id + ", '" + name + "', " + age + ", '" + city + "')";
//                 dbEngine.executeSQL(insertUserQuery);
//                 break;
//             case 1: // Insert into products table
//                 int productId = random.nextInt(1000) + 10000;
//                 String productName = "Product" + productId;
//                 double price = 50 + (random.nextDouble() * 1000);
//                 String category = getRandomCategory(random);
//                 String insertProductQuery = "INSERT INTO products VALUES (" + productId + ", '" + productName + "', " + price + ", '" + category + "')";
//                 dbEngine.executeSQL(insertProductQuery);
//                 break;
//             case 2: // Insert into orders table
//                 int orderId = random.nextInt(10000) + 1;
//                 int userId = random.nextInt(10000) + 1;
//                 int productIdRef = random.nextInt(1000) + 1;
//                 int quantity = random.nextInt(10) + 1;
//                 String insertOrderQuery = "INSERT INTO orders VALUES (" + orderId + ", " + userId + ", " + productIdRef + ", " + quantity + ")";
//                 dbEngine.executeSQL(insertOrderQuery);
//                 break;
//         }
//     }

//     // Helper method to randomly select data from tables
//     private static void selectRandomData(Random random) {
//         int tableChoice = random.nextInt(3);
//         String selectQuery;
//         switch (tableChoice) {
//             case 0:
//                 selectQuery = "SELECT * FROM users";
//                 break;
//             case 1:
//                 selectQuery = "SELECT * FROM products";
//                 break;
//             case 2:
//                 selectQuery = "SELECT * FROM orders";
//                 break;
//             default:
//                 selectQuery = "SELECT * FROM users";
//         }
//         dbEngine.executeSQL(selectQuery);
//     }

//     // Helper method to update random data in the tables
//     private static void updateRandomData(Random random) {
//         int tableChoice = random.nextInt(3);
//         switch (tableChoice) {
//             case 0: // Update users table
//                 int id = random.nextInt(10000) + 1;
//                 int newAge = random.nextInt(60) + 20;
//                 String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE id = " + id;
//                 dbEngine.executeSQL(updateUserQuery);
//                 break;
//             case 1: // Update products table
//                 int productId = random.nextInt(1000) + 1;
//                 double newPrice = 50 + (random.nextDouble() * 1000);
//                 String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE id = " + productId;
//                 dbEngine.executeSQL(updateProductQuery);
//                 break;
//             case 2: // Update orders table
//                 int orderId = random.nextInt(10000) + 1;
//                 int newQuantity = random.nextInt(10) + 1;
//                 String updateOrderQuery = "UPDATE orders SET quantity = " + newQuantity + " WHERE id = " + orderId;
//                 dbEngine.executeSQL(updateOrderQuery);
//                 break;
//         }
//     }

//     // Helper method to delete random data from tables
//     private static void deleteRandomData(Random random) {
//         int tableChoice = random.nextInt(3);
//         switch (tableChoice) {
//             case 0: // Delete from users table
//                 int userId = random.nextInt(10000) + 1;
//                 String deleteUserQuery = "DELETE FROM users WHERE id = " + userId;
//                 dbEngine.executeSQL(deleteUserQuery);
//                 break;
//             case 1: // Delete from products table
//                 int productId = random.nextInt(1000) + 1;
//                 String deleteProductQuery = "DELETE FROM products WHERE id = " + productId;
//                 dbEngine.executeSQL(deleteProductQuery);
//                 break;
//             case 2: // Delete from orders table
//                 int orderId = random.nextInt(10000) + 1;
//                 String deleteOrderQuery = "DELETE FROM orders WHERE id = " + orderId;
//                 dbEngine.executeSQL(deleteOrderQuery);
//                 break;
//         }
//     }

//     // Helper method to execute a complex SELECT query with WHERE, AND, OR, >, <, LIKE
//     private static void complexSelectQuery(Random random) {
//         int tableChoice = random.nextInt(2);  // Complex queries only on users and products for now
//         String complexSelectQuery;
//         switch (tableChoice) {
//             case 0: // Complex SELECT on users
//                 int minAge = random.nextInt(20) + 20;
//                 int maxAge = minAge + random.nextInt(30);
//                 String city = getRandomCity(random);
//                 complexSelectQuery = "SELECT * FROM users WHERE age > " + minAge + " AND age < " + maxAge;
//                 break;
//             case 1: // Complex SELECT on products
//                 double minPrice = 50 + (random.nextDouble() * 200);
//                 double maxPrice = minPrice + random.nextDouble() * 500;
//                 complexSelectQuery = "SELECT * FROM products WHERE price > " + minPrice + " AND price < " + maxPrice;
//                 break;
//             case 2: // Complex SELECT on products
//                 double minPrice2 = 50 + (random.nextDouble() * 200);
//                 String category = getRandomCategory(random);
//                 complexSelectQuery = "SELECT * FROM products WHERE price > " + minPrice2 + " AND category = " + category;
//                 break;
//             default:
//                 complexSelectQuery = "SELECT * FROM users";
//         }
//         dbEngine.executeSQL(complexSelectQuery);
//     }

//     // Helper method to execute a complex UPDATE query with WHERE
//     private static void complexUpdateQuery(Random random) {
//         int tableChoice = random.nextInt(2);  // Complex updates only on users and products for now
//         switch (tableChoice) {
//             case 0: // Complex UPDATE on users
//                 int newAge = random.nextInt(60) + 20;
//                 String city = getRandomCity(random);
//                 String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE city = '" + city + "'";
//                 dbEngine.executeSQL(updateUserQuery);
//                 break;
//             case 1: // Complex UPDATE on products
//                 double newPrice = 50 + (random.nextDouble() * 1000);
//                 String category = getRandomCategory(random);
//                 String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE category = '" + category + "'";
//                 dbEngine.executeSQL(updateProductQuery);
//                 break;
//         }
//     }

//     // Helper method to return a random city
//     private static String getRandomCity(Random random) {
//         String[] cities = {"New York", "Los Angeles", "Chicago", "Boston", "Miami", "Seattle", "Austin", "Dallas", "Atlanta", "Denver"};
//         return cities[random.nextInt(cities.length)];
//     }

//     // Helper method to return a random category for products
//     private static String getRandomCategory(Random random) {
//         String[] categories = {"Electronics", "Appliances", "Clothing", "Furniture", "Toys", "Sports", "Books", "Beauty", "Garden"};
//         return categories[random.nextInt(categories.length)];
//     }
// }

package edu.smu.smusql;

import java.util.*;

public class Main {
    // Main method to run the simple database engine
    static Engine dbEngine = new Engine();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("smuSQL version 0.2.1 2024-11-15");
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
                // break;
            } else if (query.equalsIgnoreCase("evaluate")) {
                long startTime = System.nanoTime();
                DatabaseAutoEvaluator.autoEvaluate();
                long stopTime = System.nanoTime();
                long elapsedTime = stopTime - startTime;
                double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                System.out.println("Time elapsed: " + elapsedTimeInSecond + " seconds");
                // break;
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
    
    public class DatabaseAutoEvaluator {

        private static final int NUMBER_OF_QUERIES = 40000;
        private static final String[] QUERY_TYPES = {"INSERT", "SELECT", "UPDATE", "DELETE"};
        private static final int QUERIES_PER_TYPE = NUMBER_OF_QUERIES / QUERY_TYPES.length;
        private static final Random RANDOM = new Random(12345); // Fixed seed


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
            dbEngine.executeSQL("CREATE TABLE users (id, name, age, city)");
            dbEngine.executeSQL("CREATE TABLE products (id, name, price, category)");
            dbEngine.executeSQL("CREATE TABLE orders (id, user_id, product_id, quantity)");
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
                   System.out.printf("Processed %d %s queries...%n", i, QUERY_TYPES[queryType]);
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
            // Update a user's age to reflect realistic increments or changes
            String query = RANDOM.nextBoolean() 
                ? "UPDATE users SET age =  " + (10 + RANDOM.nextInt(15)) + " WHERE id = " + (10 + RANDOM.nextInt(userCount - 10))
                : "UPDATE products SET price = " + (100 + RANDOM.nextInt(500)) + " WHERE id = " + (10 + RANDOM.nextInt(productCount - 10));
            dbEngine.executeSQL(query);
        }
        
        private static void executeUpdateQueryComplex() {
            String query;
        
            if (RANDOM.nextBoolean()) {
                // Randomize age range
                int lowerAge = 20 + RANDOM.nextInt(10); // Random age > 20
                int upperAge = lowerAge + 1 + RANDOM.nextInt(2); // Upper bound is at least 1 years higher
                int ageAdjustment = 2 + RANDOM.nextInt(5); // Adjustment between 2 and 6
        
                // Update user's age with randomized ranges
                query = RANDOM.nextBoolean() 
                    ? "UPDATE users SET age = " + (lowerAge+ageAdjustment) + " WHERE age > " + lowerAge + " AND age < " + upperAge
                    : "UPDATE users SET age = " + (lowerAge-ageAdjustment) + " WHERE age >= " + lowerAge + " AND age <= " + upperAge;
            } else {
                // Randomize price range
                double lowerPrice = 50 + (RANDOM.nextDouble() * 50); // Random price > 50
                double upperPrice = lowerPrice + 1 + (RANDOM.nextDouble() * 50); // Upper bound is at least 1 higher
                double priceMultiplier = 1.05 + (RANDOM.nextDouble() * 0.1); // Multiplier between 1.05 and 1.15
        
                // Update product prices with randomized ranges
                query = RANDOM.nextBoolean() 
                    ? "UPDATE products SET price = " + (lowerPrice*priceMultiplier) + " WHERE price > " + lowerPrice + " AND price < " + upperPrice
                    : "UPDATE products SET price = " + (lowerPrice/priceMultiplier) + " WHERE price >= " + lowerPrice + " AND price <= " + upperPrice;
            }
        
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

