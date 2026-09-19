import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import xyz.jpenilla.runpaper.task.RunServer
import xyz.jpenilla.runtask.service.DownloadsAPIService

plugins {
    id("xyz.jpenilla.run-paper")
}

val versions = extensions.getByType<VersionCatalogsExtension>().named("libs")
val javaToolchains = extensions.getByType<JavaToolchainService>()
val projectJar = tasks.named<Jar>("shadowJar").flatMap { it.archiveFile }
val extraPluginJars = rootProject.fileTree("buildSrc/plugin") {
    include("*.jar")
}
val minecraftVersions = listOf("1.21.4", "1.21.8", "1.21.10", "1.21.11", "26.1.2", "26.2")

tasks.named<RunServer>("runServer") {
    group = null
    enabled = false
}

tasks.withType<RunServer>().configureEach {
    legacyPluginLoading()
}

fun RunServer.configureServer(platform: String, version: String) {
    group = "run paper"
    description = "Run the $platform $version development server."
    displayName.set("$platform $version")
    minecraftVersion(version)
    runDirectory.set(rootProject.layout.projectDirectory.dir("run/${platform.lowercase()}/$version"))
    pluginJars.from(projectJar, extraPluginJars)
    javaLauncher.set(javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(if (version.startsWith("26.")) 25 else 21)
    })
    downloadPlugins {
        hangar("PlaceholderAPI", versions.findVersion("placeholderapi").get().requiredVersion)
        modrinth("packetevents", "2.13.0+spigot")
    }
    systemProperties["com.mojang.eula.agree"] = true
    jvmArgs(
        "-Dorg.bukkit.plugin.java.LibraryLoader.centralURL=https://maven.aliyun.com/repository/central",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "-Ddisable.watchdog=true",
        "-Xlog:redefine+class*=info",
        "-XX:+AllowEnhancedClassRedefinition"
    )
}

for (version in minecraftVersions) {
    tasks.register<RunServer>("runPaper_$version") {
        configureServer("Paper", version)
    }
    tasks.register<RunServer>("runFolia_$version") {
        configureServer("Folia", version)
        downloadsApiService.set(DownloadsAPIService.folia(project))
    }
}
