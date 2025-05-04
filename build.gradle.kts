import java.io.ByteArrayOutputStream

plugins {
    `java-library`
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.lyttledev"
version = (property("pluginVersion") as String)
description = "LyttleUtils"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = "https://repo.papermc.io/repository/maven-public/"
    }
    maven {
        name = "sonatype"
        url = "https://oss.sonatype.org/content/groups/public/"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

def targetJavaVersion = 21
java {
    def javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        options.release.set(targetJavaVersion)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

// Add -SNAPSHOT to the version if the channel is not Release
val versionString: String = if (System.getenv("CHANNEL") == "Release") {
    version.toString()
} else {
    val versionPrefix = if (System.getenv("CHANNEL") == "Snapshot") {
        "SNAPSHOT"
    } else {
        "ALPHA"
    }

    if (System.getenv("GITHUB_RUN_NUMBER") != null) {
        "${version}-${versionPrefix}+${System.getenv("GITHUB_RUN_NUMBER")}"
    } else {
        "$version-${versionPrefix}"
    }
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("plugin.yml") {
        expand("projectVersion" to versionString)
    }
}


publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java

            groupId = 'com.lyttledev'
            artifactId = 'lyttleutils'
            version = '1.0.0' // Update as needed
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Lyttle-Development/LyttleUtils")
            credentials {
                username = project.findProperty("gpr.user") ?: System.getenv("GPR_USER")
                password = project.findProperty("gpr.key") ?: System.getenv("GPR_API_KEY")
            }
        }
    }
}
