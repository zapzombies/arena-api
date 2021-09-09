import io.github.zap.build.gradle.convention.*

// Uncomment to use local maven version - help local testing faster
plugins {
    id("io.github.zap.build.gradle.convention.shadow-mc-plugin") version "1.0.0"
    //id("io.github.zap.build.gradle.convention.shadow-mc-plugin") version "1.0.0"
}

description = "arena-api"

repositories {
    maven("https://repo.rapture.pw/repository/maven-snapshots")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io")

    maven(zgpr("zap-commons"))
    maven(zgpr("zap-party"))
    mavenCentral()
    mavenLocal()
}

dependencies {
    shade(project(":nms:nms-common")) {
        isTransitive = false
    }
    shade(project(":nms:nms-1_16_R3"))

    shade("io.github.zap:zap-commons") {
        version {
            require("[0.0.0+, ${(project.property("zapCommonsVersion") as String)}]")

            if(project.property("preferLocal") == "true") {
                prefer("0.0.0-SNAPSHOT")
            }
        }
    }

    paperApi("1.16.5-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
    }
    implementation("com.github.Steanky:RegularCommands:master-SNAPSHOT")

    relocate("com.fasterxml.jackson.core:jackson-core:${project.property("jacksonVersion")}")
    relocate("com.fasterxml.jackson.core:jackson-databind:${project.property("jacksonVersion")}")
    relocate("com.fasterxml.jackson.core:jackson-annotations:${project.property("jacksonVersion")}")
    relocate("org.apache.commons:commons-lang3:${project.property("commonsLang3Version")}")

    bukkitPlugin("io.github.zap:zap-party") {
        version {
            require("[0.0.0+, ${(project.property("zapPartyVersion") as String)}]")

            if(project.property("preferLocal") == "true") {
                prefer("0.0.0.SNAPSHOT")
            }
        }
    }
    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.7.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.relocate {
    dependsOn(":nms:nms-common:build")
    dependsOn(":nms:nms-1_16_R3:build")
}

tasks.test {
    dependsOn(":nms:nms-common:build")
    dependsOn(":nms:nms-1_16_R3:build")
}

publishToZGpr()