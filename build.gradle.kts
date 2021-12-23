import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.0.1-rc2"
}

val publishVersion = findProperty("publishVersion")?.toString()
    ?: property("version.next").toString()

group = "nl.ncaj.tvlauncher"
version = publishVersion

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://jitpack.io")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            packageVersion = publishVersion

            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Rpm)

            macOS {
                iconFile.set(project.file("icon.icns"))
                bundleID = "nl.ncaj.tvlauncherinstaller"
            }
            windows {
                iconFile.set(project.file("icon.ico"))
            }
            linux {
                iconFile.set(project.file("icon.png"))
            }
        }
    }
}

dependencies {
    implementation("dev.mobile:dadb:0.0.7")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.json:json:20211205")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation(compose.desktop.currentOs)
    implementation("com.github.Dansoftowner:jSystemThemeDetector:3.6")
    implementation("org.slf4j:slf4j-nop:2.0.0-alpha5")
    implementation("org.jetbrains.compose.material:material-icons-core:1.0.0")
}


tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}