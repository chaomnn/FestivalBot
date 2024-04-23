plugins {
    kotlin("jvm") version "1.9.22"
}

group = "chaomian.baka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.telegram:telegrambotsextensions:6.9.7.1")
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")
    implementation("org.jetbrains.exposed:exposed-core:0.49.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.49.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.49.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-log4j12:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
