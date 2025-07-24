package io.github.spcookie;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SingleTypeTest {

    @Test
    public void testSingleTypeMocking() {
        BeanMethodMock mock = GlobalMocks.load(BeanMethodMock.class);

        Map<String, Object> map = mock.getMap();
        BeanMethodMock.TestEnum testEnum = mock.getEnum();
        Map<BeanMethodMock.TestBean, String> beanMap = mock.getBeanMap();

        assertNotNull(map);
        assertFalse(map.isEmpty());
        assertNotNull(beanMap);
        assertFalse(beanMap.isEmpty());
        assertNotNull(testEnum);

        assertDoesNotThrow(() -> {
            BeanMethodMock.TestEnum anEnum = GlobalMocks.mock(BeanMethodMock.TestEnum.class);
            assertNotNull(anEnum);
        });
    }

    public static class BeanMethodMock {

        public Map<String, Object> getMap() {
            return Map.of();
        }

        public TestEnum getEnum() {
            return null;
        }

        public Map<TestBean, String> getBeanMap() {
            return Map.of();
        }

        public static class TestBean {
            String name;
            int age;
            private boolean isAlive;
            private double height;
            private float weight;
            private long money;
            private short number;
            private byte number2;
            private char character;
        }

        public enum TestEnum {
            TEST,
            TEST2,
            TEST3,
            TEST4,
            TEST5,
            TEST6,
            TEST7,
            TEST8,
            TEST9,
            TEST10
        }

    }

}
