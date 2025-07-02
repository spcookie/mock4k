package com.mock4k.example;

import com.mock4k.Mock;
import com.mock4k.random.MockRandom;

import java.util.*;

/**
 * Mock4K Java适配示例
 * 演示如何在Java中使用Kotlin版本的Mock4K库
 */
public class JavaExample {
    
    public static void main(String[] args) {
        System.out.println("=== Mock4K Java适配示例 ===");
        
        // 1. 基本mock使用
        basicMockExample();
        
        // 2. 字符串规则
        stringRuleExample();
        
        // 3. 数字规则
        numberRuleExample();
        
        // 4. 数组规则
        arrayRuleExample();
        
        // 5. 占位符
        placeholderExample();
        
        // 6. 复杂嵌套结构
        complexTemplateExample();
        
        // 7. 中文内容生成
        chineseContentExample();
        
        // 8. 直接使用Mock.Random
        mockRandomExample();
    }
    
    /**
     * 基本mock使用
     */
    private static void basicMockExample() {
        System.out.println("\n--- 基本Mock ---");
        
        // 字符串
        Object result = Mock.mock("Hello World");
        System.out.println("字符串: " + result);
        
        // 数字
        result = Mock.mock(42);
        System.out.println("数字: " + result);
        
        // 布尔值
        result = Mock.mock(true);
        System.out.println("布尔值: " + result);
    }
    
    /**
     * 字符串规则示例
     */
    private static void stringRuleExample() {
        System.out.println("\n--- 字符串规则 ---");
        
        // 重复3次
        Map<String, Object> template = new HashMap<>();
        template.put("name|3", "Hello");
        Object result = Mock.mock(template);
        System.out.println("重复3次: " + result);
        
        // 重复2-5次
        template.clear();
        template.put("text|2-5", "Hi");
        result = Mock.mock(template);
        System.out.println("重复2-5次: " + result);
    }
    
    /**
     * 数字规则示例
     */
    private static void numberRuleExample() {
        System.out.println("\n--- 数字规则 ---");
        
        // 整数范围
        Map<String, Object> template = new HashMap<>();
        template.put("age|18-65", 0);
        Object result = Mock.mock(template);
        System.out.println("年龄(18-65): " + result);
        
        // 浮点数范围
        template.clear();
        template.put("price|1-100.1-3", 0.0);
        result = Mock.mock(template);
        System.out.println("价格(1-100.1-3): " + result);
        
        // 增量
        template.clear();
        template.put("id|+1", 1);
        System.out.println("ID增量1: " + Mock.mock(template));
        System.out.println("ID增量2: " + Mock.mock(template));
        System.out.println("ID增量3: " + Mock.mock(template));
    }
    
    /**
     * 数组规则示例
     */
    private static void arrayRuleExample() {
        System.out.println("\n--- 数组规则 ---");
        
        // 从数组中选择1-3个元素
        Map<String, Object> template = new HashMap<>();
        List<String> colors = Arrays.asList("red", "green", "blue", "yellow", "purple");
        template.put("colors|1-3", colors);
        Object result = Mock.mock(template);
        System.out.println("选择颜色(1-3个): " + result);
        
        // 重复数组2次
        template.clear();
        template.put("numbers|2", Arrays.asList(1, 2, 3));
        result = Mock.mock(template);
        System.out.println("重复数组2次: " + result);
    }
    
    /**
     * 占位符示例
     */
    private static void placeholderExample() {
        System.out.println("\n--- 占位符 ---");
        
        System.out.println("姓名: " + Mock.mock("@name"));
        System.out.println("邮箱: " + Mock.mock("@email"));
        System.out.println("日期: " + Mock.mock("@date"));
        System.out.println("时间: " + Mock.mock("@time"));
        System.out.println("整数(1-100): " + Mock.mock("@integer(1,100)"));
        System.out.println("字符串(5位): " + Mock.mock("@string(5)"));
        System.out.println("布尔值: " + Mock.mock("@boolean"));
        System.out.println("颜色: " + Mock.mock("@color"));
        System.out.println("URL: " + Mock.mock("@url"));
    }
    
    /**
     * 复杂模板示例
     */
    private static void complexTemplateExample() {
        System.out.println("\n--- 复杂模板 ---");
        
        // 用户对象模板
        Map<String, Object> userTemplate = new HashMap<>();
        userTemplate.put("id|+1", 1);
        userTemplate.put("name", "@name");
        userTemplate.put("email", "@email");
        userTemplate.put("age|18-65", 0);
        userTemplate.put("isActive", "@boolean");
        
        // 地址对象
        Map<String, Object> address = new HashMap<>();
        address.put("street", "@sentence");
        address.put("city", "@word");
        address.put("zipCode", "@integer(10000,99999)");
        userTemplate.put("address", address);
        
        // 爱好数组
        List<String> hobbies = Arrays.asList("reading", "swimming", "coding", "gaming", "music");
        userTemplate.put("hobbies|1-3", hobbies);
        
        // 生成用户列表
        Map<String, Object> template = new HashMap<>();
        template.put("users|3", userTemplate);
        
        Object result = Mock.mock(template);
        System.out.println("用户列表: " + result);
    }
    
    /**
     * 中文内容示例
     */
    private static void chineseContentExample() {
        System.out.println("\n--- 中文内容 ---");
        
        System.out.println("中文姓名: " + Mock.mock("@cname"));
        System.out.println("中文词语: " + Mock.mock("@cword"));
        System.out.println("中文句子: " + Mock.mock("@csentence"));
        System.out.println("中文标题: " + Mock.mock("@ctitle"));
        
        // 中文用户模板
        Map<String, Object> chineseUser = new HashMap<>();
        chineseUser.put("姓名", "@cname");
        chineseUser.put("年龄|18-65", 0);
        chineseUser.put("简介", "@cparagraph");
        
        Object result = Mock.mock(chineseUser);
        System.out.println("中文用户: " + result);
    }
    
    /**
     * 直接使用Mock.Random示例
     */
    private static void mockRandomExample() {
        System.out.println("\n--- Mock.Random方法 ---");
        
        MockRandom random = Mock.Random;
        
        System.out.println("随机布尔: " + random.bool());
        System.out.println("随机整数(1-100): " + random.integer(1, 100));
        System.out.println("随机浮点数: " + random.floating(0.0, 1.0));
        System.out.println("随机字符串(8位): " + random.string(8));
        System.out.println("随机日期: " + random.date());
        System.out.println("随机时间: " + random.time());
        System.out.println("随机英文名: " + random.name());
        System.out.println("随机中文名: " + random.cname());
        System.out.println("随机邮箱: " + random.email());
        System.out.println("随机URL: " + random.url());
        System.out.println("随机IP: " + random.ip());
        System.out.println("随机颜色: " + random.color());
        System.out.println("随机GUID: " + random.guid());
        
        // 从列表中选择
        List<String> fruits = Arrays.asList("apple", "banana", "orange", "grape");
        System.out.println("随机水果: " + random.pick(fruits));
        
        // 打乱列表
        System.out.println("打乱水果: " + random.shuffle(fruits));
    }
}