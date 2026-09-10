plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

group = "com.jetbrains.teamcity.plugins.plugin-sdk-core"
version = "0.0.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://download.jetbrains.com/teamcity-repository")
    }
}

dependencies {
    compileOnly(libs.teamcity.agent.api)
    compileOnly(libs.teamcity.common.api)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.arrow.core)
    implementation(libs.kotlin.coroutines.core)

    constraints {
        implementation(libs.constraint.transitive.icu4j) {
            because("previous versions have faulty jar files which cause problems during incremental compilation (which is enabled by default since Kotlin 1.8.20)")
        }
    }

    testImplementation(kotlin("test"))

    testImplementation(libs.teamcity.agent.api)
    testImplementation(libs.teamcity.common.api)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlin.coroutines.test)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileKotlin {
        compilerOptions {
            allWarningsAsErrors.set(true)
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    compileTestKotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
