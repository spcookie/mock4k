plugins {
    kotlin("jvm") version "1.9.20"
    java
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.vanniktech.maven.publish") version "0.28.0"
}

group = "io.github.spcookie"
version = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("ch.qos.logback:logback-classic:1.4.11")
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

// Configure dokka document generation
tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml").configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}

// Configure GitHub Packages publishing (optional)
publishing {
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

// Configure Signature for vanniktech plugin
signing {
    sign(publishing.publications)
}

// Configure Maven Central publishing via new Central Portal
mavenPublishing {
    configure(
        com.vanniktech.maven.publish.KotlinJvm(
            javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true
        )
    )
    
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    
    signAllPublications()
    
    pom {
        name.set("Mock4K")
        description.set("A powerful mock data generation library for Kotlin and Java")
        inceptionYear.set("2025")
        url.set("https://github.com/spcookie/mock4k")
        
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        
        developers {
            developer {
                id.set("spcookie")
                name.set("spcookie")
                url.set("https://github.com/spcookie")
                email.set("1307083930@qq.com")
            }
        }
        
        scm {
            url.set("https://github.com/spcookie/mock4k")
            connection.set("scm:git:git://github.com/spcookie/mock4k.git")
            developerConnection.set("scm:git:ssh://github.com/spcookie/mock4k.git")
        }
    }
}