dependencies {
    implementation("org.spongepowered:configurate-hocon:${rootProject.extra["configurateVersion"]}") {
        exclude(module = "checker-qual")
        exclude(module = "guava")
    }
    compileOnly("com.google.guava:guava:21.0") // Keep in sync with Configurate
}