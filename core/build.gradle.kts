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