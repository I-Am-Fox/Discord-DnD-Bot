plugins {
    application
}

dependencies {
    implementation(project(":shared"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2")
}

application {
    mainClass.set("com.dndmusicbot.controller.ControllerApplication")
}
