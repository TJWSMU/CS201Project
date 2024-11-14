package edu.smu.smusql;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class ProbeTest {
    private static final int[] TABLE_SIZES = {1000, 10000, 100000};
    private static final double[] LOAD_FACTORS = {0.5, 0.7, 0.9};
    
    static class ProbeMetrics {
        long insertionTime;
        long lookupTime;
        int collisions;
        double avgProbeLength;
        
        public ProbeMetrics(long insertionTime, long lookupTime, int collisions, double avgProbeLength) {
            this.insertionTime = insertionTime;
            this.lookupTime = lookupTime;
            this.collisions = collisions;
            this.avgProbeLength = avgProbeLength;
        }
    }
    
    public static ProbeMetrics runTest(boolean useLinearProbing, int tableSize, double loadFactor) {
        // Create two identical hash maps with different probing strategies
        LinearProbingHashMap<Integer, Integer> map = new LinearProbingHashMap<>(tableSize);
        int itemsToInsert = (int) (tableSize * loadFactor);
        int collisions = 0;
        int totalProbeLength = 0;
        
        // Measure insertion time
        long startInsert = System.nanoTime();
        for (int i = 0; i < itemsToInsert; i++) {
            int key = (int) (Math.random() * 1000000);
            map.put(key, i);
        }
        long endInsert = System.nanoTime();
        
        // Measure lookup time
        long startLookup = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            int key = (int) (Math.random() * 1000000);
            map.get(key);
        }
        long endLookup = System.nanoTime();
        
        return new ProbeMetrics(
            endInsert - startInsert,
            endLookup - startLookup,
            collisions,
            totalProbeLength / (double) itemsToInsert
        );
    }
    
    public static void saveResultsToXML(List<TestResult> results, String fileName) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("probingResults");
        doc.appendChild(rootElement);

        for (TestResult result : results) {
            Element testElement = doc.createElement("test");
            testElement.setAttribute("tableSize", String.valueOf(result.tableSize));
            testElement.setAttribute("loadFactor", String.valueOf(result.loadFactor));
            testElement.setAttribute("probingType", result.probingType);
            
            addElement(doc, testElement, "insertionTime", String.valueOf(result.metrics.insertionTime / 1000));
            addElement(doc, testElement, "lookupTime", String.valueOf(result.metrics.lookupTime / 1000));
            addElement(doc, testElement, "collisions", String.valueOf(result.metrics.collisions));
            addElement(doc, testElement, "avgProbeLength", String.valueOf(result.metrics.avgProbeLength));
            
            rootElement.appendChild(testElement);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));
        transformer.transform(source, result);
    }

    private static void addElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    static class TestResult {
        int tableSize;
        double loadFactor;
        String probingType;
        ProbeMetrics metrics;

        public TestResult(int tableSize, double loadFactor, String probingType, ProbeMetrics metrics) {
            this.tableSize = tableSize;
            this.loadFactor = loadFactor;
            this.probingType = probingType;
            this.metrics = metrics;
        }
    }

    public static void main(String[] args) {
        List<TestResult> allResults = new ArrayList<>();
        
        System.out.println("Comparing Linear vs Quadratic Probing");
        System.out.println("=====================================");
        
        for (int tableSize : TABLE_SIZES) {
            System.out.println("\nTable Size: " + tableSize);
            for (double loadFactor : LOAD_FACTORS) {
                System.out.println("\nLoad Factor: " + loadFactor);
                
                ProbeMetrics linearMetrics = runTest(true, tableSize, loadFactor);
                ProbeMetrics quadraticMetrics = runTest(false, tableSize, loadFactor);
                
                // Store results
                allResults.add(new TestResult(tableSize, loadFactor, "linear", linearMetrics));
                allResults.add(new TestResult(tableSize, loadFactor, "quadratic", quadraticMetrics));
                
                System.out.println("\nLinear Probing:");
                System.out.printf("Insertion time: %.3f ms\n", linearMetrics.insertionTime / 1_000_000.0);
                System.out.printf("Lookup time: %.3f ms\n", linearMetrics.lookupTime / 1_000_000.0);
                System.out.printf("Collisions: %d\n", linearMetrics.collisions);
                System.out.printf("Average probe length: %.2f\n", linearMetrics.avgProbeLength);
                
                System.out.println("\nQuadratic Probing:");
                System.out.printf("Insertion time: %.3f ms\n", quadraticMetrics.insertionTime / 1_000_000.0);
                System.out.printf("Lookup time: %.3f ms\n", quadraticMetrics.lookupTime / 1_000_000.0);
                System.out.printf("Collisions: %d\n", quadraticMetrics.collisions);
                System.out.printf("Average probe length: %.2f\n", quadraticMetrics.avgProbeLength);
            }
        }

        try {
            saveResultsToXML(allResults, "probing_results.xml");
            System.out.println("\nResults have been saved to probing_results.xml");
        } catch (Exception e) {
            System.err.println("Error saving results to XML: " + e.getMessage());
        }
    }
} 