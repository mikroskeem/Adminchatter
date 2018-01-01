plugins {
    kotlin("jvm") version "1.2.10"
    id("net.minecrell.licenser") version "0.3"
    id("net.minecrell.plugin-yml.bungee") version "0.2.1"
    id("com.github.johnrengelman.shadow") version "2.0.2"
}

val gradleWrapperVersion: String by extra
val kotlinVersion: String by extra
val waterfallApiVersion: String by extra
val configurateVersion: String by extra

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        name = "destroystokyo-repo"
        setUrl("https://repo.destroystokyo.com/repository/maven-public/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation("ninja.leaping.configurate:configurate-hocon:$configurateVersion") {
        exclude(module = "guava")
    }
    compileOnly("io.github.waterfallmc:waterfall-api:$waterfallApiVersion")
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

val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    val relocations = listOf(
            "kotlin",
            "com.typesafe.config",
            "ninja.leaping.configurate"
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
    gradleVersion = gradleWrapperVersion
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
}

tasks.getByName("jar").dependsOn(tasks.getByName("shadowJar"))
defaultTasks("licenseFormat", "build")