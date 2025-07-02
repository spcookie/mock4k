# Mock4K Java适配指南

本文档介绍如何在Java项目中使用Mock4K库。Mock4K是用Kotlin编写的，但完全兼容Java，可以在Java项目中无缝使用。

## 快速开始

### 1. 添加依赖

在你的Java项目中添加Mock4K依赖：

```gradle
dependencies {
    implementation 'com.mock4k:mock4k:1.0.0'
}
```

### 2. 基本使用

```java
import com.mock4k.Mock;

public class Example {
    public static void main(String[] args) {
        // 基本mock
        Object result = Mock.mock("Hello World");
        System.out.println(result); // "Hello World"
        
        // 使用模板
        Map<String, Object> template = new HashMap<>();
        template.put("name", "@name");
        template.put("age|18-65", 0);
        
        Object user = Mock.mock(template);
        System.out.println(user);
    }
}
```

## API参考

### Mock类

Mock4K的主要入口点，提供静态方法来生成模拟数据。

#### 方法

- `Mock.mock(Object template)` - 根据模板生成数据
- `Mock.mock(Object template, int count)` - 生成指定数量的数据
- `Mock.Random` - 访问随机数据生成器

### 数据模板定义规范

#### 1. 属性值规则

在Java中使用Map来定义对象模板：

```java
Map<String, Object> template = new HashMap<>();

// 字符串重复
template.put("name|3", "Hello");        // "HelloHelloHello"
template.put("text|2-5", "Hi");         // "HiHi" 到 "HiHiHiHiHi"

// 数字范围
template.put("age|18-65", 0);           // 18到65之间的整数
template.put("price|1-100.1-3", 0.0);   // 1.1到100.999之间的浮点数

// 增量
template.put("id|+1", 1);               // 1, 2, 3, 4...

// 布尔值
template.put("isActive|1", true);       // 随机true/false

// 数组选择
List<String> colors = Arrays.asList("red", "green", "blue");
template.put("color|1", colors);        // 随机选择1个颜色
template.put("colors|1-3", colors);     // 随机选择1-3个颜色

// 数组重复
template.put("numbers|2", Arrays.asList(1, 2, 3)); // [1,2,3,1,2,3]
```

#### 2. 占位符规则

使用`@placeholder`语法生成随机数据：

```java
// 基本类型
Mock.mock("@boolean");          // 随机布尔值
Mock.mock("@integer");          // 随机整数
Mock.mock("@integer(1,100)");   // 1-100之间的整数
Mock.mock("@float(0,1)");       // 0-1之间的浮点数
Mock.mock("@string(5)");        // 5位随机字符串

// 日期时间
Mock.mock("@date");             // 随机日期 "2023-05-15"
Mock.mock("@time");             // 随机时间 "14:30:25"
Mock.mock("@datetime");         // 随机日期时间
Mock.mock("@now");              // 当前时间戳

// 文本
Mock.mock("@word");             // 随机单词
Mock.mock("@sentence");         // 随机句子
Mock.mock("@paragraph");        // 随机段落
Mock.mock("@title");            // 随机标题

// 中文文本
Mock.mock("@cword");            // 随机中文词语
Mock.mock("@csentence");        // 随机中文句子
Mock.mock("@cparagraph");       // 随机中文段落
Mock.mock("@ctitle");           // 随机中文标题

// 姓名
Mock.mock("@first");            // 英文名
Mock.mock("@last");             // 英文姓
Mock.mock("@name");             // 英文全名
Mock.mock("@cfirst");           // 中文名
Mock.mock("@clast");            // 中文姓
Mock.mock("@cname");            // 中文全名

// 网络
Mock.mock("@url");              // 随机URL
Mock.mock("@domain");           // 随机域名
Mock.mock("@email");            // 随机邮箱
Mock.mock("@ip");               // 随机IP地址

// 颜色
Mock.mock("@color");            // 随机颜色 "#FF5733"
Mock.mock("@rgb");              // RGB颜色
Mock.mock("@rgba");             // RGBA颜色
Mock.mock("@hsl");              // HSL颜色

// 其他
Mock.mock("@guid");             // 随机GUID
Mock.mock("@id");               // 随机ID
Mock.mock("@image");            // 随机图片URL
Mock.mock("@image(300x200)");   // 指定尺寸的图片URL
```

### MockRandom类

直接使用随机数据生成器：

```java
import com.mock4k.random.MockRandom;

MockRandom random = Mock.Random;

// 基本类型
boolean bool = random.bool();
int number = random.integer(1, 100);
double floating = random.floating(0.0, 1.0);
String text = random.string(10);

// 日期时间
String date = random.date();
String time = random.time();
String datetime = random.datetime();

// 文本
String word = random.word();
String sentence = random.sentence();
String paragraph = random.paragraph();

// 姓名
String name = random.name();
String cname = random.cname();

// 网络
String email = random.email();
String url = random.url();
String ip = random.ip();

// 颜色
String color = random.color();
String rgb = random.rgb();

// 辅助方法
List<String> items = Arrays.asList("a", "b", "c");
String picked = random.pick(items);        // 随机选择一个
List<String> shuffled = random.shuffle(items); // 打乱列表
```

## 完整示例

### 用户数据生成

```java
import com.mock4k.Mock;
import java.util.*;

public class UserDataExample {
    public static void main(String[] args) {
        // 定义用户模板
        Map<String, Object> userTemplate = new HashMap<>();
        userTemplate.put("id|+1", 1);
        userTemplate.put("name", "@name");
        userTemplate.put("email", "@email");
        userTemplate.put("age|18-65", 0);
        userTemplate.put("isActive", "@boolean");
        
        // 地址信息
        Map<String, Object> address = new HashMap<>();
        address.put("street", "@sentence");
        address.put("city", "@word");
        address.put("zipCode", "@integer(10000,99999)");
        userTemplate.put("address", address);
        
        // 爱好列表
        List<String> hobbies = Arrays.asList(
            "reading", "swimming", "coding", "gaming", "music"
        );
        userTemplate.put("hobbies|1-3", hobbies);
        
        // 生成用户列表
        Map<String, Object> template = new HashMap<>();
        template.put("users|5", userTemplate);
        
        Object result = Mock.mock(template);
        System.out.println(result);
    }
}
```

### API响应模拟

```java
import com.mock4k.Mock;
import java.util.*;

public class ApiResponseExample {
    public static void main(String[] args) {
        // API响应模板
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("timestamp", "@now");
        
        // 数据部分
        Map<String, Object> data = new HashMap<>();
        data.put("total|100-1000", 0);
        data.put("page|1-10", 1);
        data.put("pageSize", 20);
        
        // 商品列表
        Map<String, Object> product = new HashMap<>();
        product.put("id|+1", 1);
        product.put("name", "@title");
        product.put("price|10-999.99", 0.0);
        product.put("description", "@paragraph");
        product.put("image", "@image(300x200)");
        product.put("inStock", "@boolean");
        
        data.put("items|10-20", product);
        response.put("data", data);
        
        Object result = Mock.mock(response);
        System.out.println(result);
    }
}
```

## 注意事项

1. **类型转换**: 由于Java的类型系统，你可能需要进行适当的类型转换：
   ```java
   @SuppressWarnings("unchecked")
   Map<String, Object> result = (Map<String, Object>) Mock.mock(template);
   ```

2. **集合类型**: 使用Java的集合类型（ArrayList, HashMap等）来定义模板。

3. **空值处理**: Mock4K会返回Object类型，使用前请检查null值。

4. **线程安全**: MockRandom是线程安全的，可以在多线程环境中使用。

## 与Kotlin版本的差异

- Java版本需要显式的类型转换
- 使用Java集合类型而不是Kotlin集合
- 方法调用语法略有不同（Java风格）
- 需要处理检查异常（如果有的话）

## 测试

可以使用JUnit进行测试：

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MockTest {
    @Test
    public void testBasicMock() {
        String result = (String) Mock.mock("Hello");
        assertEquals("Hello", result);
    }
    
    @Test
    public void testRandomData() {
        String email = (String) Mock.mock("@email");
        assertTrue(email.contains("@"));
    }
}
```

更多示例请参考 `src/main/java/com/mock4k/example/JavaExample.java` 和 `src/test/java/com/mock4k/MockTest.java`。