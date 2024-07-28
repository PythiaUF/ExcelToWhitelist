plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "uf.pcbuilding"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("com.opencsv:opencsv:5.9")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }
}