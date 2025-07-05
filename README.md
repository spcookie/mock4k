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
[![Maven Central Downloads](https://img.shields.io/maven-central/dt/io.github.spcookie/mock4k.svg)](https://search.maven.org/artifact/io.github.spcookie/mock4k)
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
- 🌍 **国际化支持**: 内置 i18n 功能，支持多语言环境和地区化数据生成
- 📊 **丰富数据类型**: 数字、字符串、布尔值、数组和复杂对象
- 🎲 **随机生成**: 内置随机数据生成器
- 📝 **模板支持**: 基于占位符的模板系统

## 📦 安装

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.spcookie:mock4k:1.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.spcookie:mock4k:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.spcookie</groupId>
    <artifactId>mock4k</artifactId>
    <version>1.0.0</version>
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
    implementation("io.github.spcookie:mock4k:1.0.0")
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
    implementation 'io.github.spcookie:mock4k:1.0.0'
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
    <version>1.0.0</version>
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
    val name = Mock.mock("@name")
    println("姓名: $name")

    // 数字规则
    val number = Mock.mock("@integer(1,100)")
    println("数字: $number")

    // 字符串规则
    val email = Mock.mock("@email")
    println("邮箱: $email")

    // 数组规则
    val array = Mock.mock("@array(5, @string(3,10))")
    println("数组: $array")

    // 复杂模板
    val user = Mock.mock("""
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
    
    // 国际化电话号码生成
    val mobilePhone = MockRandom.phoneNumber(MockRandom.PhoneType.MOBILE)
    val landlinePhone = MockRandom.phoneNumber(MockRandom.PhoneType.LANDLINE)
    val tollFreePhone = MockRandom.phoneNumber(MockRandom.PhoneType.TOLL_FREE)
    println("移动电话: $mobilePhone")
    println("固定电话: $landlinePhone")
    println("免费电话: $tollFreePhone")
    
    // 设置语言环境
    MockRandom.setLocale(java.util.Locale.ENGLISH)
    val englishName = MockRandom.name()
    println("英文姓名: $englishName")
}
```

### Java 示例
```java
import io.github.spcookie.Mock;
import io.github.spcookie.MockRandom;

public class JavaExample {
    public static void main(String[] args) {
        // 基础模拟
        String name = Mock.mock("@name");
        System.out.println("姓名: " + name);

        // 数字规则
        String number = Mock.mock("@integer(1,100)");
        System.out.println("数字: " + number);

        // 字符串规则
        String email = Mock.mock("@email");
        System.out.println("邮箱: " + email);

        // 数组规则
        String array = Mock.mock("@array(5, @string(3,10))");
        System.out.println("数组: " + array);

        // 复杂模板
        String user = Mock.mock(
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
        
        // 国际化电话号码生成
        String mobilePhone = MockRandom.phoneNumber(MockRandom.PhoneType.MOBILE);
        String landlinePhone = MockRandom.phoneNumber(MockRandom.PhoneType.LANDLINE);
        String tollFreePhone = MockRandom.phoneNumber(MockRandom.PhoneType.TOLL_FREE);
        System.out.println("移动电话: " + mobilePhone);
        System.out.println("固定电话: " + landlinePhone);
        System.out.println("免费电话: " + tollFreePhone);
        
        // 设置语言环境
        MockRandom.setLocale(java.util.Locale.ENGLISH);
        String englishName = MockRandom.name();
        System.out.println("英文姓名: " + englishName);
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

### 数组和集合

| 规则                           | 描述                  | 示例                                  |
|------------------------------|---------------------|-------------------------------------|
| `@array(count, template)`    | 使用指定数量和模板生成数组       | `@array(3, @string(5))`             |
| `@array(min, max, template)` | 生成数量在最小值和最大值之间的随机数组 | `@array(1,5, @integer(1,100))`      |
| `@range(start, stop)`        | 生成数字范围数组            | `@range(1,10)` → `[1,2,3...10]`     |
| `@range(start, stop, step)`  | 生成指定步长的数字范围数组       | `@range(0,10,2)` → `[0,2,4,6,8,10]` |

### 国际化电话号码

| 规则                           | 描述          | 示例输出                           |
|------------------------------|-------------|--------------------------------|
| `@phonenumber`               | 随机电话号码      | `"555-123-4567"`               |
| `@phonenumber(format)`       | 指定格式的随机电话号码 | `@phonenumber("###-###-####")` |
| `@phonenumber(PT.M)`         | 移动电话号码      | `"134-5678-9012"`              |
| `@phonenumber(PT.L)`         | 固定电话号码      | `"010-1234-5678"`              |
| `@phonenumber(PT.TF)`        | 免费电话号码      | `"800-123-4567"`               |
| `@phonenumber(PT.P)`         | 付费电话号码      | `"900-123-4567"`               |
| `@phonenumber(PT.MOBILE)`    | 移动电话号码      | `"134-5678-9012"`              |
| `@phonenumber(PT.LANDLINE)`  | 固定电话号码      | `"010-1234-5678"`              |
| `@phonenumber(PT.TOLL_FREE)` | 免费电话号码      | `"800-123-4567"`               |
| `@phonenumber(PT.PREMIUM)`   | 付费电话号码      | `"900-123-4567"`               |

### 规则修饰符

规则修饰符用于增强基本规则的功能，通过在规则后添加 `|` 符号和相应的修饰符来使用。

数据模板中的每个属性由 3 部分构成：属性名、生成规则、属性值，其中属性名和生成规则之间用竖线 `|` 分隔。

#### 基本语法格式

| 格式     | 描述                         | 适用类型         |
|--------|----------------------------|--------------|
| `'name | min-max': value`           | 重复次数范围       | String, Array, Object |
| `'name | count': value`             | 重复指定次数       | String, Array, Object |
| `'name | min-max.dmin-dmax': value` | 浮点数范围（可变小数位） | Number |
| `'name | min-max.dcount': value`    | 浮点数范围（固定小数位） | Number |
| `'name | count.dmin-dmax': value`   | 浮点数（可变小数位）   | Number |
| `'name | count.dcount': value`      | 浮点数（固定小数位）   | Number |
| `'name | +step': value`             | 递增规则         | Number |

#### 不同数据类型的修饰符行为

##### String 字符串

| 修饰符       | 描述                           | 示例                | 结果   |
|-----------|------------------------------|-------------------|------|
| `min-max` | 通过重复字符串生成新字符串，重复次数在min-max之间 | `@string("Hello") | 2-4` | "HelloHello" 或 "HelloHelloHello" |
| `count`   | 通过重复字符串生成新字符串，重复次数为count     | `@string("Hi")    | 3`   | "HiHiHi" |

##### Number 数字

| 修饰符                 | 描述                                  | 示例             | 结果        |
|---------------------|-------------------------------------|----------------|-----------|
| `+step`             | 属性值自动加step，生成递增数字                   | `@integer(100) | +1`       | 100, 101, 102... |
| `min-max`           | 生成大于等于min、小于等于max的整数                | `@integer      | 1-100`    | 1-100之间的随机整数 |
| `min-max.dmin-dmax` | 生成浮点数，整数部分在min-max间，小数位数在dmin-dmax间 | `@float        | 1-10.1-3` | 1.23 或 9.456 |
| `min-max.dcount`    | 生成浮点数，整数部分在min-max间，小数位数固定为dcount   | `@float        | 1-10.2`   | 3.14 或 7.89 |

##### Boolean 布尔值

| 修饰符       | 描述                               | 示例        | 结果   |
|-----------|----------------------------------|-----------|------|
| `min-max` | 随机生成布尔值，值为value的概率是min/(min+max) | `@boolean | 1-2` | true的概率为1/3 |
| `count`   | 随机生成布尔值，值为value的概率是1/count       | `@boolean | 1`   | 固定返回value值 |

##### Array 数组

| 修饰符       | 描述                      | 示例               | 结果   |
|-----------|-------------------------|------------------|------|
| `1`       | 从数组中随机选取1个元素            | `@array([1,2,3]) | 1`   | 1 或 2 或 3 |
| `+1`      | 从数组中顺序选取1个元素            | `@array([1,2,3]) | +1`  | 按顺序返回1,2,3,1,2,3... |
| `min-max` | 重复数组生成新数组，重复次数在min-max间 | `@array([1,2])   | 2-3` | [1,2,1,2] 或 [1,2,1,2,1,2] |
| `count`   | 重复数组生成新数组，重复次数为count    | `@array([1,2])   | 2`   | [1,2,1,2] |

#### 使用示例

```kotlin
// 字符串重复
val template1 = Mock.mock("@string('Hello')|3") // "HelloHelloHello"

// 数字递增
val template2 = Mock.mock("@integer(1000)|+1") // 1000, 1001, 1002...

// 浮点数范围
val template3 = Mock.mock("@float|1-10.2") // 3.14, 7.89等

// 数组重复
val template4 = Mock.mock("@array(['a','b'])|2-3") // ["a","b","a","b"] 或更长

// 复杂对象
val user = Mock.mock("""
{
    "id|+1": 1000,
    "name": "@name",
    "tags|1-3": ["@word"],
    "score|1-100.1-2": 1
}
""")
```

### 国际化支持

通过 `MockRandom.setLocale()` 方法可以设置语言环境，支持的功能包括：

- **姓名生成**: 根据不同语言环境生成本地化姓名
- **地址信息**: 城市、省份、街道名称等
- **公司名称**: 本地化的公司名称
- **职业名称**: 本地化的职业描述
- **电话号码**: 根据地区生成符合当地格式的电话号码

```kotlin
// 设置为英文环境
MockRandom.setLocale(Locale.ENGLISH)
val englishName = MockRandom.name() // "John Smith"

// 设置为中文环境
MockRandom.setLocale(Locale.CHINESE)
val chineseName = MockRandom.name() // "张三"
```

## 🛠️ 开发

### 项目结构

```
src/
└── main/
    ├── kotlin/io/github/spcookie/
    │   ├── LocaleManager.kt       # 国际化管理器
    │   ├── Mock.kt                # 主要模拟引擎
    │   ├── MockEngine.kt          # 模拟引擎实现
    │   ├── MockRandom.kt          # 随机数据生成器
    │   ├── ParsedRule.kt          # 规则解析数据结构
    │   ├── PlaceholderResolver.kt # 占位符解析逻辑
    │   ├── Rule.kt                # 规则定义
    │   ├── RuleExecutor.kt        # 规则执行引擎
    │   ├── RuleParser.kt          # 规则解析逻辑
    │   └── package-info.kt        # 包信息文档
    └── resources/
        ├── messages.properties    # 默认国际化资源
        ├── messages_en.properties # 英文国际化资源
        └── messages_zh.properties # 中文国际化资源
```

核心组件说明：

- **Mock.kt**: 主要的模拟数据生成入口，提供简单易用的API
- **MockEngine.kt**: 模拟引擎的核心实现，负责解析和执行模板
- **MockRandom.kt**: 随机数据生成器，提供各种类型的随机数据生成方法
- **RuleParser.kt**: 规则解析器，负责解析占位符规则
- **RuleExecutor.kt**: 规则执行器，负责执行解析后的规则
- **PlaceholderResolver.kt**: 占位符解析器，处理模板中的占位符
- **LocaleManager.kt**: 国际化管理器，支持多语言环境
- **ParsedRule.kt**: 规则解析结果的数据结构
- **Rule.kt**: 规则定义和相关枚举

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