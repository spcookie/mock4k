package io.github.spcookie;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.spcookie.GlobalMocks.mock;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Java integration test for Mock4K core functionality
 * Tests Java interoperability with Mock4K's main features
 *
 * @author spcookie
 * @since 1.2.0
 */
public class Mock4KJavaIntegrationTest {

    private Gson gson;

    @BeforeEach
    public void setUp() {
        gson = new Gson();
    }

    @Test
    public void testBasicMockGeneration() {
        // Test basic mock generation from Java
        Map<?, ?> result = mock(gson.fromJson("{\"name\": \"@string\", \"age\": \"@integer\"}", Map.class));
        assertNotNull(result);

        String jsonString = result.toString();
        assertNotNull(jsonString);
        assertFalse(jsonString.isEmpty());

        // Parse and verify structure
        assertTrue(result.containsKey("name"));
        assertTrue(result.containsKey("age"));
        assertInstanceOf(String.class, result.get("name"));
        assertInstanceOf(Number.class, result.get("age"));
    }

    @Test
    public void testComplexObjectMocking() {
        // Test complex object with nested structures
        String complexJson = """
                {
                    "user": {
                        "id": "@long",
                        "username": "@string",
                        "email": "@email",
                        "profile": {
                            "firstName": "@string",
                            "lastName": "@string",
                            "birthDate": "@date"
                        }
                    },
                    "preferences": {
                        "theme": "@string",
                        "notifications": "@boolean"
                    }
                }
                """;

        Object mockResult = mock(gson.fromJson(complexJson, Map.class));
        assertNotNull(mockResult);

        Map<?, ?> result = gson.fromJson(mockResult.toString(), Map.class);

        // Verify top-level structure
        assertTrue(result.containsKey("user"));
        assertTrue(result.containsKey("preferences"));

        // Verify nested user object
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) result.get("user");
        assertTrue(user.containsKey("id"));
        assertTrue(user.containsKey("username"));
        assertTrue(user.containsKey("email"));
        assertTrue(user.containsKey("profile"));

        // Verify nested profile object
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) user.get("profile");
        assertTrue(profile.containsKey("firstName"));
        assertTrue(profile.containsKey("lastName"));
        assertTrue(profile.containsKey("birthDate"));
    }

    @Test
    public void testArrayMocking() {
        // Test array generation
        String arrayJson = """
                {
                    "numbers|5": ["@integer"],
                    "strings|3": ["@string"],
                    "booleans|2": ["@boolean"]
                }
                """;

        Map<?, ?> result = mock(gson.fromJson(arrayJson, Map.class));
        assertNotNull(result);

        // Verify arrays
        assertTrue(result.containsKey("numbers"));
        assertTrue(result.containsKey("strings"));
        assertTrue(result.containsKey("booleans"));

        @SuppressWarnings("unchecked")
        List<Object> numbers = (List<Object>) result.get("numbers");
        assertEquals(5, numbers.size());

        @SuppressWarnings("unchecked")
        List<Object> strings = (List<Object>) result.get("strings");
        assertEquals(3, strings.size());

        @SuppressWarnings("unchecked")
        List<Object> booleans = (List<Object>) result.get("booleans");
        assertEquals(2, booleans.size());
    }

    @Test
    public void testCustomExtensions() {
        // Test custom extensions from Java
        GlobalMockConf.Random.extend("customJavaString", () -> "Hello from Java");
        GlobalMockConf.Random.extend("customJavaNumber", () -> 42);

        Map<?, ?> result = mock(gson.fromJson(
                """
                        {
                            "message": "@customJavaString",
                            "value": "@customJavaNumber"
                        }
                        """,
                Map.class
        ));

        assertNotNull(result);

        assertEquals("Hello from Java", result.get("message"));
        assertEquals(42, result.get("value")); // Gson parses numbers as Double
    }

    @Test
    public void testBeanMocking() {
        // Test bean-style object mocking
        String beanJson = """
                {
                    "id": "@long",
                    "name": "@string",
                    "active": "@boolean",
                    "createdAt": "@date",
                    "tags|3": ["@string"],
                    "metadata": {
                        "version": "@int",
                        "author": "@string"
                    }
                }
                """;

        Map<?, ?> result = (Map<?, ?>) mock(gson.fromJson(beanJson, Map.class));
        assertNotNull(result);

        // Verify all fields are present and have correct types
        assertDoesNotThrow(() -> Long.parseLong(result.get("id").toString()));
        assertInstanceOf(String.class, result.get("name"));
        assertDoesNotThrow(() -> Boolean.parseBoolean(result.get("active").toString()));
        assertInstanceOf(String.class, result.get("createdAt"));
        assertInstanceOf(List.class, result.get("tags"));
        assertInstanceOf(Map.class, result.get("metadata"));
    }

    @Test
    public void testErrorHandling() {
        Object invalidJson = mock(List.of("invalid json"));
        assertNotNull(invalidJson);

        // Test with empty JSON
        Map<?, ?> emptyResult = mock(gson.fromJson("{}", Map.class));
        assertNotNull(emptyResult);

        assertTrue(emptyResult.isEmpty());
    }

    @Test
    public void testSpecialCharactersAndUnicode() {
        // Test handling of special characters and Unicode
        GlobalMockConf.Random.extend("unicodeString", () -> "测试中文字符 🚀 Special chars: @#$%^&*()");

        Map<?, ?> result = mock(gson.fromJson("""
                {
                    "unicode": "@unicodeString",
                    "normal": "@string"
                }
                """, Map.class));

        assertNotNull(result);

        assertEquals("测试中文字符 🚀 Special chars: @#$%^&*()", result.get("unicode"));
        assertInstanceOf(String.class, result.get("normal"));
    }

    @Test
    public void testLargeDataGeneration() {
        // Test generation of larger datasets
        String largeArrayJson = """
                {
                    "largeArray|100": ["@string"],
                    "nestedObjects": [
                        {
                            "id": "@int",
                            "data": "@string"
                        }
                    ]
                }
                """;

        Map<?, ?> result = mock(gson.fromJson(largeArrayJson, Map.class));
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        List<Object> largeArray = (List<Object>) result.get("largeArray");
        assertEquals(100, largeArray.size());

        // Verify all elements are strings
        for (Object item : largeArray) {
            assertInstanceOf(String.class, item);
        }
    }

    @Test
    public void testConcurrentAccess() {
        // Test thread safety (basic test)
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                try {
                    Object mockResult = mock(gson.fromJson("{\"id\": \"@int\", \"name\": \"@string\"}", Map.class));
                    assertNotNull(mockResult);
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify no exceptions occurred
        assertTrue(exceptions.isEmpty(), "Concurrent access should not cause exceptions");
    }
}