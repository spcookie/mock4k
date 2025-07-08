package io.github.spcookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.github.spcookie.ToolkitKt.isContainerType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Java test class for ContainerAdapter functionality
 * Tests Java interoperability and provides additional test coverage
 *
 * @author spcookie
 * @since 1.2.0
 */
public class ContainerAdapterJavaTest {

    private ContainerAdapter containerAdapter;

    @BeforeEach
    public void setUp() {
        containerAdapter = Mocks.ContainerAdapter;
    }

    @Test
    public void testJavaContainerTypeRegistration() {
        // Test registering container types from Java
        containerAdapter.register(
                "com.example.java.JavaContainer",
                ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        );

        containerAdapter.register(
                "com.example.java.JavaList",
                ContainerAdapter.ContainerBehavior.STREAM_VALUES
        );

        containerAdapter.register(
                "com.example.java.JavaOptional",
                ContainerAdapter.ContainerBehavior.RIGHT_TYPE
        );

        // Verify registration
        Set<String> registeredPrefixes = containerAdapter.getRegisteredPrefixes();
        assertTrue(registeredPrefixes.contains("com.example.java.JavaContainer"));
        assertTrue(registeredPrefixes.contains("com.example.java.JavaList"));
        assertTrue(registeredPrefixes.contains("com.example.java.JavaOptional"));
    }

    @Test
    public void testStandardJavaContainerTypes() {
        // Test recognition of standard Java container types
        assertTrue(isContainerType("java.util.ArrayList"));
        assertTrue(isContainerType("java.util.LinkedList"));
        assertTrue(isContainerType("java.util.HashSet"));
        assertTrue(isContainerType("java.util.TreeSet"));
        assertTrue(isContainerType("java.util.HashMap"));
        assertTrue(isContainerType("java.util.TreeMap"));
        assertTrue(isContainerType("java.util.Optional"));
        assertTrue(isContainerType("java.util.stream.Stream"));
        assertTrue(isContainerType("java.util.concurrent.ConcurrentHashMap"));
    }

    @Test
    public void testNonContainerTypes() {
        // Test that non-container types are correctly identified
        assertFalse(isContainerType("java.lang.String"));
        assertFalse(isContainerType("java.lang.Integer"));
        assertFalse(isContainerType("java.lang.Double"));
        assertFalse(isContainerType("java.math.BigDecimal"));
        assertFalse(isContainerType("java.time.LocalDateTime"));
        assertFalse(isContainerType("com.example.NonContainer"));
    }

    @Test
    public void testContainerBehaviorRetrieval() {
        // Register test containers with different behaviors
        containerAdapter.register(
                "com.test.java.SingleValue",
                ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        );
        containerAdapter.register(
                "com.test.java.StreamValues",
                ContainerAdapter.ContainerBehavior.STREAM_VALUES
        );
        containerAdapter.register(
                "com.test.java.RightType",
                ContainerAdapter.ContainerBehavior.RIGHT_TYPE
        );

        // Test behavior retrieval
        ContainerAdapter.ContainerBehavior singleValue =
                containerAdapter.getContainerBehavior("com.test.java.SingleValue");
        ContainerAdapter.ContainerBehavior streamValues =
                containerAdapter.getContainerBehavior("com.test.java.StreamValues");
        ContainerAdapter.ContainerBehavior rightType =
                containerAdapter.getContainerBehavior("com.test.java.RightType");

        assertEquals(ContainerAdapter.ContainerBehavior.SINGLE_VALUE, singleValue);
        assertEquals(ContainerAdapter.ContainerBehavior.STREAM_VALUES, streamValues);
        assertEquals(ContainerAdapter.ContainerBehavior.RIGHT_TYPE, rightType);
    }

    @Test
    public void testCustomContainerWithNestedClasses() {
        // Test container type recognition with nested classes
        containerAdapter.register(
                "com.example.CustomContainer",
                ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        );

        // Test main class and nested classes
        assertTrue(isContainerType("com.example.CustomContainer"));
        assertTrue(isContainerType("com.example.CustomContainer$InnerClass"));
        assertTrue(isContainerType("com.example.CustomContainer.StaticNestedClass"));
        assertTrue(isContainerType("com.example.CustomContainer$1AnonymousClass"));
    }

    @Test
    public void testContainerAdapterAccessibility() {
        // Test that ContainerAdapter is accessible through Mocks
        ContainerAdapter mocksAdapter = Mocks.ContainerAdapter;
        assertNotNull(mocksAdapter);
        assertSame(containerAdapter, mocksAdapter);
    }

    @Test
    public void testJavaCollectionFrameworkTypes() {
        // Test comprehensive Java Collection Framework types
        String[] collectionTypes = {
                "java.util.Collection",
                "java.util.List",
                "java.util.Set",
                "java.util.Queue",
                "java.util.Deque",
                "java.util.Map",
                "java.util.SortedSet",
                "java.util.SortedMap",
                "java.util.NavigableSet",
                "java.util.NavigableMap",
                "java.util.concurrent.BlockingQueue",
                "java.util.concurrent.BlockingDeque",
                "java.util.concurrent.ConcurrentMap",
                "java.util.concurrent.ConcurrentNavigableMap"
        };

        for (String type : collectionTypes) {
            assertTrue(isContainerType(type),
                    "Type should be recognized as container: " + type);
        }
    }

    @Test
    public void testStreamAndOptionalTypes() {
        // Test Stream API and Optional types
        String[] streamTypes = {
                "java.util.stream.Stream",
                "java.util.stream.IntStream",
                "java.util.stream.LongStream",
                "java.util.stream.DoubleStream",
                "java.util.Optional",
                "java.util.OptionalInt",
                "java.util.OptionalLong",
                "java.util.OptionalDouble"
        };

        for (String type : streamTypes) {
            assertTrue(isContainerType(type),
                    "Type should be recognized as container: " + type);
        }
    }

    @Test
    public void testEdgeCases() {
        // Test edge cases and boundary conditions

        // Empty string
        assertFalse(isContainerType(""));

        // Null handling (if supported)
        try {
            assertFalse(isContainerType(null));
        } catch (Exception e) {
            // Null handling may throw exception, which is acceptable
        }

        // Very long class name
        String longClassName = "com.example." + "VeryLong".repeat(100) + ".Container";
        assertFalse(isContainerType(longClassName));

        // Class name with special characters
        assertFalse(isContainerType("com.example.Container@#$%"));
    }

    @Test
    public void testContainerRegistrationOverride() {
        // Test that re-registering a container type updates its behavior
        String containerType = "com.test.OverrideContainer";

        // Initial registration
        containerAdapter.register(containerType, ContainerAdapter.ContainerBehavior.SINGLE_VALUE);
        assertEquals(ContainerAdapter.ContainerBehavior.SINGLE_VALUE,
                containerAdapter.getContainerBehavior(containerType));

        // Override with different behavior
        containerAdapter.register(containerType, ContainerAdapter.ContainerBehavior.STREAM_VALUES);
        assertEquals(ContainerAdapter.ContainerBehavior.STREAM_VALUES,
                containerAdapter.getContainerBehavior(containerType));
    }
}