package io.github.spcookie;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SingleTypeTest {

    @Test
    public void testSingleTypeMocking() {
        BeanMethodMock mock = MockUtils.load(BeanMethodMock.class);

        Map<String, Object> map = mock.getMap();
        BeanMethodMock.TestEnum testEnum = mock.getEnum();

        assertNotNull(map);
        assertFalse(map.isEmpty());
        assertNotNull(testEnum);

        assertDoesNotThrow(() -> {
            BeanMethodMock.TestEnum anEnum = MockUtils.mock(BeanMethodMock.TestEnum.class);
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
