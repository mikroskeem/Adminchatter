plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.3.0"
}

dependencies {
    implementation(project(":common"))
    implementation("org.bstats:bstats-bungeecord-lite:${rootProject.extra["bstatsVersion"]}")
    implementation("net.kyori:adventure-platform-bungeecord:${rootProject.extra["kyoriAdapterBungeecordVersion"]}")
    compileOnly("io.github.waterfallmc:waterfall-api:${rootProject.extra["waterfallApiVersion"]}")
}

bungee {
    name = "Adminchatter"
    main = "eu.mikroskeem.adminchatter.bungee.AdminchatterPlugin"
    description = "An adminchat plugin"
    author = "${listOf("mikroskeem")}"
}