package io.github.spcookie;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class SimpleTest {

    @Test
    public void test_kotlin_friends_me() {
        Mocks.Random.extend("extend", () -> "你好");
        Map<?, ?> result = (Map<?, ?>) MocksKt.mock("""
                {"name": "@extend"}
                """);
        System.out.println(result);
    }

}
