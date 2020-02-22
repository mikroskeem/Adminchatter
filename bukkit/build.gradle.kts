import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

dependencies {
    implementation(project(":common"))
    implementation("org.bstats:bstats-bukkit-lite:${rootProject.extra["bstatsVersion"]}")
    implementation("net.kyori:text-adapter-bukkit:${rootProject.extra["kyoriTextVersion"]}") {
        exclude(module = "spigot-api")
        exclude(module = "gson")
    }
    compileOnly("com.destroystokyo.paper:paper-api:${rootProject.extra["paperApiVersion"]}")
}

bukkit {
    name = "Adminchatter"
    main = "eu.mikroskeem.adminchatter.bukkit.AdminchatterPlugin"
    description = "An adminchat plugin"
    authors = listOf("mikroskeem")
    apiVersion = "1.15"

    commands {
        create("adminchatter") {
            permission = "adminchatter.reload"
        }
    }

    permissions {
        create("adminchatter.reload") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}