
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.diffplug.spotless")
    id("io.github.rodm.teamcity-base")
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

project.group = "teamcity-unreal-engine-plugin"
project.version = if (project.findProperty("version") == "unspecified") {
    "SNAPSHOT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss"))
} else {
    project.version
}

val libs = the<LibrariesForLibs>()

teamcity {
    // Compile against published OpenAPI artifacts (patch builds are often not published to Maven).
    version = libs.versions.teamcity.api.get()
    validateBeanDefinition = com.github.rodm.teamcity.ValidationMode.FAIL
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

spotless {
    kotlin {
        ktlint(libs.versions.ktlint.get())
            .setEditorConfigPath("${rootDir}/.editorconfig")
    }
}

tasks {
    compileKotlin {
        compilerOptions {
            allWarningsAsErrors.set(true)
            freeCompilerArgs.add("-Xcontext-parameters")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    compileTestKotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
