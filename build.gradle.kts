import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import xyz.jpenilla.runpaper.task.RunServer
import java.util.*

// 插件
plugins {
    alias(libs.plugins.kotlin.jvm) // Kotlin
    alias(libs.plugins.shadow) // Shadow
    alias(libs.plugins.runpaper) // Run Paper
    alias(libs.plugins.paperweight) apply false // Paper Weight
}

val v26_1Runtime by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, libs.versions.java.get().toInt())
    }
}

// 依赖所有子模块
dependencies {
    implementation(project(":api"))
    implementation(project(":compatibility"))
    implementation(project(":core"))
    implementation(project(":core:invui_v1"))
    implementation(project(":core:invui_v2"))
    implementation(project(":nms:v1_21_R3"))
    implementation(project(":nms:v1_21_R4"))
    implementation(project(":nms:v1_21_R5"))
    implementation(project(":nms:v1_21_R6"))
    implementation(project(":nms:v1_21_R7"))
    add(v26_1Runtime.name, project(path = ":nms:v26_1", configuration = "runtimeElements")) {
        isTransitive = false
    }
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
        // Koin
        implementation("io.insert-koin:koin-core:4.1.1")
        implementation("io.insert-koin:koin-annotations:2.3.1")
        // 数据库
        compileOnly(rootProject.libs.bundles.mysql) // Mysql Bundles
        // 依赖库
        compileOnly(rootProject.libs.bundles.rtag) // RTag Bundles
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
        dependsOn(":nms:v1_21_R3:reobfJar")
        dependsOn(":nms:v1_21_R4:reobfJar")
        dependsOn(":nms:v1_21_R5:reobfJar")
        dependsOn(":nms:v1_21_R6:reobfJar")
        dependsOn(":nms:v1_21_R7:reobfJar")
        dependsOn(":nms:v26_1:jar")
        from({ v26_1Runtime.map { zipTree(it) } })
        mergeServiceFiles()

        manifest.attributes("paperweight-mappings-namespace" to "mojang")

        archiveFileName.set("${project.name}-${project.version}.jar")
        destinationDirectory.set(file("$rootDir/target"))

        // 排除kotlin, 不然跟aiya冲突
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8"))

            exclude("META-INF/kotlin-stdlib.kotlin_module")
            exclude("META-INF/kotlin-stdlib-common.kotlin_module")
            exclude("kotlin/**")
            exclude("kotlinx/**")
        }
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
        minecraftVersion("26.1.2")
    }
}

registerPaperTask("1.21.10","1_21_10", javaVersion = 21)
registerPaperTask("1.21.11","1_21_11", javaVersion = 21)
registerPaperTask("26.1.2","26_1_2", javaVersion = 25)

fun registerPaperTask(
    version: String,
    taskName: String = version,
    javaVersion: Int,
    serverJarFile: File? = null
) {
    fun RunServer.applyCommonConfig() {
        description = "run dev server"
        downloadPlugins {
            hangar("PlaceholderAPI", libs.versions.placeholderapi.get())
            modrinth("CraftEngine", libs.versions.craftengine.get())
            modrinth("packetevents", "2.12.1+spigot")
        }
        minecraftVersion(version)
        serverJarFile?.let { serverJar(it) }
        pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
        javaLauncher = javaToolchains.launcherFor {
            vendor = JvmVendorSpec.JETBRAINS
            languageVersion = JavaLanguageVersion.of(javaVersion)
        }
        systemProperties["com.mojang.eula.agree"] = true
        jvmArgs(
//            "-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false", // 关闭旧版格式的警告
            "-Dorg.bukkit.plugin.java.LibraryLoader.centralURL=https://maven.aliyun.com/repository/central",
            "-Dsun.stdout.encoding=UTF-8",
            "-Dsun.stderr.encoding=UTF-8",
            "-Ddisable.watchdog=true",
            "-Xlog:redefine+class*=info",
            "-XX:+AllowEnhancedClassRedefinition"
        )

    }

    tasks.register<RunServer>(taskName) {
        description = "run dev server"
        group = "run dev server"
        runDirectory = rootProject.layout.projectDirectory.dir("runPaper/$taskName")
        applyCommonConfig()
    }
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

        versionProps.setProperty("kotlin.version", libs.versions.kotlin.get())
        versionProps.setProperty("kotlinx-coroutines.version", libs.versions.kotlinx.coroutines.get())
        versionProps.setProperty("ormlite.version", libs.versions.ormlite.get())
        versionProps.setProperty("hikaricp.version", libs.versions.hikaricp.get())
        versionProps.setProperty("rtag.version", libs.versions.rtag.get())
        versionProps.setProperty("invui.version", libs.versions.invui.get())
        versionProps.setProperty("invui2.version", libs.versions.invui2.get())

        val outputFile = outputDir.get().asFile.resolve("dependency-version.properties")
        outputFile.parentFile.mkdirs()
        outputFile.outputStream().use { output ->
            versionProps.store(output, "Generated version information")
        }
    }
}
