plugins {
    application
}

dependencies {
    implementation(project(":shared"))
    implementation("io.javalin:javalin:7.2.2")
    implementation("net.dv8tion:JDA:6.4.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2")
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
}

application {
    mainClass.set("com.dndmusicbot.bot.BotServerApplication")
}

tasks.register<Jar>("shadowJar") {
    group = "build"
    description = "Builds a runnable fat jar for hosted deployment."
    archiveBaseName.set("dnd-music-bot-server")
    archiveVersion.set("")
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
    }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}
