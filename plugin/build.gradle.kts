
plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.2.10"
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
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.11")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
