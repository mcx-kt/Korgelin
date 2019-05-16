import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    application
}

group = "net.toliner.korgelin"
version = "1.0.0" // This is version of updater.

repositories {
    mavenCentral()
}

dependencies {
    val ktor_version = "1.2.0"
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-client-cio:$ktor_version")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "net.toliner.korgelin.updater.MainKt"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}