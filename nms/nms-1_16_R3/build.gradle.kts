import io.github.zap.build.gradle.convention.*

plugins {
    id("io.github.zap.build.gradle.convention.shadow-lib")
}

description = "arena-nms_v1_16_R3"

configureShadow(File("$buildDir/libs"))

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven(zgpr("zap-commons"))
}

dependencies {
    compileOnlyApi(project(":nms:nms-common"))
    paperNms("1.16.5-R0.1-SNAPSHOT")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}