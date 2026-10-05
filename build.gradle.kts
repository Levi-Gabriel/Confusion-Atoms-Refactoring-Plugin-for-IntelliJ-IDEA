import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.anon"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.2")
        bundledPlugin("com.intellij.java")
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2024.2")
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2025.1")
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2024.2")
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2025.1")
        }
    }
}

tasks {
    withType<JavaCompile> {
        options.release = 21
        options.encoding = "UTF-8"
    }
}

