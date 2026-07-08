import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

//java {
//    sourceCompatibility = JavaVersion.VERSION_25
//    targetCompatibility = JavaVersion.VERSION_25
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(25)
//    }
//}
//
//kotlin {
//    jvmToolchain(25)
//    compilerOptions {
//        freeCompilerArgs.add("-Xjvm-default=all")
//    }
//}

plugins {
    alias(libs.plugins.pluginyml)
}

dependencies {
    implementation(project(":api"))
    implementation(project(":compatibility"))

    // For test
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    // Kotlin 标准库
    testImplementation(kotlin("stdlib"))
    // JUnit 5 依赖
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.test {
    useJUnitPlatform() // 关键：使用 JUnit 5 平台
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// 确保 plugin.yml 被正确处理
tasks.processResources {
    expand("version" to project.version)
}

paper {
    main = "top.catnies.firenchantkt.FirEnchantPlugin"
    bootstrapper = "top.catnies.firenchantkt.FirEnchantBootstrapper"
    loader = "top.catnies.firenchantkt.FirEnchantPluginLoader"
    hasOpenClassloader = false
    name = "FirEnchantKt"
    prefix = "FirEnchant"
    apiVersion = "1.21.4"
    foliaSupported = true
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP  or POSTWORLD
    authors = listOf("Catnies", "ChengZhiMeow")

    generateLibrariesJson = false

    serverDependencies {
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
            joinClasspath = true
        }

        register("Nexo") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("Oraxen") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("Itemsadder") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("MythicMobs") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("CraftEngine") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }


        register("EnchantmentSlots") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        // 声明插件依赖
        register("Aiyatsbus") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("MythicChanger") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("CustomFishing") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("CustomCrops") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }

        register("Aiyatsbus") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
    }

}