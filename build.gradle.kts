import io.github.zap.build.gradle.convention.*

// Uncomment to use local maven version - help local testing faster
plugins {
    id("io.github.zap.build.gradle.convention.shadow-mc-plugin") version "1.1.1"
}

description = "arena-api"

repositories {
    maven(zgpr("zap-commons"))
    maven(zgpr("zap-party"))
    mavenCentral()

    maven("https://repo.rapture.pw/repository/maven-snapshots")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io")
}

dependencies {
    shade(project(":nms:nms-common")) {
        isTransitive = false
    }
    shade(project(":nms:nms-1_16_R3"))

    shade("io.github.zap:zap-commons:1.1.0-SNAPSHOT-1634304479", qs())

    paperApi("1.16.5-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
    }
    shade("io.github.zap:regular-commands:1.0.1-SNAPSHOT-1633911875", qs())

    relocate("com.fasterxml.jackson.core:jackson-core:2.13.0")
    relocate("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    relocate("com.fasterxml.jackson.core:jackson-annotations:2.13.0")
    relocate("org.apache.commons:commons-lang3:3.12.0")
    relocate("it.unimi.dsi:fastutil:8.5.6")

    bukkitPlugin("io.github.zap:zap-party:1.0.0-SNAPSHOT-1638545602", qs())
    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.7.0")

    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

tasks.relocate {
    dependsOn(":nms:nms-common:build")
    dependsOn(":nms:nms-1_16_R3:build")
}

tasks.test {
    dependsOn(":nms:nms-common:build")
    dependsOn(":nms:nms-1_16_R3:build")

    testLogging {
        showStandardStreams = true
    }
}

publishToZGpr()
