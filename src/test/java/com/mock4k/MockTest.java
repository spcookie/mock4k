package com.mock4k;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Mock4K Java版本测试
 */
public class MockTest {
    
    @Test
    @DisplayName("基本mock测试")
    public void testBasicMock() {
        String result = (String) Mock.mock("Hello World");
        assertEquals("Hello World", result);
        
        Integer number = (Integer) Mock.mock(42);
        assertEquals(42, number);
        
        Boolean bool = (Boolean) Mock.mock(true);
        assertTrue(bool);
    }
    
    @Test
    @DisplayName("字符串规则测试")
    public void testStringRules() {
        // 重复规则
        Map<String, Object> template = new HashMap<>();
        template.put("name|3", "Hello");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) Mock.mock(template);
        assertEquals("HelloHelloHello", result.get("name"));
        
        // 范围规则
        template.clear();
        template.put("text|2-4", "Hi");
        
        result = (Map<String, Object>) Mock.mock(template);
        String text = (String) result.get("text");
        assertTrue(text.equals("HiHi") || text.equals("HiHiHi") || text.equals("HiHiHiHi"));
    }
    
    @Test
    @DisplayName("数字规则测试")
    public void testNumberRules() {
        // 范围规则
        Map<String, Object> template = new HashMap<>();
        template.put("age|18-65", 0);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) Mock.mock(template);
        Integer age = (Integer) result.get("age");
        assertTrue(age >= 18 && age <= 65);
        
        // 浮点数规则
        template.clear();
        template.put("price|1-100.1-3", 0.0);
        
        result = (Map<String, Object>) Mock.mock(template);
        Double price = (Double) result.get("price");
        assertTrue(price >= 1.0 && price <= 100.999);
    }
    
    @Test
    @DisplayName("数组规则测试")
    public void testArrayRules() {
        // 从数组中选择
        Map<String, Object> template = new HashMap<>();
        List<String> colors = Arrays.asList("red", "green", "blue");
        template.put("color|1", colors);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) Mock.mock(template);
        @SuppressWarnings("unchecked")
        List<String> selectedColors = (List<String>) result.get("color");
        assertEquals(1, selectedColors.size());
        assertTrue(colors.contains(selectedColors.get(0)));
        
        // 重复数组
        template.clear();
        template.put("numbers|2", Arrays.asList(1, 2, 3));
        
        result = (Map<String, Object>) Mock.mock(template);
        @SuppressWarnings("unchecked")
        List<Integer> numbers = (List<Integer>) result.get("numbers");
        assertEquals(6, numbers.size()); // 2 * 3 = 6
    }
    
    @Test
    @DisplayName("占位符测试")
    public void testPlaceholders() {
        String result = (String) Mock.mock("@name");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        result = (String) Mock.mock("@email");
        assertTrue(result.contains("@"));
        
        result = (String) Mock.mock("@date");
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
        
        result = (String) Mock.mock("@integer(1,100)");
        int num = Integer.parseInt(result);
        assertTrue(num >= 1 && num <= 100);
    }
    
    @Test
    @DisplayName("复杂模板测试")
    public void testComplexTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("id|+1", 1);
        template.put("name", "@name");
        template.put("email", "@email");
        template.put("age|18-65", 0);
        template.put("isActive", "@boolean");
        
        List<String> hobbies = Arrays.asList("reading", "swimming", "coding", "gaming");
        template.put("hobbies|1-3", hobbies);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) Mock.mock(template);
        
        assertNotNull(result.get("id"));
        assertNotNull(result.get("name"));
        assertTrue(((String) result.get("email")).contains("@"));
        
        Integer age = (Integer) result.get("age");
        assertTrue(age >= 18 && age <= 65);
        
        @SuppressWarnings("unchecked")
        List<String> resultHobbies = (List<String>) result.get("hobbies");
        assertTrue(resultHobbies.size() >= 1 && resultHobbies.size() <= 3);
    }
    
    @Test
    @DisplayName("增量规则测试")
    public void testIncrementRule() {
        Map<String, Object> template = new HashMap<>();
        template.put("id|+1", 1);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result1 = (Map<String, Object>) Mock.mock(template);
        @SuppressWarnings("unchecked")
        Map<String, Object> result2 = (Map<String, Object>) Mock.mock(template);
        
        Integer id1 = (Integer) result1.get("id");
        Integer id2 = (Integer) result2.get("id");
        
        assertEquals(1, id2 - id1); // 增量为1
    }
    
    @Test
    @DisplayName("布尔规则测试")
    public void testBooleanRule() {
        String result = (String) Mock.mock("@boolean");
        assertTrue(result.equals("true") || result.equals("false"));
    }
    
    @Test
    @DisplayName("对象规则测试")
    public void testObjectRule() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "@name");
        user.put("age", "@integer(18,65)");
        
        Map<String, Object> template = new HashMap<>();
        template.put("users|3", user);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) Mock.mock(template);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        
        assertEquals(3, users.size());
    }
    
    @Test
    @DisplayName("Mock.Random方法测试")
    public void testMockRandomMethods() {
        // 测试基本方法
        assertNotNull(Mock.Random.bool());
        
        int num = Mock.Random.integer(1, 10);
        assertTrue(num >= 1 && num <= 10);
        
        double floating = Mock.Random.floating(0.0, 1.0);
        assertTrue(floating >= 0.0 && floating <= 1.0);
        
        assertNotNull(Mock.Random.string(5));
        assertEquals(5, Mock.Random.string(5).length());
        
        // 测试日期时间
        assertNotNull(Mock.Random.date());
        assertNotNull(Mock.Random.time());
        assertNotNull(Mock.Random.datetime());
        
        // 测试文本
        assertNotNull(Mock.Random.word());
        assertNotNull(Mock.Random.sentence());
        assertNotNull(Mock.Random.paragraph());
        
        // 测试姓名
        assertNotNull(Mock.Random.name());
        assertNotNull(Mock.Random.cname());
        
        // 测试网络
        assertTrue(Mock.Random.email().contains("@"));
        assertTrue(Mock.Random.url().startsWith("http"));
        
        // 测试颜色
        assertTrue(Mock.Random.color().startsWith("#"));
        assertTrue(Mock.Random.rgb().startsWith("rgb("));
        
        // 测试其他
        assertNotNull(Mock.Random.guid());
        assertNotNull(Mock.Random.id());
    }
}