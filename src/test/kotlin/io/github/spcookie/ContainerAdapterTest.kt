package io.github.spcookie

import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test class for containerAdapter functionality
 *
 * @author spcookie
 * @since 1.2.0
 */
class ContainerAdapterTest {

    @Test
    fun testRegisterCustomContainerTypes() {
        // Access the global container adapter through GlobalMockConf object
        val containerAdapter = GlobalMockConf.ContainerAdapter

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
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        )

        // Test 4: Register a container type with custom analyzer and mapper
        containerAdapter.register(
            "com.example.MyCustomContainer",
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE,
            analyzer = { types: List<KType>, _: Any?, config: BeanMockConfig, _: Int ->
                // Custom analysis logic for TypeIntrospect
                "@custom_placeholder"
            },
            mapper = { value: Any?, targetType: List<KType>, config: BeanMockConfig ->
                // Custom mapping logic for TypeMockMapper
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
        val containerAdapter = GlobalMockConf.ContainerAdapter
        containerAdapter.register(
            "com.test.TestContainer",
            ContainerAdapter.ContainerBehavior.SINGLE_VALUE
        )


        // Test standard Java container types
        assertTrue(isCollectionType(List::class))
        assertTrue(isCollectionType(Set::class))
        assertTrue(isCollectionType(Map::class))
        assertTrue(isContainerType(Optional::class, containerAdapter))
        assertTrue(isContainerType(Stream::class, containerAdapter))


        // Test non-container types
        assertFalse(isContainerType(String::class, containerAdapter))
        assertFalse(isContainerType(Integer::class, containerAdapter))
    }

}