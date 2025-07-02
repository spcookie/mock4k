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

A powerful mock data generation library for Kotlin and Java.

## 📋 Table of Contents

- [Features](#-features)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Available Rules](#-available-rules)
- [Building and Publishing](#-building-and-publishing)
- [Development](#-development)
- [Contributing](#-contributing)
- [License](#-license)
- [Acknowledgments](#-acknowledgments)

## ✨ Features

- 🎯 **Easy to Use**: Simple API for generating mock data
- 🔧 **Flexible Rules**: Support for various data types and custom rules
- 🌐 **Multi-language**: Works with both Kotlin and Java
- 📊 **Rich Data Types**: Numbers, strings, booleans, arrays, and complex objects
- 🎲 **Random Generation**: Built-in random data generators
- 📝 **Template Support**: Placeholder-based template system

## 📦 Installation

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

You can also install Mock4K from GitHub Packages:

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

> **Note**: To use GitHub Packages, you need to authenticate with a GitHub Personal Access Token. You can create one in your GitHub account settings under "Developer settings" > "Personal access tokens" with `read:packages` permission.

## 🚀 Quick Start

### Kotlin Example
```kotlin
import io.github.spcookie.Mock
import io.github.spcookie.MockRandom

fun main() {
    // Basic mock
    val name = Mock.mock("@name")
    println("Name: $name")
    
    // Number rules
    val number = Mock.mock("@integer(1,100)")
    println("Number: $number")
    
    // String rules
    val email = Mock.mock("@email")
    println("Email: $email")
    
    // Array rules
    val array = Mock.mock("@array(5, @string(3,10))")
    println("Array: $array")
    
    // Complex template
    val user = Mock.mock("""
        {
            "id": "@integer(1,1000)",
            "name": "@name",
            "email": "@email",
            "age": "@integer(18,65)",
            "active": "@boolean"
        }
    """)
    println("User: $user")
    
    // Direct random usage
    val randomInt = MockRandom.integer(1, 100)
    val randomString = MockRandom.string(5, 15)
    println("Random Int: $randomInt, Random String: $randomString")
}
```

### Java Example
```java
import io.github.spcookie.Mock;
import io.github.spcookie.MockRandom;

public class JavaExample {
    public static void main(String[] args) {
        // Basic mock
        String name = Mock.mock("@name");
        System.out.println("Name: " + name);
        
        // Number rules
        String number = Mock.mock("@integer(1,100)");
        System.out.println("Number: " + number);
        
        // String rules
        String email = Mock.mock("@email");
        System.out.println("Email: " + email);
        
        // Array rules
        String array = Mock.mock("@array(5, @string(3,10))");
        System.out.println("Array: " + array);
        
        // Complex template
        String user = Mock.mock(
            "{" +
            "\"id\": \"@integer(1,1000)\"," +
            "\"name\": \"@name\"," +
            "\"email\": \"@email\"," +
            "\"age\": \"@integer(18,65)\"," +
            "\"active\": \"@boolean\"" +
            "}"
        );
        System.out.println("User: " + user);
        
        // Direct random usage
        int randomInt = MockRandom.integer(1, 100);
        String randomString = MockRandom.string(5, 15);
        System.out.println("Random Int: " + randomInt + ", Random String: " + randomString);
    }
}
```

## 📚 Available Rules

### Basic Types

| Rule                         | Description                                   | Example                      |
|------------------------------|-----------------------------------------------|------------------------------|
| `@boolean`                   | Random boolean value                          | `true` or `false`            |
| `@integer(min, max)`         | Random integer between min and max            | `@integer(1,100)` → `42`     |
| `@float(min, max, decimals)` | Random float with specified decimal places    | `@float(1,10,2)` → `3.14`    |
| `@string(min, max)`          | Random string with length between min and max | `@string(5,10)` → `"abcdef"` |

### Common Data

| Rule       | Description          | Example Output       |
|------------|----------------------|----------------------|
| `@name`    | Random person name   | `"John Doe"`         |
| `@email`   | Random email address | `"user@example.com"` |
| `@phone`   | Random phone number  | `"+1-555-123-4567"`  |
| `@address` | Random address       | `"123 Main St"`      |
| `@city`    | Random city name     | `"New York"`         |
| `@country` | Random country name  | `"United States"`    |

### Arrays and Collections

| Rule                         | Description                                          | Example                        |
|------------------------------|------------------------------------------------------|--------------------------------|
| `@array(count, template)`    | Generate array with specified count and template     | `@array(3, @string(5))`        |
| `@array(min, max, template)` | Generate array with random count between min and max | `@array(1,5, @integer(1,100))` |

### Placeholders

| Rule                          | Description                    | Example Output                          |
|-------------------------------|--------------------------------|-----------------------------------------|
| `@placeholder(width, height)` | Generate placeholder image URL | `"https://via.placeholder.com/300x200"` |
| `@color`                      | Random color value             | `"#FF5733"`                             |
| `@date`                       | Random date                    | `"2023-12-25"`                          |
| `@time`                       | Random time                    | `"14:30:00"`                            |

## 🔨 Building and Publishing

### Build the Project

```bash
./gradlew build
```

### Generate Documentation

```bash
./gradlew dokkaHtml
```

### Publish to Maven Central

#### 1. Setup OSSRH Account

- Create an account at [OSSRH](https://issues.sonatype.org/)
- Create a new project ticket for your group ID

#### 2. Generate GPG Key

```bash
gpg --gen-key
gpg --list-secret-keys --keyid-format LONG
gpg --armor --export-secret-keys YOUR_KEY_ID
```

#### 3. Configure Credentials

Create `gradle.properties` in your home directory or project root:

```properties
centralUsername=your_sonatype_username
centralPassword=your_sonatype_password
signingKey=your_gpg_private_key_in_ascii_armor_format
signingPassword=your_gpg_key_passphrase
```

#### 4. Setup GitHub Actions Secrets (for automated publishing)

If you're using GitHub Actions for automated publishing, add these secrets to your repository:

- `CENTRAL_USERNAME`: Your Sonatype username
- `CENTRAL_PASSWORD`: Your Sonatype password
- `SIGNING_KEY`: Your GPG private key in ASCII armor format
- `SIGNING_PASSWORD`: Your GPG key passphrase
- `GITHUB_TOKEN`: Automatically provided by GitHub Actions

#### 5. Verify Build

```bash
# Clean and build the project
./gradlew clean build

# Run tests
./gradlew test

# Generate documentation
./gradlew dokkaHtml
```

#### 6. Publish to Staging

```bash
./gradlew publishToSonatype
```

#### 7. Release to Central

```bash
./gradlew closeAndReleaseSonatypeStagingRepository
```

#### 8. Automated Publishing via GitHub Actions

The project includes GitHub Actions workflow that automatically:
- Publishes to GitHub Packages on every release
- Publishes to Maven Central when a release is created (if secrets are configured)

To trigger automated publishing, create a new release on GitHub.

### Publish to GitHub Packages

#### 1. Setup GitHub Personal Access Token

- Go to GitHub Settings > Developer settings > Personal access tokens
- Generate a new token with `write:packages` permission
- Copy the token for later use

#### 2. Configure Credentials

Add to your `gradle.properties` file:

```properties
gpr.user=your_github_username
gpr.key=your_github_personal_access_token
```

Or set environment variables:

```bash
export USERNAME=your_github_username
export TOKEN=your_github_personal_access_token
```

#### 3. Publish to GitHub Packages

```bash
# Build and publish to GitHub Packages
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

#### 4. Verify Publication

Check your GitHub repository's "Packages" tab to verify the publication was successful.

### Alternative: Publish to Local Repository

```bash
./gradlew publishToMavenLocal
```

## 🛠️ Development

### Project Structure
```
src/
├── main/
│   └── kotlin/io/github/spcookie/
│       ├── Mock.kt              # Main mock engine
│       ├── MockEngine.kt        # Mock engine implementation
│       ├── MockRandom.kt        # Random data generators
│       ├── ParsedRule.kt        # Rule parsing data structures
│       ├── PlaceholderResolver.kt # Placeholder resolution logic
│       ├── Rule.kt              # Rule definitions
│       ├── RuleExecutor.kt      # Rule execution engine
│       └── RuleParser.kt        # Rule parsing logic
└── test/
    └── kotlin/io/github/spcookie/
        ├── Example.kt           # Usage examples
        └── MockTest.kt          # Unit tests
```

### Running Tests

```bash
./gradlew test
```

### Code Style

This project follows the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please make sure to update tests as appropriate and follow the existing code style.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with [Kotlin](https://kotlinlang.org/) - A modern programming language for the JVM
- Documentation generated with [Dokka](https://github.com/Kotlin/dokka) - Documentation engine for Kotlin

---

<div align="center">
  <sub>Built with ❤️ by the Spcookie.</sub>
</div>