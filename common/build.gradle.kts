dependencies {
    implementation("org.spongepowered:configurate-hocon:${rootProject.extra["configurateVersion"]}") {
        exclude(module = "guava")
        exclude(module = "checker-qual")
    }
}