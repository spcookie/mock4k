package io.github.spcookie

data class UserWithMockParamTest(
    @Mock.Property(rule = Mock.Rule(min = 1000, max = 9999))
    var id: Long = 0L,
    @Mock.Property(placeholder = Mock.Placeholder(value = "@FIRST @LAST"))
    var name: String = "",
    @Mock.Property(rule = Mock.Rule(min = 18, max = 65))
    var age: Int = 0
)