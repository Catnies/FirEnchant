import java.util.Properties

// 插件
plugins {
    alias(libs.plugins.kotlin.jvm) // Kotlin
    alias(libs.plugins.shadow) // Shadow
    alias(libs.plugins.runpaper) // Run Paper
    alias(libs.plugins.paperweight) apply false // Paper Weight
}

// 依赖所有子模块
dependencies {
    implementation(project(":core"))
    implementation(project(":api"))
    implementation(project(":compatibility"))
    implementation(project(path = ":nms:v1_21_R1"))
    implementation(project(path = ":nms:v1_21_R2"))
    implementation(project(path = ":nms:v1_21_R3"))
    implementation(project(path = ":nms:v1_21_R4"))
    implementation(project(path = ":nms:v1_21_R5"))
}


// 需要将一些部分统一进行配置，可以直接在根项目使用`subprojects`或`allprojects`编写内容
allprojects {
    // 应用插件到子项目
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.gradleup.shadow")

    // 属性
    group = "top.catnies"
    version = rootProject.libs.versions.plugin.version.get()
    java.sourceCompatibility = JavaVersion.VERSION_21
    java.targetCompatibility = JavaVersion.VERSION_21

    // Kotlin 配置
    kotlin {
        jvmToolchain(21)
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io") // RTag
        maven("https://repo.xenondevs.xyz/releases") // InvUI
        maven("https://repo.papermc.io/repository/maven-public/") // Paper
        maven("https://maven.devs.beer/") // ItemsAdder
        maven("https://repo.nexomc.com/releases") // Nexo
        maven("https://repo.oraxen.com/releases") // Oraxen
        maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs
        maven("https://repo.momirealms.net/releases/") // CustomCrops, CustomFishing, CraftEngine
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo-eo.catnies.top/mhdf") // ChengZhiMeow
        maven("https://repo.nightexpressdev.com/releases") // CoinsEngine
    }

    dependencies {
        // 开发
        compileOnly(rootProject.libs.paper.api) // PAPER
        compileOnly(rootProject.libs.bundles.kotlin) // Kotlin Bundles
        compileOnly(rootProject.libs.lombok) // Lombok
        annotationProcessor(rootProject.libs.lombok) // Lombok
        // 数据库
        compileOnly(rootProject.libs.bundles.mysql) // Mysql Bundles
        // 依赖库
        compileOnly(rootProject.libs.bundles.rtag) // RTag Bundles
        compileOnly(rootProject.libs.bundles.invui) { // InvUI
            exclude("org.jetbrains.kotlin", "*")
            exclude("org.jetbrains.kotlinx", "*")
        }
        implementation(rootProject.libs.mhdf.scheduler) // Scheduler

        // 兼容
        compileOnly(rootProject.libs.placeholderapi) // PlaceholderAPI
    }
}


// 任务配置
tasks {
    clean {
        delete("$rootDir/target")
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        dependsOn(":nms:v1_21_R1:reobfJar")
        dependsOn(":nms:v1_21_R2:reobfJar")
        dependsOn(":nms:v1_21_R3:reobfJar")
        dependsOn(":nms:v1_21_R4:reobfJar")
        dependsOn(":nms:v1_21_R5:reobfJar")
        dependsOn(":nms:v1_21_R6:reobfJar")
        mergeServiceFiles()

        manifest.attributes("paperweight-mappings-namespace" to "mojang")

        archiveFileName.set("${project.name}-${project.version}.jar")
        destinationDirectory.set(file("$rootDir/target"))
    }

    // 展开 gradle.properties 到 resources
    processResources {
        dependsOn(generateVersionProperties)
        from(generateVersionProperties.map { it.outputs })
    }

    // 调试测试
    runServer {
        dependsOn(shadowJar)
        dependsOn(jar)
        minecraftVersion("1.21.8")
        downloadPlugins {
            hangar("PlaceholderAPI", "2.11.6")
            modrinth("rtag", "1.5.13")
        }
    }

}

// 调试测试环境
tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-Dsun.stdout.encoding=UTF-8")
    jvmArgs("-Dsun.stderr.encoding=UTF-8")
    jvmArgs("-Ddisable.watchdog=true")
    jvmArgs("-Xlog:redefine+class*=info")
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}

// 生成版本信息资源文件
val generateVersionProperties by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/resources")
    outputs.dir(outputDir)

    doLast {
        val versionProps = Properties()

        // 从 version catalog 获取版本信息
        versionProps.setProperty("project.version", version.toString())
        versionProps.setProperty("repository.central", "https://maven.aliyun.com/repository/public")

        versionProps.setProperty("kotlin.version", libs.versions.kotlin.stdlib.get())
        versionProps.setProperty("kotlinx-coroutines.version", libs.versions.kotlinx.coroutines.get())
        versionProps.setProperty("ormlite.version", libs.versions.ormlite.get())
        versionProps.setProperty("hikaricp.version", libs.versions.hikaricp.get())
        versionProps.setProperty("rtag.version", libs.versions.rtag.get())
        versionProps.setProperty("invui.version", libs.versions.invui.get())

        val outputFile = outputDir.get().asFile.resolve("dependency-version.properties")
        outputFile.parentFile.mkdirs()
        outputFile.outputStream().use { output ->
            versionProps.store(output, "Generated version information")
        }
    }
}