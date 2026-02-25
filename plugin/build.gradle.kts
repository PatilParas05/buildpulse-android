
plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.2.0"
    `maven-publish`
}

group = "com.github.PatilParas05"
version = "0.1.6"

repositories {
    mavenCentral()
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
gradlePlugin {
    plugins {
        create("buildPulse") {
            id = "dev.paraspatil.buildpulse-android"
            implementationClass = "dev.paraspatil.buildpulse.BuildPulsePlugin"
            displayName = "BuildPulse Android"
            description = "Tracks and reports Gradle build performance metrics for Android projects"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            artifactId = "buildpulse-android"

        }
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(localGroovy())

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.11")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
