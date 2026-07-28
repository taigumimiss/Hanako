plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven ("https://repo.codemc.io/repository/maven-releases/")
    maven ("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
    compileOnly("dev.folia:folia-api:[26.1.2.build,)")
    implementation("io.lettuce:lettuce-core:6.5.2.RELEASE")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
