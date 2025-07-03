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

- [特性](#-特性)
- [安装](#-安装)
- [快速开始](#-快速开始)
- [可用规则](#-可用规则)
- [构建和发布](#-构建和发布)
- [开发](#-开发)
- [贡献](#-贡献)
- [许可证](#-许可证)
- [致谢](#-致谢)

## ✨ 特性

- 🎯 **易于使用**: 简单的 API 用于生成模拟数据
- 🔧 **灵活规则**: 支持各种数据类型和自定义规则
- 🌐 **多语言**: 同时支持 Kotlin 和 Java
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
    }
}
```

## 📚 可用规则

### 基础类型

| 规则                           | 描述                 | 示例                           |
|------------------------------|--------------------|------------------------------|
| `@boolean`                   | 随机布尔值              | `true` 或 `false`             |
| `@integer(min, max)`         | 在最小值和最大值之间的随机整数    | `@integer(1,100)` → `42`     |
| `@float(min, max, decimals)` | 指定小数位数的随机浮点数       | `@float(1,10,2)` → `3.14`    |
| `@string(min, max)`          | 长度在最小值和最大值之间的随机字符串 | `@string(5,10)` → `"abcdef"` |

### 常用数据

| 规则         | 描述     | 示例输出                 |
|------------|--------|----------------------|
| `@name`    | 随机人名   | `"John Doe"`         |
| `@email`   | 随机邮箱地址 | `"user@example.com"` |
| `@phone`   | 随机电话号码 | `"+1-555-123-4567"`  |
| `@address` | 随机地址   | `"123 Main St"`      |
| `@city`    | 随机城市名称 | `"New York"`         |
| `@country` | 随机国家名称 | `"United States"`    |

### 数组和集合

| 规则                           | 描述                  | 示例                             |
|------------------------------|---------------------|--------------------------------|
| `@array(count, template)`    | 使用指定数量和模板生成数组       | `@array(3, @string(5))`        |
| `@array(min, max, template)` | 生成数量在最小值和最大值之间的随机数组 | `@array(1,5, @integer(1,100))` |

### 占位符

| 规则                            | 描述          | 示例输出                                    |
|-------------------------------|-------------|-----------------------------------------|
| `@placeholder(width, height)` | 生成占位符图片 URL | `"https://via.placeholder.com/300x200"` |
| `@color`                      | 随机颜色值       | `"#FF5733"`                             |
| `@date`                       | 随机日期        | `"2023-12-25"`                          |
| `@time`                       | 随机时间        | `"14:30:00"`                            |

## 🔨 构建和发布

### 构建项目

```bash
./gradlew build
```

### 生成文档

```bash
./gradlew dokkaHtml
```

### 发布到 Maven Central

#### 1. 设置 OSSRH 账户

- 在 [OSSRH](https://issues.sonatype.org/) 创建账户
- 为您的组 ID 创建新的项目工单

#### 2. 生成 GPG 密钥

```bash
gpg --gen-key
gpg --list-secret-keys --keyid-format LONG
gpg --armor --export-secret-keys YOUR_KEY_ID
```

#### 3. 配置凭据

在您的主目录或项目根目录中创建 `gradle.properties`:

```properties
centralUsername=your_sonatype_username
centralPassword=your_sonatype_password
signingKey=your_gpg_private_key_in_ascii_armor_format
signingPassword=your_gpg_key_passphrase
```

#### 4. 设置 GitHub Actions 密钥（用于自动发布）

如果您使用 GitHub Actions 进行自动发布，请将这些密钥添加到您的仓库中：

- `CENTRAL_USERNAME`: 您的 Sonatype 用户名
- `CENTRAL_PASSWORD`: 您的 Sonatype 密码
- `SIGNING_KEY`: ASCII armor 格式的 GPG 私钥
- `SIGNING_PASSWORD`: 您的 GPG 密钥密码短语
- `GITHUB_TOKEN`: 由 GitHub Actions 自动提供

#### 5. 验证构建

```bash
# 清理并构建项目
./gradlew clean build

# 运行测试
./gradlew test

# 生成文档
./gradlew dokkaHtml
```

#### 6. 发布到暂存区

```bash
./gradlew publishToSonatype
```

#### 7. 发布到 Central

```bash
./gradlew closeAndReleaseSonatypeStagingRepository
```

#### 8. 通过 GitHub Actions 自动发布

项目包含 GitHub Actions 工作流，可以自动：

- 在每次发布时发布到 GitHub Packages
- 在创建发布时发布到 Maven Central（如果配置了密钥）

要触发自动发布，请在 GitHub 上创建新的发布。

### 发布到 GitHub Packages

#### 1. 设置 GitHub 个人访问令牌

- 转到 GitHub 设置 > 开发者设置 > 个人访问令牌
- 生成具有 `write:packages` 权限的新令牌
- 复制令牌以备后用

#### 2. 配置凭据

添加到您的 `gradle.properties` 文件：

```properties
gpr.user=your_github_username
gpr.key=your_github_personal_access_token
```

或设置环境变量：

```bash
export USERNAME=your_github_username
export TOKEN=your_github_personal_access_token
```

#### 3. 发布到 GitHub Packages

```bash
# 构建并发布到 GitHub Packages
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

#### 4. 验证发布

检查您的 GitHub 仓库的 "Packages" 选项卡以验证发布是否成功。

### 替代方案：发布到本地仓库

```bash
./gradlew publishToMavenLocal
```

## 🛠️ 开发

### 项目结构
```
src/
├── main/
│   └── kotlin/io/github/spcookie/
│       ├── Mock.kt                # 主要模拟引擎
│       ├── MockEngine.kt          # 模拟引擎实现
│       ├── MockRandom.kt          # 随机数据生成器
│       ├── ParsedRule.kt          # 规则解析数据结构
│       ├── PlaceholderResolver.kt # 占位符解析逻辑
│       ├── Rule.kt                # 规则定义
│       ├── RuleExecutor.kt        # 规则执行引擎
│       └── RuleParser.kt          # 规则解析逻辑
└── test/
    └── kotlin/io/github/spcookie/
        ├── Example.kt           # 使用示例
        └── MockTest.kt          # 单元测试
```

### 运行测试

```bash
./gradlew test
```

### 代码风格

本项目遵循 [官方 Kotlin 编码约定](https://kotlinlang.org/docs/coding-conventions.html)。

## 🤝 贡献

欢迎贡献！请随时提交 Pull Request。

1. Fork 项目
2. 创建您的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开 Pull Request

请确保适当更新测试并遵循现有的代码风格。

## 📄 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

## 🙏 致谢

- 使用 [Kotlin](https://kotlinlang.org/) 构建 - JVM 的现代编程语言
- 文档由 [Dokka](https://github.com/Kotlin/dokka) 生成 - Kotlin 文档引擎

---

<div align="center">
  <sub>Built with ❤️ by the Spcookie.</sub>
</div>