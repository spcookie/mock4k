package com.mock4k.example;

import com.mock4k.Mock;
import com.mock4k.random.MockRandom;

import java.util.*;

/**
 * Mock4K Java Adapter Example
 * Demonstrates how to use the Kotlin version of Mock4K library in Java
 */
public class JavaExample {
    
    public static void main(String[] args) {
        System.out.println("=== Mock4K Java Adapter Example ===");
        
        // 1. Basic mock usage
        basicMockExample();
        
        // 2. String rules
        stringRuleExample();
        
        // 3. Number rules
        numberRuleExample();
        
        // 4. Array rules
        arrayRuleExample();
        
        // 5. Placeholders
        placeholderExample();
        
        // 6. Complex nested structures
        complexTemplateExample();
        
        // 7. Chinese content generation
        chineseContentExample();
        
        // 8. Direct use of Mock.Random
        mockRandomExample();
    }
    
    /**
     * Basic mock usage
     */
    private static void basicMockExample() {
        System.out.println("\n--- Basic Mock ---");
        
        // String
        Object result = Mock.mock("Hello World");
        System.out.println("String: " + result);
        
        // Number
        result = Mock.mock(42);
        System.out.println("Number: " + result);
        
        // Boolean
        result = Mock.mock(true);
        System.out.println("Boolean: " + result);
    }
    
    /**
     * String rule examples
     */
    private static void stringRuleExample() {
        System.out.println("\n--- String Rules ---");
        
        // Repeat 3 times
        Map<String, Object> template = new HashMap<>();
        template.put("name|3", "Hello");
        Object result = Mock.mock(template);
        System.out.println("Repeat 3 times: " + result);
        
        // Repeat 2-5 times
        template.clear();
        template.put("text|2-5", "Hi");
        result = Mock.mock(template);
        System.out.println("Repeat 2-5 times: " + result);
    }
    
    /**
     * Number rule examples
     */
    private static void numberRuleExample() {
        System.out.println("\n--- Number Rules ---");
        
        // Integer range
        Map<String, Object> template = new HashMap<>();
        template.put("age|18-65", 0);
        Object result = Mock.mock(template);
        System.out.println("Age(18-65): " + result);
        
        // Float range
        template.clear();
        template.put("price|1-100.1-3", 0.0);
        result = Mock.mock(template);
        System.out.println("Price(1-100.1-3): " + result);
        
        // Increment
        template.clear();
        template.put("id|+1", 1);
        System.out.println("ID increment 1: " + Mock.mock(template));
        System.out.println("ID increment 2: " + Mock.mock(template));
        System.out.println("ID increment 3: " + Mock.mock(template));
    }
    
    /**
     * Array rule examples
     */
    private static void arrayRuleExample() {
        System.out.println("\n--- Array Rules ---");
        
        // Select 1-3 elements from array
        Map<String, Object> template = new HashMap<>();
        List<String> colors = Arrays.asList("red", "green", "blue", "yellow", "purple");
        template.put("colors|1-3", colors);
        Object result = Mock.mock(template);
        System.out.println("Select colors(1-3): " + result);
        
        // Repeat array 2 times
        template.clear();
        template.put("numbers|2", Arrays.asList(1, 2, 3));
        result = Mock.mock(template);
        System.out.println("Repeat array 2 times: " + result);
    }
    
    /**
     * Placeholder examples
     */
    private static void placeholderExample() {
        System.out.println("\n--- Placeholders ---");
        
        System.out.println("Name: " + Mock.mock("@name"));
        System.out.println("Email: " + Mock.mock("@email"));
        System.out.println("Date: " + Mock.mock("@date"));
        System.out.println("Time: " + Mock.mock("@time"));
        System.out.println("Integer(1-100): " + Mock.mock("@integer(1,100)"));
        System.out.println("String(5 chars): " + Mock.mock("@string(5)"));
        System.out.println("Boolean: " + Mock.mock("@boolean"));
        System.out.println("Color: " + Mock.mock("@color"));
        System.out.println("URL: " + Mock.mock("@url"));
    }
    
    /**
     * Complex template examples
     */
    private static void complexTemplateExample() {
        System.out.println("\n--- Complex Templates ---");
        
        // User object template
        Map<String, Object> userTemplate = new HashMap<>();
        userTemplate.put("id|+1", 1);
        userTemplate.put("name", "@name");
        userTemplate.put("email", "@email");
        userTemplate.put("age|18-65", 0);
        userTemplate.put("isActive", "@boolean");
        
        // Address object
        Map<String, Object> address = new HashMap<>();
        address.put("street", "@sentence");
        address.put("city", "@word");
        address.put("zipCode", "@integer(10000,99999)");
        userTemplate.put("address", address);
        
        // Hobbies array
        List<String> hobbies = Arrays.asList("reading", "swimming", "coding", "gaming", "music");
        userTemplate.put("hobbies|1-3", hobbies);
        
        // Generate user list
        Map<String, Object> template = new HashMap<>();
        template.put("users|3", userTemplate);
        
        Object result = Mock.mock(template);
        System.out.println("User list: " + result);
    }
    
    /**
     * Chinese content examples
     */
    private static void chineseContentExample() {
        System.out.println("\n--- Chinese Content ---");
        
        System.out.println("Chinese name: " + Mock.mock("@cname"));
        System.out.println("Chinese word: " + Mock.mock("@cword"));
        System.out.println("Chinese sentence: " + Mock.mock("@csentence"));
        System.out.println("Chinese title: " + Mock.mock("@ctitle"));
        
        // Chinese user template
        Map<String, Object> chineseUser = new HashMap<>();
        chineseUser.put("name", "@cname");
        chineseUser.put("age|18-65", 0);
        chineseUser.put("bio", "@cparagraph");
        
        Object result = Mock.mock(chineseUser);
        System.out.println("Chinese user: " + result);
    }
    
    /**
     * Direct use of Mock.Random examples
     */
    private static void mockRandomExample() {
        System.out.println("\n--- Mock.Random Methods ---");
        
        MockRandom random = Mock.Random;
        
        System.out.println("Random boolean: " + random.bool());
        System.out.println("Random integer(1-100): " + random.integer(1, 100));
        System.out.println("Random float: " + random.floating(0.0, 1.0));
        System.out.println("Random string(8 chars): " + random.string(8));
        System.out.println("Random date: " + random.date());
        System.out.println("Random time: " + random.time());
        System.out.println("Random English name: " + random.name());
        System.out.println("Random Chinese name: " + random.cname());
        System.out.println("Random email: " + random.email());
        System.out.println("Random URL: " + random.url());
        System.out.println("Random IP: " + random.ip());
        System.out.println("Random color: " + random.color());
        System.out.println("Random GUID: " + random.guid());
        
        // Pick from list
        List<String> fruits = Arrays.asList("apple", "banana", "orange", "grape");
        System.out.println("Random fruit: " + random.pick(fruits));
        
        // Shuffle list
        System.out.println("Shuffled fruits: " + random.shuffle(fruits));
    }
}