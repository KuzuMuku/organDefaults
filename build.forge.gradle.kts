plugins {
    id("net.neoforged.moddev.legacyforge")
}

version = "${property("mod.version")}-${property("deps.minecraft")}-forge"
base.archivesName = property("mod.id") as String
group = property("mod.group") as String

repositories {
    mavenLocal()
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    flatDir { dir(rootProject.file("../organAPI/build/libs")) }
    flatDir { dir(rootProject.file("../organEffects/build/libs")) }
}

dependencies {
    implementation(":organapi:1.1.0-dev")
    implementation(":organeffects:1.0.0-dev")

    // EMI is published in SRG mappings; the mod* configurations remap it to official.
    modCompileOnly("dev.emi:emi-forge:1.1.24+1.20.1:api")
    modRuntimeOnly("dev.emi:emi-forge:1.1.24+1.20.1")
}

legacyForge {
    version = property("deps.minecraft") as String + "-" + property("deps.forge") as String

    if (hasProperty("deps.parchment")) parchment {
        val (mc, ver) = (property("deps.parchment") as String).split(':')
        mappingsVersion = ver
        minecraftVersion = mc
    }

    runs {
        register("client") {
            gameDirectory = file("run/")
            client()
        }
        register("server") {
            gameDirectory = file("run/")
            server()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

java {
    val javaCompat = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5")) JavaVersion.VERSION_21
    else if (stonecutter.eval(stonecutter.current.version, ">=1.18")) JavaVersion.VERSION_17
    else if (stonecutter.eval(stonecutter.current.version, ">=1.17")) JavaVersion.VERSION_16
    else JavaVersion.VERSION_1_8
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks {
    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    processResources {
        val props = mapOf(
            "mod_id" to project.property("mod.id"),
            "mod_name" to project.property("mod.name"),
            "mod_license" to project.property("mod.license"),
            "mod_version" to project.property("mod.version"),
            "mod_authors" to project.property("mod.authors"),
            "mod_description" to project.property("mod.description"),
            "minecraft_version" to project.property("deps.minecraft"),
            "minecraft_version_range" to project.property("deps.minecraft_version_range"),
            "forge_version" to project.property("deps.forge"),
            "forge_version_range" to project.property("deps.forge_version_range"),
            "loader_version_range" to project.property("deps.loader_version_range"),
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
            expand(props)
        }
    }

    jar {
        finalizedBy("reobfJar")
    }
}

// Compatibility dev jar consumed by downstream mods via flatDir as organdefaults:1.0.0-dev.
tasks.register<Jar>("devJar") {
    archiveClassifier = "dev"
    archiveVersion = project.property("mod.version") as String
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
    from(sourceSets["main"].output)
    dependsOn("classes")
}

tasks.named("build") {
    dependsOn("devJar")
}
