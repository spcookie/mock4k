# Mock4K

A powerful mock data generation library for Kotlin and Java, inspired by Mock.js.

## Features

- 🎯 **Easy to Use**: Simple API for generating mock data
- 🔧 **Flexible Rules**: Support for various data types and custom rules
- 🌐 **Multi-language**: Works with both Kotlin and Java
- 📊 **Rich Data Types**: Numbers, strings, booleans, arrays, and complex objects
- 🎲 **Random Generation**: Built-in random data generators
- 📝 **Template Support**: Placeholder-based template system

## Installation

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("com.mock4k:mock4k:1.0.0")
}
```

### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'com.mock4k:mock4k:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>com.mock4k</groupId>
    <artifactId>mock4k</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### Kotlin Example
```kotlin
import com.mock4k.Mock
import com.mock4k.MockRandom

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
import com.mock4k.Mock;
import com.mock4k.MockRandom;

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

## Available Rules

### Basic Types
- `@boolean` - Random boolean value
- `@integer(min, max)` - Random integer between min and max
- `@float(min, max, decimals)` - Random float with specified decimal places
- `@string(min, max)` - Random string with length between min and max

### Common Data
- `@name` - Random person name
- `@email` - Random email address
- `@phone` - Random phone number
- `@address` - Random address
- `@city` - Random city name
- `@country` - Random country name

### Arrays and Collections
- `@array(count, template)` - Generate array with specified count and template
- `@array(min, max, template)` - Generate array with random count between min and max

### Placeholders
- `@placeholder(width, height)` - Generate placeholder image URL
- `@color` - Random color value
- `@date` - Random date
- `@time` - Random time

## Building and Publishing

### Build the Project
```bash
./gradlew build
```

### Generate Documentation
```bash
./gradlew dokkaHtml
```

### Publish to Maven Central

1. **Setup OSSRH Account**
   - Create an account at [OSSRH](https://issues.sonatype.org/)
   - Create a new project ticket for your group ID

2. **Generate GPG Key**
   ```bash
   gpg --gen-key
   gpg --list-secret-keys --keyid-format LONG
   gpg --armor --export-secret-keys YOUR_KEY_ID
   ```

3. **Configure Credentials**
   Create `gradle.properties` in your home directory or project root:
   ```properties
   ossrhUsername=your_sonatype_username
   ossrhPassword=your_sonatype_password
   signingKey=your_gpg_private_key_in_ascii_armor_format
   signingPassword=your_gpg_key_passphrase
   ```

4. **Publish to Staging**
   ```bash
   ./gradlew publishToSonatype
   ```

5. **Release to Central**
   ```bash
   ./gradlew closeAndReleaseRepository
   ```

### Alternative: Publish to Local Repository
```bash
./gradlew publishToMavenLocal
```

## Development

### Project Structure
```
src/
├── main/
│   ├── kotlin/com/mock4k/
│   │   ├── Mock.kt              # Main mock engine
│   │   ├── MockRandom.kt        # Random data generators
│   │   └── example/
│   │       └── Example.kt       # Kotlin usage examples
│   └── java/com/mock4k/example/
│       └── JavaExample.java     # Java usage examples
└── test/
    └── kotlin/com/mock4k/
        └── MockTest.kt          # Unit tests
```

### Running Tests
```bash
./gradlew test
```

### Code Style
This project follows the official Kotlin coding conventions.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Inspired by [Mock.js](http://mockjs.com/)
- Built with [Kotlin](https://kotlinlang.org/)
- Documentation generated with [Dokka](https://github.com/Kotlin/dokka)