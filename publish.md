# Maven Central Publishing Guide

This guide provides step-by-step instructions for publishing Mock4K to Maven Central Repository.

## Prerequisites

### 1. OSSRH Account Setup

1. Create a JIRA account at [OSSRH](https://issues.sonatype.org/)
2. Create a new project ticket:
   - Project: Community Support - Open Source Project Repository Hosting (OSSRH)
   - Issue Type: New Project
   - Summary: Request for com.mock4k group ID
   - Group Id: com.mock4k
   - Project URL: https://github.com/yourusername/mock4k
   - SCM URL: https://github.com/yourusername/mock4k.git

### 2. GPG Key Generation

```bash
# Generate a new GPG key
gpg --gen-key

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format LONG

# Export your public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export your private key in ASCII armor format
gpg --armor --export-secret-keys YOUR_KEY_ID
```

### 3. Configure Credentials

Create or update `~/.gradle/gradle.properties` (or project `gradle.properties`):

```properties
# OSSRH Credentials
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password

# GPG Signing
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
signingPassword=your_gpg_passphrase
```

**Important**: Never commit these credentials to version control!

## Publishing Steps

### Step 1: Verify Build

```bash
# Clean and build the project
./gradlew clean build

# Run tests
./gradlew test

# Generate documentation
./gradlew dokkaHtml
```

### Step 2: Publish to Staging Repository

```bash
# Publish to OSSRH staging repository
./gradlew publishToSonatype
```

This will:
- Compile the project
- Generate source and javadoc JARs
- Sign all artifacts with GPG
- Upload to OSSRH staging repository

### Step 3: Close and Release

Option A: Using Gradle Plugin (Recommended)
```bash
# Close and release automatically
./gradlew closeAndReleaseRepository
```

Option B: Manual Process via Nexus UI
1. Go to [OSSRH Nexus](https://s01.oss.sonatype.org/)
2. Login with your OSSRH credentials
3. Navigate to "Staging Repositories"
4. Find your staging repository (com.mock4k-XXXX)
5. Select it and click "Close"
6. Wait for validation to complete
7. If successful, click "Release"

### Step 4: Verify Publication

After release (may take 10-30 minutes):
- Check [Maven Central Search](https://search.maven.org/)
- Search for `g:com.mock4k a:mock4k`

## Alternative: Local Testing

For local testing without publishing to central:

```bash
# Publish to local Maven repository
./gradlew publishToMavenLocal
```

Then in other projects:
```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.mock4k:mock4k:1.0.0")
}
```

## Troubleshooting

### Common Issues

1. **GPG Signing Fails**
   - Ensure GPG key is properly formatted in gradle.properties
   - Check that the passphrase is correct
   - Verify the key hasn't expired

2. **OSSRH Authentication Fails**
   - Verify username/password in gradle.properties
   - Ensure OSSRH ticket is approved

3. **Validation Errors**
   - Check that all required POM elements are present
   - Ensure sources and javadoc JARs are included
   - Verify all artifacts are signed

4. **Network Issues**
   - Configure proxy settings if behind corporate firewall
   - Try using VPN if direct connection fails

### Gradle Proxy Configuration

If behind a corporate firewall, add to `gradle.properties`:

```properties
systemProp.http.proxyHost=proxy.company.com
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=proxy.company.com
systemProp.https.proxyPort=8080
```

## Version Management

For future releases:

1. Update version in `build.gradle.kts`
2. Create a git tag: `git tag v1.0.1`
3. Push tag: `git push origin v1.0.1`
4. Follow publishing steps above

## Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** for CI/CD:
   ```bash
   export OSSRH_USERNAME="your_username"
   export OSSRH_PASSWORD="your_password"
   export SIGNING_KEY="your_gpg_key"
   export SIGNING_PASSWORD="your_passphrase"
   ```
3. **Rotate keys regularly**
4. **Use separate keys** for different projects if needed

## Useful Commands

```bash
# Check what will be published
./gradlew publishToMavenLocal --dry-run

# List all publications
./gradlew tasks --group=publishing

# Verify signatures
gpg --verify mock4k-1.0.0.jar.asc mock4k-1.0.0.jar

# Check POM content
./gradlew generatePomFileForMavenPublication
cat build/publications/maven/pom-default.xml
```

## Resources

- [OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Gradle Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [Gradle Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)
- [GPG Documentation](https://gnupg.org/documentation/)