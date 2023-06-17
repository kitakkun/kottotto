import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.github.kitakkun"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    testImplementation(kotlin("test"))

    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.10")
    // JDA-KTX
    implementation("com.github.minndevelopment:jda-ktx:0.9.5-alpha.19")

    // Exposed
    val exposedVersion = "0.41.1"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    // mysql
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("org.postgresql:postgresql:42.3.8")

    // dotenv
    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")

    // dagger
    implementation("com.google.dagger:dagger:2.44")
    annotationProcessor("com.google.dagger:dagger-compiler:2.44")
    kapt("com.google.dagger:dagger-compiler:2.44")

    // kotlin-logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("ch.qos.logback:logback-classic:1.4.1")
    implementation("org.slf4j:slf4j-api:2.0.1")

    // LavaPlayer
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    // ARM support
    implementation("com.github.aikaterna:lavaplayer-natives:original-SNAPSHOT")

    // shell command
    implementation("com.sealwu:kscript-tools:1.0.21")

    // koin
    implementation("io.insert-koin:koin-core:3.4.2")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.18")

    // gpt4all binding
    implementation("com.hexadevlabs:gpt4all-java-binding:1.1.2")
}

application {
    mainClass.set("${group}.${rootProject.name}.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
}

tasks.withType<Jar> {

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "${group}.${rootProject.name}.MainKt"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
