package io.github.spcookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScopeMockTest {

    @Test
    public void testScopeMocking() {
        ScopeMock inheritScopeMock = GlobalMocks.inherit();
        ScopeMock scopeMock = ScopeMock.create();
        Assertions.assertSame(GlobalMockConf.Random, inheritScopeMock.getRandom());
        Assertions.assertNotSame(GlobalMockConf.Random, scopeMock.getRandom());
    }

}
