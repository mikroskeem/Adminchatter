import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    id("net.minecrell.licenser") version "0.4.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
    id("net.minecrell.plugin-yml.bungee") version "0.3.0"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "eu.mikroskeem"
version = "0.0.11-SNAPSHOT"

val paperApiVersion = "1.13.2-R0.1-SNAPSHOT"
val waterfallApiVersion = "1.13-SNAPSHOT"
val configurateVersion = "3.7-SNAPSHOT"
val bstatsVersion = "1.4"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.spongepowered:configurate-hocon:$configurateVersion") {
        exclude(module = "guava")
    }
    implementation("org.bstats:bstats-bukkit-lite:$bstatsVersion")
    implementation("org.bstats:bstats-bungeecord-lite:$bstatsVersion")

    compileOnly("io.github.waterfallmc:waterfall-api:$waterfallApiVersion")
    compileOnly("com.destroystokyo.paper:paper-api:$paperApiVersion")
}

license {
    header = rootProject.file("etc/HEADER")
    filter.include("**/*.kt")
}

bungee {
    name = "Adminchatter"
    main = "eu.mikroskeem.adminchatter.bungee.AdminchatterPlugin"
    description = "An adminchat plugin"
    author = "${listOf("mikroskeem")}"
}

bukkit {
    name = "Adminchatter"
    main = "eu.mikroskeem.adminchatter.bukkit.AdminchatterPlugin"
    description = "An adminchat plugin"
    authors = listOf("mikroskeem")
    apiVersion = "1.13"

    commands {
        create("adminchatter") {
            permission = "adminchatter.reload"
        }
    }

    permissions {
        create("adminchatter.reload") {
            default = Default.OP
        }
    }
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

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
}

tasks["jar"].dependsOn(shadowJar)
defaultTasks("licenseFormat", "build")
