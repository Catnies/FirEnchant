rootProject.name = "FirEnchantKt"

include(":api")
include(":compatibility")
include(":core")
include("nms:v1_21_R1")
include("nms:v1_21_R2")
include("nms:v1_21_R3")
include("nms:v1_21_R4")
include("nms:v1_21_R5")
include("nms:v1_21_R6")
include("nms:v1_21_R7")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/") // Paper Weight
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
//    repositories {
//        mavenCentral()
//        gradlePluginPortal()
//        maven("https://jitpack.io") // RTag
//        maven("https://repo.xenondevs.xyz/releases") // InvUI
//        maven("https://repo.papermc.io/repository/maven-public/") // Paper
//        maven("https://maven.devs.beer/") // ItemsAdder
//        maven("https://repo.nexomc.com/releases") // Nexo
//        maven("https://repo.oraxen.com/releases") // Oraxen
//        maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs
//        maven("https://repo.momirealms.net/releases/") // CustomCrops, CustomFishing, CraftEngine
//        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
//        maven("https://maven.chengzhimeow.cn/releases") // ChengZhiMeow
//        maven("https://repo.nightexpressdev.com/releases") // CoinsEngine
//    }

    versionCatalogs {
        create("library") { from(files("gradle/libs.versions.toml")) }
    }
}

