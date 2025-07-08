package io.github.spcookie

import kotlin.reflect.KType
import kotlin.test.*

/**
 * Test class for ContainerAdapter functionality
 *
 * @author spcookie
 * @since 1.2.0
 */
class ContainerAdapterTest {

    @Test
    fun testRegisterCustomContainerTypes() {
        // Access the global container adapter through Mocks object
        val containerAdapter = Mocks.ContainerAdapter

        // Test 1: Register a simple container type with SINGLE_VALUE behavior
        containerAdapter.register(
            "com.example.MyContainer",
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        )

        // Test 2: Register a stream-like container type
        containerAdapter.register(
            "com.example.MyStream",
            ContainerAdapter.ContainerBehavior.STREAM_VALUES
        )

        // Test 3: Register an Either-like container type
        containerAdapter.register(
            "com.example.MyEither",
            ContainerAdapter.ContainerBehavior.RIGHT_TYPE
        )

        // Test 4: Register a container type with custom analyzer and mapper
        containerAdapter.register(
            "com.example.MyCustomContainer",
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE,
            analyzer = { type: KType, _: Any?, config: BeanMockConfig, _: Int ->
                // Custom analysis logic for BeanIntrospect
                val wrappedType = type.arguments.firstOrNull()?.type
                if (wrappedType != null) {
                    "@custom_placeholder"
                } else {
                    "@string"
                }
            },
            mapper = { value: Any?, targetType: KType, config: BeanMockConfig ->
                // Custom mapping logic for BeanMockMapper
                // This would create an instance of MyCustomContainer with the wrapped value
                // For demonstration purposes, we'll just return the value
                value
            }
        )

        // Verify that the registered prefixes are available
        val registeredPrefixes = containerAdapter.getRegisteredPrefixes()
        assertTrue(registeredPrefixes.contains("com.example.MyContainer"))
        assertTrue(registeredPrefixes.contains("com.example.MyStream"))
        assertTrue(registeredPrefixes.contains("com.example.MyEither"))
        assertTrue(registeredPrefixes.contains("com.example.MyCustomContainer"))
    }

    @Test
    fun testIsContainerType() {
        // Register test container types
        val containerAdapter = Mocks.ContainerAdapter
        containerAdapter.register(
            "com.test.TestContainer",
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        )

        // Test standard Java container types
        assertTrue(isContainerType("java.util.List"))
        assertTrue(isContainerType("java.util.Set"))
        assertTrue(isContainerType("java.util.Map"))
        assertTrue(isContainerType("java.util.Optional"))
        assertTrue(isContainerType("java.util.stream.Stream"))

        // Test registered custom container types
        assertTrue(isContainerType("com.test.TestContainer"))
        assertTrue(isContainerType("com.test.TestContainer.InnerClass"))

        // Test non-container types
        assertFalse(isContainerType("java.lang.String"))
        assertFalse(isContainerType("java.lang.Integer"))
        assertFalse(isContainerType("com.other.NonContainer"))
    }

    @Test
    fun testContainerBehaviorTypes() {
        val containerAdapter = Mocks.ContainerAdapter

        // Test different behavior types
        containerAdapter.register(
            "com.test.SingleValue",
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        )
        containerAdapter.register(
            "com.test.StreamValues",
            ContainerAdapter.ContainerBehavior.STREAM_VALUES
        )
        containerAdapter.register(
            "com.test.RightType",
            ContainerAdapter.ContainerBehavior.RIGHT_TYPE
        )

        // Verify behavior retrieval
        val singleValueBehavior = containerAdapter.getContainerBehavior("com.test.SingleValue")
        val streamValuesBehavior = containerAdapter.getContainerBehavior("com.test.StreamValues")
        val rightTypeBehavior = containerAdapter.getContainerBehavior("com.test.RightType")

        assertNotNull(singleValueBehavior)
        assertNotNull(streamValuesBehavior)
        assertNotNull(rightTypeBehavior)

        assertEquals(ContainerAdapter.ContainerBehavior.SINGLE_VALUE, singleValueBehavior)
        assertEquals(ContainerAdapter.ContainerBehavior.STREAM_VALUES, streamValuesBehavior)
        assertEquals(ContainerAdapter.ContainerBehavior.RIGHT_TYPE, rightTypeBehavior)
    }

    @Test
    fun testContainerAdapterIntegration() {
        // Test the integration between ContainerAdapter and other components
        val containerAdapter = Mocks.ContainerAdapter

        // Register a test container type
        containerAdapter.register(
            "com.integration.TestContainer",
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        )

        // Verify that isContainerType works with the registered type
        assertTrue(isContainerType("com.integration.TestContainer"))
        assertTrue(isContainerType("com.integration.TestContainer.NestedClass"))

        // Verify that the container adapter is accessible through Mocks
        val mocksContainerAdapter = Mocks.ContainerAdapter
        assertNotNull(mocksContainerAdapter)
        assertTrue(mocksContainerAdapter.getRegisteredPrefixes().contains("com.integration.TestContainer"))
    }
}