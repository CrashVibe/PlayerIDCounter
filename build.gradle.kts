plugins {
    kotlin("jvm") version "2.3.10"
    `maven-publish`
}

group = "org.crashvibe.playeridcounter"
version = "0.0.1"

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.helpch.at/releases/")
    }
}

dependencies {
    compileOnly(libs.io.papermc.paper.api)
    compileOnly(libs.me.clip.placeholderapi)
}

tasks {
    withType<JavaCompile>() {
        options.encoding = "UTF-8"
    }

    withType<Javadoc>() {
        options.encoding = "UTF-8"
    }

    jar {
        from(configurations.runtimeClasspath.get().map { zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    build {
        dependsOn(jar)
    }

    kotlin {
        jvmToolchain(21)
    }

    processResources {
        filesMatching("**/plugin.yml") {
            expand("version" to project.version)
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
