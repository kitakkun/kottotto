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
    implementation("net.dv8tion:JDA:5.0.0-alpha.19")
    // JDA-KTX
    implementation("com.github.minndevelopment:jda-ktx:0.9.5-alpha.19")

    // Exposed
    implementation("org.jetbrains.exposed", "exposed-core", "0.39.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.39.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.39.1")
    // mysql
    implementation("mysql:mysql-connector-java:8.0.30")

    // dotenv
    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")

    // dagger
    implementation("com.google.dagger:dagger:2.44")
    annotationProcessor("com.google.dagger:dagger-compiler:2.44")
    kapt("com.google.dagger:dagger-compiler:2.44")
}

application {
    mainClass.set("${group}.${rootProject.name}.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
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
