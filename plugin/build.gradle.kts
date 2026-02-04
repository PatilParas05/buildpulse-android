
plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.22"
}

group = "dev.paraspatil"
version = "0.1.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("buildPulse") {
            id = "dev.paraspatil.buildpulse-android"
            implementationClass = "dev.paraspatil.buildpulse.BuildPulsePlugin"
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.11")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
