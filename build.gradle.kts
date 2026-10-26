plugins {
    // Applies the correct Fabric Loom variant for the active Minecraft version.
    id("dev.kikugie.loom-back-compat")
}

// Do not set group here; Loom needs it from gradle.properties.
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

repositories {
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
}

loom {
    runConfigs.configureEach {
        preferGradleTask = true
        runDirectory = rootProject.file("run")
    }
    @Suppress("UnstableApiUsage")
    mods {
        create("voltliz") { sourceSet(sourceSets["main"]) }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to sc.properties.get<String>("mod.version"),
        "minecraft" to sc.properties.get<String>("mod.mc_compat")
    )
    for ((key, value) in props) inputs.property(key, value)
    filesMatching("fabric.mod.json") { expand(props) }
}

// Collects each version's mod jar into build/libs/<mc version>/; run it from the root to build every version.
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the mod jar and copies it to build/libs/<minecraft version>/"
    from(loomx.modJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs/${sc.current.version}"))
    dependsOn("build")
}
