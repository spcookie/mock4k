# Mock4K Java Integration Guide

This document describes how to use the Mock4K library in Java projects. Mock4K is written in Kotlin but is fully compatible with Java and can be used seamlessly in Java projects.

## Quick Start

### 1. Add Dependency

Add Mock4K dependency to your Java project:

```gradle
dependencies {
    implementation 'com.mock4k:mock4k:1.0.0'
}
```

### 2. Basic Usage

```java
import com.mock4k.Mock;

public class Example {
    public static void main(String[] args) {
        // Basic mock
        Object result = Mock.mock("Hello World");
        System.out.println(result); // "Hello World"
        
        // Using templates
        Map<String, Object> template = new HashMap<>();
        template.put("name", "@name");
        template.put("age|18-65", 0);
        
        Object user = Mock.mock(template);
        System.out.println(user);
    }
}
```

## API Reference

### Mock Class

The main entry point for Mock4K, providing static methods to generate mock data.

#### Methods

- `Mock.mock(Object template)` - Generate data based on template
- `Mock.mock(Object template, int count)` - Generate specified amount of data
- `Mock.Random` - Access random data generators

### Data Template Definition Specification

#### 1. Property Value Rules

Use Map to define object templates in Java:

```java
Map<String, Object> template = new HashMap<>();

// String repetition
template.put("name|3", "Hello");        // "HelloHelloHello"
template.put("text|2-5", "Hi");         // "HiHi" to "HiHiHiHiHi"

// Number range
template.put("age|18-65", 0);           // Integer between 18-65
template.put("price|1-100.1-3", 0.0);   // Float between 1.1-100.999

// Increment
template.put("id|+1", 1);               // 1, 2, 3, 4...

// Boolean
template.put("isActive|1", true);       // Random true/false

// Array selection
List<String> colors = Arrays.asList("red", "green", "blue");
template.put("color|1", colors);        // Randomly select 1 color
template.put("colors|1-3", colors);     // Randomly select 1-3 colors

// Array repetition
template.put("numbers|2", Arrays.asList(1, 2, 3)); // [1,2,3,1,2,3]
```

#### 2. Placeholder Rules

Use `@placeholder` syntax to generate random data:

```java
// Basic types
Mock.mock("@boolean");          // Random boolean
Mock.mock("@integer");          // Random integer
Mock.mock("@integer(1,100)");   // Integer between 1-100
Mock.mock("@float(0,1)");       // Float between 0-1
Mock.mock("@string(5)");        // 5-character random string

// Date and time
Mock.mock("@date");             // Random date "2023-05-15"
Mock.mock("@time");             // Random time "14:30:25"
Mock.mock("@datetime");         // Random datetime
Mock.mock("@now");              // Current timestamp

// Text
Mock.mock("@word");             // Random word
Mock.mock("@sentence");         // Random sentence
Mock.mock("@paragraph");        // Random paragraph
Mock.mock("@title");            // Random title

// Chinese text
Mock.mock("@cword");            // Random Chinese word
Mock.mock("@csentence");        // Random Chinese sentence
Mock.mock("@cparagraph");       // Random Chinese paragraph
Mock.mock("@ctitle");           // Random Chinese title

// Names
Mock.mock("@first");            // English first name
Mock.mock("@last");             // English last name
Mock.mock("@name");             // English full name
Mock.mock("@cfirst");           // Chinese first name
Mock.mock("@clast");            // Chinese last name
Mock.mock("@cname");            // Chinese full name

// Network
Mock.mock("@url");              // Random URL
Mock.mock("@domain");           // Random domain
Mock.mock("@email");            // Random email
Mock.mock("@ip");               // Random IP address

// Colors
Mock.mock("@color");            // Random color "#FF5733"
Mock.mock("@rgb");              // RGB color
Mock.mock("@rgba");             // RGBA color
Mock.mock("@hsl");              // HSL color

// Others
Mock.mock("@guid");             // Random GUID
Mock.mock("@id");               // Random ID
Mock.mock("@image");            // Random image URL
Mock.mock("@image(300x200)");   // Image URL with specified dimensions
```

### MockRandom Class

Direct access to random data generators:

```java
import com.mock4k.MockRandom;

MockRandom random = Mock.Random;

// Basic types
boolean bool = random.bool();
int number = random.integer(1, 100);
double floating = random.floating(0.0, 1.0);
String text = random.string(10);

// Date and time
String date = random.date();
String time = random.time();
String datetime = random.datetime();

// Text
String word = random.word();
String sentence = random.sentence();
String paragraph = random.paragraph();

// Names
String name = random.name();
String cname = random.cname();

// Network
String email = random.email();
String url = random.url();
String ip = random.ip();

// Colors
String color = random.color();
String rgb = random.rgb();

// Utility methods
List<String> items = Arrays.asList("a", "b", "c");
String picked = random.pick(items);        // Randomly pick one
List<String> shuffled = random.shuffle(items); // Shuffle list
```

## Complete Examples

### User Data Generation

```java
import com.mock4k.Mock;
import java.util.*;

public class UserDataExample {
    public static void main(String[] args) {
        // Define user template
        Map<String, Object> userTemplate = new HashMap<>();
        userTemplate.put("id|+1", 1);
        userTemplate.put("name", "@name");
        userTemplate.put("email", "@email");
        userTemplate.put("age|18-65", 0);
        userTemplate.put("isActive", "@boolean");
        
        // Address information
        Map<String, Object> address = new HashMap<>();
        address.put("street", "@sentence");
        address.put("city", "@word");
        address.put("zipCode", "@integer(10000,99999)");
        userTemplate.put("address", address);
        
        // Hobbies list
        List<String> hobbies = Arrays.asList(
            "reading", "swimming", "coding", "gaming", "music"
        );
        userTemplate.put("hobbies|1-3", hobbies);
        
        // Generate user list
        Map<String, Object> template = new HashMap<>();
        template.put("users|5", userTemplate);
        
        Object result = Mock.mock(template);
        System.out.println(result);
    }
}
```

### API Response Mocking

```java
import com.mock4k.Mock;
import java.util.*;

public class ApiResponseExample {
    public static void main(String[] args) {
        // API response template
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("timestamp", "@now");
        
        // Data section
        Map<String, Object> data = new HashMap<>();
        data.put("total|100-1000", 0);
        data.put("page|1-10", 1);
        data.put("pageSize", 20);
        
        // Product list
        Map<String, Object> product = new HashMap<>();
        product.put("id|+1", 1);
        product.put("name", "@title");
        product.put("price|10-999.99", 0.0);
        product.put("description", "@paragraph");
        product.put("image", "@image(300x200)");
        product.put("inStock", "@boolean");
        
        data.put("items|10-20", product);
        response.put("data", data);
        
        Object result = Mock.mock(response);
        System.out.println(result);
    }
}
```

## Important Notes

1. **Type Casting**: Due to Java's type system, you may need appropriate type casting:
   ```java
   @SuppressWarnings("unchecked")
   Map<String, Object> result = (Map<String, Object>) Mock.mock(template);
   ```

2. **Collection Types**: Use Java collection types (ArrayList, HashMap, etc.) to define templates.

3. **Null Handling**: Mock4K returns Object type, please check for null values before use.

4. **Thread Safety**: MockRandom is thread-safe and can be used in multi-threaded environments.

## Differences from Kotlin Version

- Java version requires explicit type casting
- Uses Java collection types instead of Kotlin collections
- Method call syntax is slightly different (Java style)
- Need to handle checked exceptions (if any)

## Testing

You can use JUnit for testing:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MockTest {
    @Test
    public void testBasicMock() {
        String result = (String) Mock.mock("Hello");
        assertEquals("Hello", result);
    }
    
    @Test
    public void testRandomData() {
        String email = (String) Mock.mock("@email");
        assertTrue(email.contains("@"));
    }
}
```

For more examples, please refer to `src/main/java/com/mock4k/example/JavaExample.java` and `src/test/java/com/mock4k/MockTest.java`.