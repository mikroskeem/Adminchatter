plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:${rootProject.extra["velocityApiVersion"]}")
    kapt("com.velocitypowered:velocity-api:${rootProject.extra["velocityApiVersion"]}")
}