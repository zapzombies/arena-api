import io.github.zap.build.gradle.convention.*

plugins {
    id("io.github.zap.build.gradle.convention.shadow-lib")
}

description = "arena-nms-common"

configureShadow(File("$buildDir/libs"))

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven(zgpr("zap-commons"))
}

dependencies {
    paperApi("1.16.5-R0.1-SNAPSHOT")
    compileOnlyApi("io.github.zap:zap-commons:1.0.0-SNAPSHOT-1631102507")
    // compileOnlyApi("io.github.zap:zap-commons:0.0.0-SNAPSHOT")
    compileOnlyApi("com.comphenix.protocol:ProtocolLib:4.7.0")
}
