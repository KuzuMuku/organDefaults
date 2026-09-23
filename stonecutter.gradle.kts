plugins {
    id("dev.kikugie.stonecutter")
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("net.neoforged.moddev.legacyforge") version "2.0.141" apply false
}

stonecutter active "1.20.1-forge"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "forge")
}
