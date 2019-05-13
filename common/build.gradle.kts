dependencies {
    implementation("org.spongepowered:configurate-hocon:${rootProject.extra["configurateVersion"]}") {
        exclude(module = "checker-qual")
    }
}