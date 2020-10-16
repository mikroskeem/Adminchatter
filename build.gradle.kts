import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10" apply false
    id("net.minecrell.licenser") version "0.4.1"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

extra["paperApiVersion"] = "1.16.1-R0.1-SNAPSHOT"
extra["waterfallApiVersion"] = "1.16-R0.4-SNAPSHOT"
extra["velocityApiVersion"] = "1.1.0-SNAPSHOT"
extra["configurateVersion"] = "3.7-SNAPSHOT"
extra["bstatsVersion"] = "1.4"
extra["kyoriVersion"] = "4.0.1"
extra["kyoriAdapterBukkitVersion"] = "4.0.0-SNAPSHOT"
extra["kyoriAdapterBungeecordVersion"] = "4.0.0-SNAPSHOT"

allprojects {
    group = "eu.mikroskeem"
    version = "0.0.14-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()

        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://repo.spongepowered.org/maven")
        maven("https://repo.velocitypowered.com/snapshots/")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "net.minecrell.licenser")

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            languageVersion = "1.4"
        }
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("net.kyori:adventure-api:${rootProject.extra["kyoriVersion"]}")
        implementation("net.kyori:adventure-text-serializer-legacy:${rootProject.extra["kyoriVersion"]}")
    }

    license {
        header = rootProject.file("etc/HEADER")
        filter.include("**/*.java")
        filter.include("**/*.kt")
    }
}

dependencies {
    implementation(project(":bukkit"))
    implementation(project(":bungee"))
    implementation(project(":velocity"))
}

val shadowJar by tasks.getting(ShadowJar::class) {
    val relocations = listOf(
            "kotlin",
            "com.typesafe.config",
            "ninja.leaping.configurate",
            "org.bstats",
            "net.kyori"
    )
    val targetPackage = "eu.mikroskeem.adminchatter.lib"

    relocations.forEach {
        relocate(it, "$targetPackage.$it")
    }

    exclude("org/jetbrains/annotations/**")
    exclude("org/intellij/lang/annotations/**")
    exclude("org/checkerframework/**")
    exclude("META-INF/maven/**")

    // Also don't include Velocity codebase and files here
    exclude("eu/mikroskeem/adminchatter/velocity/**")
    exclude("velocity-plugin.json")
}

val shadowJarVelocity by tasks.creating(ShadowJar::class) {
    archiveClassifier.set("velocity-all")
    configurations = listOf(project.configurations["runtimeClasspath"])

    val relocations = listOf(
            "kotlin",
            "com.typesafe.config",
            "ninja.leaping.configurate",
            "org.bstats"
    )
    val targetPackage = "eu.mikroskeem.adminchatter.lib"

    relocations.forEach {
        relocate(it, "$targetPackage.$it")
    }

    exclude("org/jetbrains/annotations/**")
    exclude("org/intellij/lang/annotations/**")
    exclude("org/checkerframework/**")
    exclude("META-INF/maven/**")

    // Don't include Bukkit or Bungee codebase and files here
    exclude("eu/mikroskeem/adminchatter/bukkit/**")
    exclude("eu/mikroskeem/adminchatter/bungee/**")
    exclude("plugin.yml")
    exclude("bungee.yml")

    // Provided by Velocity
    exclude("net/kyori/**")
}

tasks["jar"].dependsOn(shadowJar, shadowJarVelocity)
defaultTasks("licenseFormat", "build")
