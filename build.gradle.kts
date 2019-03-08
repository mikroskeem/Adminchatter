import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.21"
    id("net.minecrell.licenser") version "0.4.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
    id("net.minecrell.plugin-yml.bungee") version "0.3.0"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "eu.mikroskeem"
version = "0.0.10-SNAPSHOT"

val paperApiVersion = "1.13.2-R0.1-SNAPSHOT"
val waterfallApiVersion = "1.13-SNAPSHOT"
val configurateVersion = "3.6"
val bstatsVersion = "1.2"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.destroystokyo.com/repository/maven-public/")
    maven("http://repo.bstats.org/content/repositories/releases/")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.spongepowered:configurate-hocon:$configurateVersion") {
        exclude(module = "guava")
    }
    implementation("org.bstats:bstats-bukkit:$bstatsVersion")
    implementation("org.bstats:bstats-bungeecord:$bstatsVersion")

    compileOnly("io.github.waterfallmc:waterfall-api:$waterfallApiVersion")
    compileOnly("com.destroystokyo.paper:paper-api:$paperApiVersion")
}

license {
    header = rootProject.file("etc/HEADER")
    filter.include("**/*.kt")
}

bungee {
    name = "Adminchatter"
    main = "eu.mikroskeem.adminchatter.AdminchatterPlugin"
    description = "An adminchat plugin"
    author = "${listOf("mikroskeem")}"
}

bukkit {
    name = "Adminchatter"
    main = "eu.mikroskeem.adminchatter.AdminchatterPluginBukkit"
    description = "An adminchat plugin. Companion version on Bukkit to play sounds"
    authors = listOf("mikroskeem")
}

val shadowJar by tasks.getting(ShadowJar::class) {
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

    dependencies {
        exclude("org/jetbrains/annotations/**")
        exclude("org/intellij/lang/annotations/**")
        exclude("META-INF/maven/**")
    }
}

tasks["jar"].dependsOn(shadowJar)
defaultTasks("licenseFormat", "build")
