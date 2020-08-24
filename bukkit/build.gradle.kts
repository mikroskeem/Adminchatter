import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

dependencies {
    implementation(project(":common"))
    implementation("org.bstats:bstats-bukkit-lite:${rootProject.extra["bstatsVersion"]}")
    implementation("net.kyori:adventure-platform-bukkit:${rootProject.extra["kyoriVersion"]}")
    compileOnly("com.destroystokyo.paper:paper-api:${rootProject.extra["paperApiVersion"]}")
}

bukkit {
    name = "Adminchatter"
    main = "eu.mikroskeem.adminchatter.bukkit.AdminchatterPlugin"
    description = "An adminchat plugin"
    authors = listOf("mikroskeem")
    apiVersion = "1.16"

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