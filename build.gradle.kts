plugins {
    kotlin("jvm") version "2.3.10"
    id("com.gradleup.shadow") version "9.3.1"
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

    maven {
        url = uri("https://repo.tcoded.com/releases/")
    }
}

dependencies {
    compileOnly(libs.io.papermc.paper.api)
    compileOnly(libs.me.clip.placeholderapi)

    implementation(libs.de.exlll.configlib.yaml)
    implementation(libs.com.tcoded.foliaLib)
}

tasks {
    withType<JavaCompile>() {
        options.encoding = "UTF-8"
    }

    withType<Javadoc>() {
        options.encoding = "UTF-8"
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.version}.${archiveExtension.get()}")
        exclude("META-INF/**")
        minimize {
            exclude(dependency("com.tcoded:FoliaLib:.*"))
        }
        relocate("de.exlll.configlib", "${project.group}.libs.configlib")
        relocate("com.tcoded.folialib", "${project.group}.libs.folialib")
    }

    build {
        dependsOn(shadowJar)
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
