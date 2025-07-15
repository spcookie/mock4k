package io.github.spcookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class SingleTypeTest {

    @Test
    public void testSingleTypeMocking() {
        BeanMethodMock mock = MocksKt.load(BeanMethodMock.class);

        Map<String, Object> map = mock.getMap();

        Assertions.assertNotNull(map);
        Assertions.assertNotNull(map.get("name"));
        Assertions.assertNotEquals("John Doe", map.get("name"));
    }

    public static class BeanMethodMock {

        public Map<String, Object> getMap() {
            return Map.of("name", "John Doe");
        }

    }

}
