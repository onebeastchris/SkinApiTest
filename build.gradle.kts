plugins {
    java
}

group = "dev.onebeastchris"
version = "1.0.0"

val geyserApiVersion = "2.12.1-SNAPSHOT"

repositories {
    maven("https://repo.opencollab.dev/main/")
    mavenCentral()
}

dependencies {
    compileOnly("org.geysermc.geyser:api:$geyserApiVersion")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.processResources {
    filesMatching("extension.yml") {
        expand("version" to project.version)
    }
}
