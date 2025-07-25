# Mock4K

[![Maven Central](https://img.shields.io/maven-central/v/io.github.spcookie/mock4k.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.spcookie%22%20AND%20a:%22mock4k%22)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8+-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Java](https://img.shields.io/badge/java-17-orange.svg?logo=java)](https://www.oracle.com/java/)
[![GitHub release](https://img.shields.io/github/release/spcookie/mock4k.svg)](https://github.com/spcookie/mock4k/releases)
[![GitHub stars](https://img.shields.io/github/stars/spcookie/mock4k.svg?style=social&label=Star)](https://github.com/spcookie/mock4k)
[![GitHub forks](https://img.shields.io/github/forks/spcookie/mock4k.svg?style=social&label=Fork)](https://github.com/spcookie/mock4k/fork)
[![GitHub issues](https://img.shields.io/github/issues/spcookie/mock4k.svg)](https://github.com/spcookie/mock4k/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/spcookie/mock4k.svg)](https://github.com/spcookie/mock4k/pulls)
[![Codecov](https://img.shields.io/codecov/c/github/spcookie/mock4k.svg)](https://codecov.io/gh/spcookie/mock4k)
[![Documentation](https://img.shields.io/badge/docs-latest-brightgreen.svg)](https://spcookie.github.io/mock4k/)

一个强大的 Kotlin 和 Java 模拟数据生成库。

## 📋 目录

- [✨ 特性](#-特性)
- [📦 安装](#-安装)
- [🚀 快速开始](#-快速开始)
- [📚 可用规则](#-可用规则)
- [🛠️ 开发](#-开发)
- [🤝 贡献](#-贡献)
- [🐛 问题反馈](#-问题反馈)
- [📄 许可证](#-许可证)
- [🙏 致谢](#-致谢)

## ✨ 特性

- 🎯 **易于使用**: 简单的 API 用于生成模拟数据
- 🔧 **灵活规则**: 支持各种数据类型和自定义规则
- 🌐 **多语言**: 同时支持 Kotlin 和 Java
- 🌍 **国际化支持**: 支持 44 种语言环境，覆盖全球主要语言和地区，提供本地化的姓名、地址、公司、电话号码等数据生成
- 📊 **丰富数据类型**: 数字、字符串、布尔值、数组和复杂对象
- 🎲 **随机生成**: 内置随机数据生成器
- 📝 **模板支持**: 基于占位符的模板系统
- 🔌 **自定义占位符**: 支持扩展和自定义占位符功能
- 🔍 **正则表达式**: 支持基于正则表达式模式的字符串生成
- 🏗️ **Bean Mock**: 自动生成复杂对象，支持递归深度限制和内省功能
- 🔄 **容器类型**: 增强的数组、列表、集合等容器类型处理
- ⚡ **性能优化**: 优化的代码结构和改进的互操作性

## 📦 安装

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.spcookie:mock4k:1.2.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.spcookie:mock4k:1.2.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.spcookie</groupId>
    <artifactId>mock4k</artifactId>
    <version>1.2.0</version>
</dependency>
```

### GitHub Packages

您也可以从 GitHub Packages 安装 Mock4K:

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/spcookie/mock4k")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.spcookie:mock4k:1.2.0")
}
```

#### Gradle (Groovy DSL)

```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = "https://maven.pkg.github.com/spcookie/mock4k"
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation 'io.github.spcookie:mock4k:1.2.0'
}
```

#### Maven

Add the repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/spcookie/mock4k</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.spcookie</groupId>
    <artifactId>mock4k</artifactId>
    <version>1.2.0</version>
</dependency>
```

And configure authentication in your `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>your_github_username</username>
            <password>your_github_personal_access_token</password>
        </server>
    </servers>
</settings>
```

> **注意**: 要使用 GitHub Packages，您需要使用 GitHub 个人访问令牌进行身份验证。您可以在 GitHub 账户设置中的 "Developer
> settings" > "Personal access tokens" 下创建一个具有 `read:packages` 权限的令牌。

## 🚀 快速开始

### Kotlin 示例
```kotlin
import io.github.spcookie.Mock
import io.github.spcookie.MockRandom

fun main() {
    // 基础模拟
    val name = mock("@name")
    println("姓名: $name")

    // 数字规则
    val number = mock("@integer(1,100)")
    println("数字: $number")

    // 字符串规则
    val email = mock("@email")
    println("邮箱: $email")

    // 复杂模板
    val user = mock("""
        {
            "id": "@integer(1,1000)",
            "name": "@name",
            "email": "@email",
            "age": "@integer(18,65)",
            "active": "@boolean"
        }
    """)
    println("用户: $user")

    // 直接使用随机生成
    val randomInt = MockRandom.integer(1, 100)
    val randomString = MockRandom.string(5, 15)
    println("随机整数: $randomInt, 随机字符串: $randomString")
    
    // 设置语言环境
    MockRandom.setLocale(java.util.Locale.ENGLISH)
    val englishName = MockRandom.name()
    println("英文姓名: $englishName")
    
    // 正则表达式生成
    val regexString = mock("/[a-z]{3}\\d{2}/")
    println("正则生成: $regexString")
    
    // Bean Mock 示例（需要定义数据类）
    data class User(val id: String, val name: String, val email: String)
    val userBean = mock<User>()
    println("Bean对象: $userBean")
    
    // 自定义占位符扩展示例
    Mock.Random.entend("people") { args ->
        "people-${args[0]}"
    }
    val customTemplate = mock("@people(5)")
    println("自定义模板: $customTemplate")
}
```

### Java 示例
```java
import io.github.spcookie.Mock;
import io.github.spcookie.MockRandom;

public class JavaExample {
    public static void main(String[] args) {
        // 基础模拟
        String name = mock("@name");
        System.out.println("姓名: " + name);

        // 数字规则
        String number = mock("@integer(1,100)");
        System.out.println("数字: " + number);

        // 字符串规则
        String email = mock("@email");
        System.out.println("邮箱: " + email);

        // 复杂模板
        String user = mock(
            "{" +
            "\"id\": \"@integer(1,1000)\"," +
            "\"name\": \"@name\"," +
            "\"email\": \"@email\"," +
            "\"age\": \"@integer(18,65)\"," +
            "\"active\": \"@boolean\"" +
            "}"
        );
        System.out.println("用户: " + user);

        // 直接使用随机生成
        int randomInt = MockRandom.integer(1, 100);
        String randomString = MockRandom.string(5, 15);
        System.out.println("随机整数: " + randomInt + ", 随机字符串: " + randomString);
        
        // 设置语言环境
        MockRandom.setLocale(java.util.Locale.ENGLISH);
        String englishName = MockRandom.name();
        System.out.println("英文姓名: " + englishName);
        
        // 正则表达式生成
        String regexString = mock("/[a-z]{3}\\d{2}/");
        System.out.println("正则生成: " + regexString);
        
        // UUID 生成
        String uuid = mock("@uuid");
        System.out.println("UUID: " + uuid);
        
        // Bean Mock 示例（需要定义数据类）
        // User userBean = mock(User.class);
        // System.out.println("Bean对象: " + userBean);
        
        // 自定义占位符扩展示例
        Mock.Random.entend("people", args -> "people-%s".formatted(args[0]));
        String customTemplate = mock("@people(5)");
        System.out.println("自定义模板: " + customTemplate);
    }
}
```

## 📚 可用规则

### 基础类型

| 规则                      | 描述                 | 示例                                                 |
|-------------------------|--------------------|----------------------------------------------------|
| `@boolean`              | 随机布尔值              | `true` 或 `false`                                   |
| `@boolean(probability)` | 指定概率的随机布尔值         | `@boolean(0.7)` → `true`                           |
| `@integer(min, max)`    | 在最小值和最大值之间的随机整数    | `@integer(1,100)` → `42`                           |
| `@natural(min, max)`    | 自然数（正整数）           | `@natural(1,100)` → `42`                           |
| `@float(min, max)`      | 指定范围的随机浮点数         | `@float(1,10)` → `3.14`                            |
| `@character`            | 随机字符               | `@character` → `"a"`                               |
| `@string(length)`       | 指定长度的随机字符串         | `@string(5)` → `"abcde"`                           |
| `@string(min, max)`     | 长度在最小值和最大值之间的随机字符串 | `@string(5,10)` → `"abcdef"`                       |
| `@guid`                 | 随机GUID             | `@guid` → `"550e8400-e29b-41d4-a716-446655440000"` |
| `@uuid`                 | 随机UUID             | `@uuid` → `"550e8400-e29b-41d4-a716-446655440000"` |
| `@id`                   | 随机ID               | `@id` → `"abc123def456"`                           |

### 常用数据

| 规则            | 描述     | 示例输出                    |
|---------------|--------|-------------------------|
| `@name`       | 随机人名   | `"John Doe"`            |
| `@first`      | 随机名字   | `"John"`                |
| `@last`       | 随机姓氏   | `"Doe"`                 |
| `@email`      | 随机邮箱地址 | `"user@example.com"`    |
| `@phone`      | 随机电话号码 | `"+1-555-123-4567"`     |
| `@areacode`   | 随机区号   | `"201"`                 |
| `@city`       | 随机城市名称 | `"New York"`            |
| `@province`   | 随机省份名称 | `"California"`          |
| `@company`    | 随机公司名称 | `"Tech Corp"`           |
| `@profession` | 随机职业   | `"Engineer"`            |
| `@streetname` | 随机街道名称 | `"Main Street"`         |
| `@url`        | 随机URL  | `"https://example.com"` |
| `@domain`     | 随机域名   | `"example.com"`         |
| `@ip`         | 随机IP地址 | `"192.168.1.1"`         |
| `@tld`        | 随机顶级域名 | `"com"`                 |
| `@bankcard`   | 随机银行卡号 | `"6225123456789012345"` |

### 正则表达式生成

| 规则          | 描述             | 示例                                     |
|-------------|----------------|----------------------------------------|
| `/pattern/` | 根据正则表达式模式生成字符串 | `/[a-z]{3}\d{2}/` → `"abc12"`          |
| `/pattern/` | 支持各种正则表达式模式    | `/\d{4}-\d{2}-\d{2}/` → `"2023-12-25"` |

### 文本生成

| 规则                     | 描述          | 示例输出                        |
|------------------------|-------------|-----------------------------|
| `@word`                | 随机单词        | `"hello"`                   |
| `@word(min, max)`      | 指定长度范围的随机单词 | `@word(3,8)` → `"world"`    |
| `@sentence`            | 随机句子        | `"Hello world."`            |
| `@sentence(min, max)`  | 指定单词数量的随机句子 | `@sentence(5,10)`           |
| `@paragraph`           | 随机段落        | `"Hello world. This is..."` |
| `@paragraph(min, max)` | 指定句子数量的随机段落 | `@paragraph(3,5)`           |
| `@title`               | 随机标题        | `"Hello World"`             |
| `@title(min, max)`     | 指定单词数量的随机标题 | `@title(2,4)`               |

### 日期时间

| 规则                  | 描述          | 示例输出                            |
|---------------------|-------------|---------------------------------|
| `@date`             | 随机日期        | `"2023-12-25"`                  |
| `@date(format)`     | 指定格式的随机日期   | `@date("yyyy/MM/dd")`           |
| `@time`             | 随机时间        | `"14:30:00"`                    |
| `@time(format)`     | 指定格式的随机时间   | `@time("HH:mm")`                |
| `@datetime`         | 随机日期时间      | `"2023-12-25 14:30:00"`         |
| `@datetime(format)` | 指定格式的随机日期时间 | `@datetime("yyyy-MM-dd HH:mm")` |
| `@now`              | 当前时间        | `"2023-12-25 14:30:00"`         |
| `@now(format)`      | 指定格式的当前时间   | `@now("yyyy-MM-dd")`            |

### 颜色和图片

| 规则                 | 描述             | 示例输出                                    |
|--------------------|----------------|-----------------------------------------|
| `@color`           | 随机颜色值          | `"#FF5733"`                             |
| `@image`           | 随机图片URL        | `"https://via.placeholder.com/200x200"` |
| `@image(size)`     | 指定尺寸的随机图片URL   | `@image("300x200")`                     |
| `@dataimage`       | 随机数据图片URL      | `"data:image/svg+xml;base64,..."`       |
| `@dataimage(size)` | 指定尺寸的随机数据图片URL | `@dataimage("100x100")`                 |

### 集合

| 规则                           | 描述                  | 示例                                  |
|------------------------------|---------------------|-------------------------------------|
| `@range(start, stop)`        | 生成数字范围数组            | `@range(1,10)` → `[1,2,3...10]`     |
| `@range(start, stop, step)`  | 生成指定步长的数字范围数组       | `@range(0,10,2)` → `[0,2,4,6,8,10]` |

### 国际化电话号码

| 规则                           | 描述          | 示例输出                           |
|------------------------------|-------------|--------------------------------|
| `@phonenumber`               | 随机电话号码      | `"555-123-4567"`               |
| `@phonenumber(m)`            | 移动电话号码      | `"134-5678-9012"`              |
| `@phonenumber(l)`            | 固定电话号码      | `"010-1234-5678"`              |
| `@phonenumber(tf)`           | 免费电话号码      | `"800-123-4567"`               |
| `@phonenumber(p)`            | 付费电话号码      | `"900-123-4567"`               |

### 规则修饰符

规则修饰符用于增强基本规则的功能，通过在规则后添加 `|` 符号和相应的修饰符来使用。

数据模板中的每个属性由 3 部分构成：属性名、生成规则、属性值，其中属性名和生成规则之间用竖线 `|` 分隔。

#### 基本语法格式

| 格式                                 | 描述               | 适用类型                                   |
|------------------------------------|------------------|----------------------------------------|
| `'name\|min-max': value`           | 重复次数范围/数值范围/权重比例 | String, Array, Object, Number, Boolean |
| `'name\|count': value`             | 重复指定次数/固定值/权重    | String, Array, Object, Boolean         |
| `'name\|min-max.dmin-dmax': value` | 浮点数范围（可变小数位）     | Number                                 |
| `'name\|min-max.dcount': value`    | 浮点数范围（固定小数位）     | Number                                 |
| `'name\|count.dmin-dmax': value`   | 浮点数（可变小数位）       | Number                                 |
| `'name\|count.dcount': value`      | 浮点数（固定小数位）       | Number                                 |
| `'name\|+step': value`             | 递增规则/顺序选择        | Number, Array                          |

#### 不同数据类型的修饰符行为

##### String 字符串

| 修饰符       | 描述                           | 示例                     | 结果                               |
|-----------|------------------------------|------------------------|----------------------------------|
| `min-max` | 通过重复字符串生成新字符串，重复次数在min-max之间 | `"text\|2-4": "Hello"` | "HelloHello" 或 "HelloHelloHello" |
| `count`   | 通过重复字符串生成新字符串，重复次数为count     | `"text\|3": "Hi"`      | "HiHiHi"                         |

##### Number 数字

| 修饰符                 | 描述                                  | 示例                        | 结果               |
|---------------------|-------------------------------------|---------------------------|------------------|
| `+step`             | 属性值自动加step，生成递增数字                   | `"id\|+1": 100`           | 100, 101, 102... |
| `min-max`           | 生成大于等于min、小于等于max的整数                | `"age\|18-65": 0`         | 18-65之间的随机整数     |
| `min-max.dmin-dmax` | 生成浮点数，整数部分在min-max间，小数位数在dmin-dmax间 | `"price\|10-20.2-4": 0.0` | 12.345 或 18.67   |
| `min-max.dcount`    | 生成浮点数，整数部分在min-max间，小数位数固定为dcount   | `"price\|10-20.2": 0.0`   | 15.23 或 18.76    |

##### Boolean 布尔值

| 修饰符       | 描述                               | 示例                  | 结果          |
|-----------|----------------------------------|---------------------|-------------|
| `min-max` | 随机生成布尔值，值为value的概率是min/(min+max) | `"flag\|1-3": true` | true的概率为1/4 |
| `count`   | 随机生成布尔值，当count=1时固定返回value值      | `"flag\|1": true`   | 固定返回true    |

##### Array 数组

| 修饰符       | 描述                      | 示例                                 | 结果                                            |
|-----------|-------------------------|------------------------------------|-----------------------------------------------|
| `1`       | 从数组中随机选取1个元素            | `"color\|1": ["red","blue"]`       | "red" 或 "blue"                                |
| `+1`      | 从数组中顺序选取1个元素            | `"status\|+1": ["pending","done"]` | 按顺序返回pending,done,pending...                  |
| `min-max` | 重复数组生成新数组，重复次数在min-max间 | `"items\|2-3": ["a","b"]`          | ["a","b","a","b"] 或 ["a","b","a","b","a","b"] |
| `count`   | 重复数组生成新数组，重复次数为count    | `"tags\|2": ["java","kotlin"]`     | ["java","kotlin","java","kotlin"]             |

##### Object 对象

| 修饰符       | 描述                 | 示例                                                          | 结果                                             |
|-----------|--------------------|-------------------------------------------------------------|------------------------------------------------|
| `min-max` | 从对象中随机选取min-max个属性 | `"config\|1-2": {"debug":true,"timeout":5000}`              | {"debug":true} 或 {"debug":true,"timeout":5000} |
| `count`   | 从对象中随机选取count个属性   | `"user\|2": {"name":"@name","age":"@age","email":"@email"}` | 包含2个随机属性的对象                                    |

#### 使用示例

```kotlin
// 字符串重复
val template1 = mapOf("text|3" to "Hello")
val result1 = mock(template1) // {"text": "HelloHelloHello"}

// 数字递增
val template2 = mapOf(
    "list|3" to listOf(
        mapOf("id|+1" to 1000)
    )
)
val result2 = mock(template2) // 生成id为1000, 1001, 1002的列表

// 浮点数范围
val template3 = mapOf("price|10-20.2" to 0.0)
val result3 = mock(template3) // {"price": 15.23}

// 数组选择
val template4 = mapOf("color|1" to listOf("red", "green", "blue"))
val result4 = mock(template4) // {"color": "red"} 或其他颜色

// 复杂对象示例
val complexTemplate = mapOf(
    "id|+1" to 1000,
    "name" to "@name",
    "tags|1-3" to listOf("@word"),
    "score|1-100.1-2" to 1.0
)
val user = mock(complexTemplate)
```

### 🌍 国际化支持

Mock4K 提供强大的国际化支持，能够根据不同的语言环境生成本地化的模拟数据。通过 `MockRandom.setLocale()` 和 `LocaleManager` 可以轻松切换语言环境。

#### 支持的语言环境

Mock4K 支持 **44 种语言环境**，覆盖全球主要语言和地区：

| 语言环境   | 语言代码 | Locale 代码                     |
|--------|------|-------------------------------|
| 英语     | en   | `Locale.ENGLISH`              |
| 中文     | zh   | `Locale.CHINESE`              |
| 日语     | ja   | `Locale.JAPANESE`             |
| 韩语     | ko   | `Locale.KOREAN`               |
| 法语     | fr   | `Locale.FRENCH`               |
| 德语     | de   | `Locale.GERMAN`               |
| 意大利语   | it   | `Locale.ITALIAN`              |
| 西班牙语   | es   | `Locale.forLanguageTag("es")` |
| 俄语     | ru   | `Locale.forLanguageTag("ru")` |
| 阿拉伯语   | ar   | `Locale.forLanguageTag("ar")` |
| 葡萄牙语   | pt   | `Locale.forLanguageTag("pt")` |
| 荷兰语    | nl   | `Locale.forLanguageTag("nl")` |
| 波兰语    | pl   | `Locale.forLanguageTag("pl")` |
| 土耳其语   | tr   | `Locale.forLanguageTag("tr")` |
| 瑞典语    | sv   | `Locale.forLanguageTag("sv")` |
| 挪威语    | no   | `Locale.forLanguageTag("no")` |
| 丹麦语    | da   | `Locale.forLanguageTag("da")` |
| 芬兰语    | fi   | `Locale.forLanguageTag("fi")` |
| 匈牙利语   | hu   | `Locale.forLanguageTag("hu")` |
| 捷克语    | cs   | `Locale.forLanguageTag("cs")` |
| 斯洛伐克语  | sk   | `Locale.forLanguageTag("sk")` |
| 罗马尼亚语  | ro   | `Locale.forLanguageTag("ro")` |
| 保加利亚语  | bg   | `Locale.forLanguageTag("bg")` |
| 克罗地亚语  | hr   | `Locale.forLanguageTag("hr")` |
| 塞尔维亚语  | sr   | `Locale.forLanguageTag("sr")` |
| 斯洛文尼亚语 | sl   | `Locale.forLanguageTag("sl")` |
| 波斯尼亚语  | bs   | `Locale.forLanguageTag("bs")` |
| 黑山语    | me   | `Locale.forLanguageTag("me")` |
| 马其顿语   | mk   | `Locale.forLanguageTag("mk")` |
| 阿尔巴尼亚语 | sq   | `Locale.forLanguageTag("sq")` |
| 希腊语    | el   | `Locale.forLanguageTag("el")` |
| 立陶宛语   | lt   | `Locale.forLanguageTag("lt")` |
| 拉脱维亚语  | lv   | `Locale.forLanguageTag("lv")` |
| 爱沙尼亚语  | et   | `Locale.forLanguageTag("et")` |
| 冰岛语    | is   | `Locale.forLanguageTag("is")` |
| 马耳他语   | mt   | `Locale.forLanguageTag("mt")` |
| 威尔士语   | cy   | `Locale.forLanguageTag("cy")` |
| 爱尔兰语   | ga   | `Locale.forLanguageTag("ga")` |
| 希伯来语   | he   | `Locale.forLanguageTag("he")` |
| 泰语     | th   | `Locale.forLanguageTag("th")` |
| 越南语    | vi   | `Locale.forLanguageTag("vi")` |
| 印尼语    | id   | `Locale.forLanguageTag("id")` |
| 马来语    | ms   | `Locale.forLanguageTag("ms")` |
| 乌克兰语   | uk   | `Locale.forLanguageTag("uk")` |

> **注意**: 所有语言环境都支持相同的数据类型，包括姓名、城市、公司、职业、街道名称、电话号码、邮箱域名、银行信息等。每种语言的数据都经过本地化处理，确保生成的内容符合当地的文化和语言习惯。

#### 国际化功能特性

- **姓名生成**: 根据不同语言环境生成符合当地命名习惯的姓名
- **地址信息**: 城市、省份、街道名称等地理位置信息
- **公司名称**: 本地化的公司名称和企业信息
- **职业名称**: 符合当地文化的职业描述
- **电话号码**: 根据地区生成符合当地格式和规则的电话号码
- **文本内容**: 单词、句子、段落等文本内容的本地化
- **语言环境检测**: 自动检测和验证支持的语言环境

#### 基本使用示例

```kotlin
import io.github.spcookie.MockRandom
import io.github.spcookie.LocaleManager
import java.util.Locale

// 设置为英文环境
MockRandom.setLocale(Locale.ENGLISH)
val englishName = MockRandom.name() // "John Smith"
val englishCity = MockRandom.city() // "New York"
val englishCompany = MockRandom.company() // "Tech Corp"

// 设置为中文环境
MockRandom.setLocale(Locale.CHINESE)
val chineseName = MockRandom.name() // "张三"
val chineseCity = MockRandom.city() // "北京"
val chineseCompany = MockRandom.company() // "科技有限公司"

// 设置为日文环境
MockRandom.setLocale(Locale.JAPANESE)
val japaneseName = MockRandom.name() // "田中太郎"
val japaneseCity = MockRandom.city() // "東京"

// 检查语言环境支持
val isSupported = LocaleManager.isLocaleSupported(Locale.FRENCH)
val supportedLocales = LocaleManager.getSupportedLocales()
```

#### 电话号码国际化

不同语言环境下的电话号码格式会自动适配当地的号码规则：

```kotlin
// 中文环境 - 中国电话号码格式
MockRandom.setLocale(Locale.CHINESE)
val chinesePhone = MockRandom.phoneNumber() // "138-1234-5678"
val chineseMobile = MockRandom.phoneNumber(MockRandom.PhoneType.MOBILE) // "134-5678-9012"
val chineseLandline = MockRandom.phoneNumber(MockRandom.PhoneType.LANDLINE) // "010-1234-5678"

// 英文环境 - 美国电话号码格式
MockRandom.setLocale(Locale.ENGLISH)
val usPhone = MockRandom.phoneNumber() // "555-123-4567"
val usMobile = MockRandom.phoneNumber(MockRandom.PhoneType.MOBILE) // "555-987-6543"
val usTollFree = MockRandom.phoneNumber(MockRandom.PhoneType.TOLL_FREE) // "800-123-4567"
```

#### 模板中的国际化

在模板中使用占位符时，会自动根据当前语言环境生成对应的本地化数据：

```kotlin
// 设置语言环境
MockRandom.setLocale(Locale.CHINESE)

val template = mapOf(
    "用户信息" to mapOf(
        "姓名" to "@NAME",
        "城市" to "@CITY",
        "公司" to "@COMPANY",
        "职业" to "@PROFESSION",
        "电话" to "@PHONENUMBER(PT.M)",
        "地址" to "@PROVINCE @CITY @STREETNAME"
    )
)

val result = mock(template)
// 生成的数据将全部是中文本地化内容
```

### 🏗️ Bean Mock 功能

Bean Mock 支持自动生成复杂的 Java Bean 和 Kotlin 数据类对象。

#### 基本使用

```kotlin
// 定义数据类
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val active: Boolean
)

// 自动生成 User 对象
val user = mock<User>()
println(user)
// 输出: User(id=123456, name="张三", email="user@example.com", age=25, active=true)
```

#### 使用注解控制生成

```kotlin
import io.github.spcookie.Mock

data class Product(
    @Mock("@integer(1,1000)")
    val id: Long,
    
    @Mock("@string(5,20)")
    val name: String,
    
    @Mock("@float(10,1000)")
    val price: Double,
    
    @Mock("@boolean(0.8)")
    val inStock: Boolean
)

val product = mock<Product>()
```

#### 复杂嵌套对象

```kotlin
data class Address(
    val street: String,
    val city: String,
    val zipCode: String
)

data class Company(
    val name: String,
    val address: Address
)

data class Employee(
    val id: Long,
    val name: String,
    val company: Company,
    val skills: List<String>
)

// 自动处理嵌套对象和集合
val employee = mock<Employee>()
```

#### 配置选项

```kotlin
// 使用配置控制生成行为
val config = BeanMockConfig(
    maxDepth = 5,  // 最大递归深度
    fillStrategy = Mock.FillStrategy.ALL  // 填充策略
)

val user = mock<User>(config)
```

#### Java 中使用

```java
// Java 中的使用方式
User user = Mocks.mock(User.class);
System.out.println(user);

// 使用配置
BeanMockConfig config = new BeanMockConfig(5, Mock.FillStrategy.ALL);
User configuredUser = Mocks.mock(User.class, config);
```

#### 支持的类型

Bean Mock 支持以下类型的自动生成：

- **基本类型**: `int`, `long`, `double`, `boolean` 等
- **包装类型**: `Integer`, `Long`, `Double`, `Boolean` 等
- **字符串类型**: `String`
- **日期时间**: `Date`, `LocalDate`, `LocalDateTime` 等
- **集合类型**: `List`, `Set`, `Map` 等
- **数组类型**: 各种类型的数组
- **枚举类型**: 自动从枚举值中随机选择
- **嵌套对象**: 递归生成复杂对象结构

#### 递归深度控制

为避免无限递归，Bean Mock 提供了递归深度限制：

```kotlin
// 设置最大递归深度为 3
val config = BeanMockConfig(maxDepth = 3)
val deepObject = mock<ComplexNestedObject>(config)
```

#### 容器填充策略

支持不同的属性填充策略：

```kotlin
// 重复填充
Mock.FillStrategy.REPECT

// 随机填充
Mock.FillStrategy.RANDOM
```


#### LocaleManager 高级功能

```kotlin
// 获取当前语言环境
val currentLocale = LocaleManager.getCurrentLocale()

// 获取所有支持的语言环境
val supportedLocales = LocaleManager.getSupportedLocales()
println("支持的语言环境: $supportedLocales")

// 检查特定语言环境是否支持
val isFrenchSupported = LocaleManager.isLocaleSupported(Locale.FRENCH)
val isChineseSupported = LocaleManager.isLocaleSupported(Locale.CHINESE)

// 获取特定类型的本地化数据
val chineseWords = LocaleManager.getDataList("words")
val chineseCities = LocaleManager.getDataList("cities")
```

## 🛠️ 开发

### 项目结构

```
src/
├── main/
│   ├── kotlin/io/github/spcookie/
│   │   ├── ExecutionContext.kt    # 执行上下文管理
│   │   ├── LocaleManager.kt       # 国际化管理器
│   │   ├── Mock.kt                # 主要Mock对象和注解
│   │   ├── MockEngine.kt          # Mock引擎实现
│   │   ├── MockRandom.kt          # 随机数据生成器
│   │   ├── GlobalMocks.kt               # Mock对象
│   │   ├── ParsedRule.kt          # 规则解析数据结构
│   │   ├── PlaceholderResolver.kt # 占位符解析逻辑
│   │   ├── RegexResolver.kt       # 正则表达式解析器
│   │   ├── Rule.kt                # 规则定义
│   │   ├── RuleExecutor.kt        # 规则执行引擎
│   │   ├── RuleParser.kt          # 规则解析逻辑
│   │   ├── BeanMockBridge.kt      # Bean Mock 桥接实现
│   │   ├── BeanMockConfig.kt      # Bean Mock 配置
│   │   ├── BeanIntrospect.kt      # Bean 内省功能
│   │   ├── ContainerAdapter.kt    # 容器类型适配器
│   │   └── package.kt             # 包信息文档
```

核心组件说明：

- **Mock.kt**: 提供简单易用的API和注解定义
- **MockEngine.kt**: 模拟引擎的核心实现，负责解析和执行模板
- **MockRandom.kt**: 随机数据生成器，提供各种类型的随机数据生成方法
- **GlobalMock.kt**: 主要的模拟数据生成入口
- **RuleParser.kt**: 规则解析器，负责解析占位符规则
- **RuleExecutor.kt**: 规则执行器，负责执行解析后的规则
- **PlaceholderResolver.kt**: 占位符解析器，处理模板中的占位符，支持属性引用
- **RegexResolver.kt**: 正则表达式解析器，专门处理正则表达式模式的解析和生成
- **ExecutionContext.kt**: 执行上下文管理器，提供线程安全的执行环境
- **LocaleManager.kt**: 国际化管理器，支持多语言环境和属性文件加载优化
- **ParsedRule.kt**: 规则解析结果的数据结构
- **Rule.kt**: 规则定义和相关枚举，支持正则表达式和自定义占位符
- **package.kt**: 包信息文档，包含包级别的文档和元数据
- **BeanMockBridge.kt**: Bean Mock 桥接实现，提供强大的对象创建能力
- **BeanMockConfig.kt**: Bean Mock 配置管理，包含填充策略和递归深度限制
- **BeanIntrospect.kt**: Bean 内省功能，自动分析和处理复杂对象结构
- **ContainerAdapter.kt**: 容器类型适配器，优化数组、列表、集合等容器类型的处理

### 开发环境设置

#### 前置要求

- **Java 17+**: 项目使用 Java 17 作为目标版本
- **Kotlin 1.9.10+**: 使用最新的 Kotlin 版本
- **Gradle 8.0+**: 构建工具

#### 克隆项目

```bash
git clone https://github.com/spcookie/mock4k.git
cd mock4k
```

#### 构建项目

```bash
# 清理并构建
./gradlew clean build

# 仅编译
./gradlew compileKotlin

# 生成文档
./gradlew dokkaHtml
```

## 🤝 贡献

欢迎贡献！请随时提交 Pull Request。

1. Fork 项目
2. 创建您的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开 Pull Request

请确保适当更新测试并遵循现有的代码风格。

## 🐛 问题反馈

如果您在使用 Mock4K 过程中遇到任何问题，或者有功能建议，欢迎通过以下方式反馈：

### 提交 Issue

1. **访问项目仓库**: [https://github.com/spcookie/mock4k](https://github.com/spcookie/mock4k)
2. **点击 "Issues" 标签页**
3. **点击 "New issue" 按钮**
4. **选择合适的 issue 模板**：
    - 🐛 **Bug Report**: 报告软件缺陷
    - 🚀 **Feature Request**: 请求新功能
    - 📖 **Documentation**: 文档改进建议
    - ❓ **Question**: 使用问题咨询

### Issue 提交指南

为了帮助我们更好地理解和解决问题，请在提交 issue 时包含以下信息：

#### Bug Report 应包含：

- **问题描述**: 清晰描述遇到的问题
- **复现步骤**: 详细的步骤说明
- **期望行为**: 描述您期望的正确行为
- **实际行为**: 描述实际发生的情况
- **环境信息**:
    - Mock4K 版本
    - Kotlin/Java 版本
    - 操作系统
    - 构建工具版本 (Gradle/Maven)
- **代码示例**: 提供最小可复现的代码示例
- **错误日志**: 如果有相关的错误信息或堆栈跟踪

#### Feature Request 应包含：

- **功能描述**: 详细描述建议的新功能
- **使用场景**: 说明该功能的应用场景
- **预期收益**: 解释该功能将如何改善用户体验
- **实现建议**: 如果有实现思路，欢迎分享

## 📄 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

## 🙏 致谢

- 使用 [Kotlin](https://kotlinlang.org/) 构建 - JVM 的现代编程语言
- 文档由 [Dokka](https://github.com/Kotlin/dokka) 生成 - Kotlin 文档引擎

---

<div align="center">
  <sub>Built with ❤️ by the Spcookie.</sub>
</div>