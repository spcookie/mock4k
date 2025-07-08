package io.github.spcookie;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.spcookie.MocksKt.mock;
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
        Object mockResult = mock("{\"name\": \"@string\", \"age\": \"@int\"}");
        assertNotNull(mockResult);

        String jsonString = mockResult.toString();
        assertNotNull(jsonString);
        assertFalse(jsonString.isEmpty());

        // Parse and verify structure
        Map<?, ?> result = gson.fromJson(jsonString, Map.class);
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

        Object mockResult = mock(complexJson);
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
                    "numbers": "@int[5]",
                    "strings": "@string[3]",
                    "booleans": "@boolean[2]"
                }
                """;

        Object mockResult = mock(arrayJson);
        assertNotNull(mockResult);

        Map<?, ?> result = gson.fromJson(mockResult.toString(), Map.class);

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
        Mocks.Random.extend("customJavaString", () -> "Hello from Java");
        Mocks.Random.extend("customJavaNumber", () -> 42);

        Object mockResult = mock("""
                {
                    "message": "@customJavaString",
                    "value": "@customJavaNumber"
                }
                """);

        assertNotNull(mockResult);

        Map<?, ?> result = gson.fromJson(mockResult.toString(), Map.class);
        assertEquals("Hello from Java", result.get("message"));
        assertEquals(42.0, result.get("value")); // Gson parses numbers as Double
    }

    @Test
    public void testTypeAdapterIntegration() {
        // Test TypeAdapter functionality from Java
        TypeAdapter typeAdapter = Mocks.TypeAdapter;
        assertNotNull(typeAdapter);

        // Test basic type recognition
        assertTrue(typeAdapter.isBasicType("java.lang.String"));
        assertTrue(typeAdapter.isBasicType("java.lang.Integer"));
        assertTrue(typeAdapter.isBasicType("java.lang.Boolean"));
        assertFalse(typeAdapter.isBasicType("com.example.CustomClass"));

        // Test collection type recognition
        assertTrue(typeAdapter.isCollectionType("java.util.List"));
        assertTrue(typeAdapter.isCollectionType("java.util.Set"));
        assertTrue(typeAdapter.isCollectionType("java.util.Map"));
        assertFalse(typeAdapter.isCollectionType("java.lang.String"));
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
                    "tags": "@string[3]",
                    "metadata": {
                        "version": "@int",
                        "author": "@string"
                    }
                }
                """;

        Object mockResult = mock(beanJson);
        assertNotNull(mockResult);

        Map<?, ?> result = gson.fromJson(mockResult.toString(), Map.class);

        // Verify all fields are present and have correct types
        assertInstanceOf(Number.class, result.get("id"));
        assertInstanceOf(String.class, result.get("name"));
        assertInstanceOf(Boolean.class, result.get("active"));
        assertInstanceOf(String.class, result.get("createdAt"));
        assertInstanceOf(List.class, result.get("tags"));
        assertInstanceOf(Map.class, result.get("metadata"));
    }

    @Test
    public void testErrorHandling() {
        // Test error handling with invalid JSON
        assertThrows(Exception.class, () -> {
            mock("invalid json");
        });

        // Test with empty JSON
        Object emptyMock = mock("{}");
        assertNotNull(emptyMock);

        Map<?, ?> emptyResult = gson.fromJson(emptyMock.toString(), Map.class);
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    public void testSpecialCharactersAndUnicode() {
        // Test handling of special characters and Unicode
        Mocks.Random.extend("unicodeString", () -> "测试中文字符 🚀 Special chars: @#$%^&*()");

        Object mockResult = mock("""
                {
                    "unicode": "@unicodeString",
                    "normal": "@string"
                }
                """);

        assertNotNull(mockResult);

        Map<?, ?> result = gson.fromJson(mockResult.toString(), Map.class);
        assertEquals("测试中文字符 🚀 Special chars: @#$%^&*()", result.get("unicode"));
        assertInstanceOf(String.class, result.get("normal"));
    }

    @Test
    public void testLargeDataGeneration() {
        // Test generation of larger datasets
        String largeArrayJson = """
                {
                    "largeArray": "@string[100]",
                    "nestedObjects": [
                        {
                            "id": "@int",
                            "data": "@string"
                        }
                    ]
                }
                """;

        Object mockResult = mock(largeArrayJson);
        assertNotNull(mockResult);

        Map<?, ?> result = gson.fromJson(mockResult.toString(), Map.class);

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
                    Object mockResult = mock("{\"id\": \"@int\", \"name\": \"@string\"}");
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