plugins {
    kotlin("jvm") version "1.9.10"
    java
    application
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.mock4k"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// 配置Java和Kotlin互操作
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.mock4k.example.ExampleKt")
}

// 配置源码和文档JAR
java {
    withSourcesJar()
    withJavadocJar()
}

// 配置Dokka文档生成
tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml").configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}

// 配置Javadoc JAR使用Dokka生成的文档
tasks.named<Jar>("javadocJar") {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
}

// 配置Maven发布
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Mock4K")
                description.set("A powerful mock data generation library for Kotlin and Java, inspired by Mock.js")
                url.set("https://github.com/yourusername/mock4k")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("yourusername")
                        name.set("Your Name")
                        email.set("your.email@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/yourusername/mock4k.git")
                    developerConnection.set("scm:git:ssh://github.com/yourusername/mock4k.git")
                    url.set("https://github.com/yourusername/mock4k")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

// 配置签名
signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

// 只在发布时进行签名
tasks.withType<Sign>().configureEach {
    onlyIf { gradle.taskGraph.hasTask("publish") }
}