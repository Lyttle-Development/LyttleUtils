import java.io.ByteArrayOutputStream

plugins {
    java
    `java-library`
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

// Project coordinates
group = "com.lyttledev"
version = (property("pluginVersion") as String)
description = "LyttleUtils"
java.sourceCompatibility = JavaVersion.VERSION_21

// Java toolchain and compatibility
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Repositories
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // PaperMC
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://repo.extendedclip.com/releases/") // PlaceholderAPI
    maven("https://repo.william278.net/releases/") // PAPI Proxy Bridge
}

// Dependencies
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.william278:papiproxybridge:1.8.1")
}

// run-paper plugin configuration
tasks {
    runServer {
        minecraftVersion("1.21")
    }
}

// Compile options
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (JavaVersion.current().isJava10Compatible()) {
        options.release.set(21)
    }
}

// Git helper functions
fun executeGitCommand(vararg command: String): String {
    val output = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = output
    }
    return output.toString(Charsets.UTF_8.name()).trim()
}

fun latestCommitMessage(): String = executeGitCommand("log", "-1", "--pretty=%B")

// Version string logic
val versionString: String = if (System.getenv("CHANNEL") == "Release") {
    version.toString()
} else {
    val versionPrefix = if (System.getenv("CHANNEL") == "Snapshot") {
        "SNAPSHOT"
    } else {
        "ALPHA"
    }

    if (System.getenv("GITHUB_RUN_NUMBER") != null) {
        "${version}-${versionPrefix}.${System.getenv("GITHUB_RUN_NUMBER")}"
    } else {
        "$version-${versionPrefix}"
    }
}

// Process resources: expand plugin.yml
tasks.named<ProcessResources>("processResources") {
    inputs.property("version", versionString)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(
            "version" to versionString,
            "projectVersion" to versionString
        )
    }
}

// Publishing to Maven (GitHub Packages)
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "lyttleutils"
            version = versionString
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Lyttle-Development/LyttleUtils")
            credentials {
                username = System.getenv("GPR_USER") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GPR_API_KEY") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}

// Logging
println("Version: $versionString")
println("Changelog: ${latestCommitMessage()}")
