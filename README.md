# Mock4K

一个受 Mock.js 启发的 Kotlin 模拟数据生成库。

## 特性

- 数据模板定义 (DTD) 支持
- 数据占位符定义 (DPD) 支持
- 随机数据生成
- 灵活的基于规则的模拟生成
- Java 兼容性支持

## 使用方法

### Kotlin

```kotlin
import com.mock4k.Mock

// 基本使用
val data = Mock.mock(mapOf(
    "list|1-10" to listOf(mapOf(
        "id|+1" to 1,
        "name" to "@name",
        "email" to "@email"
    ))
))

println(data)
```

### Java

```java
import com.mock4k.Mock;
import java.util.*;

// 基本使用
Map<String, Object> template = new HashMap<>();
template.put("name", "@name");
template.put("age|18-65", 0);

Object data = Mock.mock(template);
System.out.println(data);
```

## 语法规范

### 数据模板定义 (DTD)

每个属性由 3 部分组成：属性名、生成规则和属性值：

```
'name|rule': value
```

### 生成规则

1. **字符串**: `'name|min-max': string` 或 `'name|count': string`
2. **数字**: `'name|+1': number`, `'name|min-max': number`, `'name|min-max.dmin-dmax': number`
3. **布尔值**: `'name|1': boolean`, `'name|min-max': value`
4. **对象**: `'name|count': object`, `'name|min-max': object`
5. **数组**: `'name|1': array`, `'name|+1': array`, `'name|min-max': array`
6. **函数**: `'name': function`
7. **正则**: `'name': regexp`

### 数据占位符定义 (DPD)

占位符以 `@` 开头，引用 `Mock.Random` 中的方法：

```
@placeholder
@placeholder(param1, param2)
```

### 常用占位符

#### 基本类型
- `@boolean` - 随机布尔值
- `@integer` - 随机整数
- `@integer(min, max)` - 指定范围的随机整数
- `@float` - 随机浮点数
- `@string` - 随机字符串
- `@string(length)` - 指定长度的随机字符串

#### 日期时间
- `@date` - 随机日期
- `@time` - 随机时间
- `@datetime` - 随机日期时间
- `@now` - 当前时间戳

#### 文本
- `@word` - 随机单词
- `@sentence` - 随机句子
- `@paragraph` - 随机段落
- `@title` - 随机标题

#### 中文文本
- `@cword` - 随机中文词语
- `@csentence` - 随机中文句子
- `@cparagraph` - 随机中文段落
- `@ctitle` - 随机中文标题

#### 姓名
- `@first` - 英文名
- `@last` - 英文姓
- `@name` - 英文全名
- `@cfirst` - 中文名
- `@clast` - 中文姓
- `@cname` - 中文全名

#### 网络
- `@url` - 随机URL
- `@domain` - 随机域名
- `@email` - 随机邮箱
- `@ip` - 随机IP地址

#### 颜色
- `@color` - 随机颜色
- `@hex` - 十六进制颜色
- `@rgb` - RGB颜色
- `@rgba` - RGBA颜色
- `@hsl` - HSL颜色

#### 其他
- `@guid` - 随机GUID
- `@id` - 随机ID
- `@image` - 随机图片URL
- `@image(size)` - 指定尺寸的图片URL

## Java 支持

Mock4K 完全支持 Java，详细使用方法请参考 [Java 适配指南](JAVA_ADAPTER.md)。

## 示例

更多使用示例请查看：
- [Kotlin 示例](src/main/kotlin/com/mock4k/example/Example.kt)
- [Java 示例](src/main/java/com/mock4k/example/JavaExample.java)

## 许可证

MIT