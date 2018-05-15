import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.2.41"
    id("net.minecrell.licenser") version "0.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.2.1"
    id("net.minecrell.plugin-yml.bungee") version "0.2.1"
    id("com.github.johnrengelman.shadow") version "2.0.2"
}

group = "eu.mikroskeem"
version = "0.0.9"

val paperApiVersion = "1.12.2-R0.1-SNAPSHOT"
val waterfallApiVersion = "1.12-SNAPSHOT"
val configurateVersion = "3.3"
val bstatsVersion = "1.2"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.destroystokyo.com/repository/maven-public/")
    maven("http://repo.bstats.org/content/repositories/releases/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("ninja.leaping.configurate:configurate-hocon:$configurateVersion") {
        exclude(module = "guava")
    }
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

val wrapper by tasks.creating(Wrapper::class) {
    gradleVersion = "4.7"
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
}

tasks["jar"].dependsOn(shadowJar)
defaultTasks("licenseFormat", "build")