
plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.2.10"
    `maven-publish`
}

group = "com.github.PatilParas05"
version = "0.1.4"

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

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            artifactId = "buildpulse-android"

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
