plugins {
    kotlin("jvm") version "1.9.10"
    java
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "io.github.spcookie"
version = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // SLF4J logging framework - API only for library
    implementation("org.slf4j:slf4j-api:2.0.9")
    
    // Logback for testing only
    testImplementation("ch.qos.logback:logback-classic:1.4.11")
    
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
}

// Configure java and kotlin interoperability
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

// Configure source code and document jar
java {
    withSourcesJar()
    withJavadocJar()
}

// Configure dokka document generation
tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml").configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}

// Configuring Javadoc JAR using documentation generated by Dokka
tasks.named<Jar>("javadocJar") {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
}

// Configure Maven release
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Mock4K")
                description.set("A powerful mock data generation library for Kotlin and Java")
                url.set("https://github.com/spcookie/mock4k")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("spcookie")
                        name.set("spcookie")
                        email.set("1307083930@qq.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/spcookie/mock4k.git")
                    developerConnection.set("scm:git:ssh://github.com/spcookie/mock4k.git")
                    url.set("https://github.com/spcookie/mock4k")
                }
            }
        }
    }
    
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
}

// Configure Signature
signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

// Configure Nexus publishing for Central Portal
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(project.findProperty("centralUsername") as String? ?: System.getenv("CENTRAL_USERNAME"))
            password.set(project.findProperty("centralPassword") as String? ?: System.getenv("CENTRAL_PASSWORD"))
        }
    }
}