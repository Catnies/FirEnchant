plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("xyz.jpenilla.run-paper:xyz.jpenilla.run-paper.gradle.plugin:${libs.versions.runpaper.get()}")
}
