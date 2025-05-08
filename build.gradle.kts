plugins {
    id("fabric-loom").version("1.10-SNAPSHOT")
    id("me.modmuss50.mod-publish-plugin").version("0.8.4")
    id("maven-publish")
}

base {
    archivesName = "${project.property("archives_base_name")}"
}

fun getModVersion(): String {
    var version = project.property("mod_version") as String
    if (System.getenv("BUILD_RELEASE") != "true" && System.getenv("JITPACK") != "true") {
        val buildNumber = System.getenv("GITHUB_RUN_NUMBER")
        version += if (buildNumber != null) ("+build.$buildNumber") else "-SNAPSHOT"
    }
    return version
}

group = "${project.property("maven_group")}"
version = "v${this.getModVersion()}-mc${project.property("minecraft_version")}"

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation(fabricApi.module("fabric-api-base", "${project.property("fabric_version")}"))
    modImplementation("maven.modrinth:fabric-permissions-api:${project.property("permission_version")}")
}

loom {
    runConfigs.configureEach {
        vmArgs("-Dmixin.debug.export=true")
    }
}

tasks.processResources {
    val modId = project.property("mod_id")
    val modName = project.property("mod_name")
    val modVersion = getModVersion()

    inputs.property("id", modId)
    inputs.property("name", modName)
    inputs.property("version", modVersion)

    filesMatching("fabric.mod.json") {
        val valueMap = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to modVersion
        )
        expand(valueMap)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.base.archivesName.get()
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}