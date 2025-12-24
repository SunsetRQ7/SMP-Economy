plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.kyori.blossom") version "1.3.1"
}

group = "com.sunsetrq7"
version = "2.0.0"
description = "Advanced Economy Plugin for SMP Servers"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/central/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.testcontainers:testcontainers:1.19.7")
    testImplementation("org.testcontainers:mysql:1.19.7")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    
    shadowJar {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "com.sunsetrq7.smpeconomy.libs.hikari")
        relocate("org.sqlite", "com.sunsetrq7.smpeconomy.libs.sqlite")
        relocate("com.mysql", "com.sunsetrq7.smpeconomy.libs.mysql")
        relocate("org.mariadb", "com.sunsetrq7.smpeconomy.libs.mariadb")
    }
    
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    
    test {
        useJUnitPlatform()
    }
    
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}