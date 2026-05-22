repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(rootProject.libs.bundles.invui) { // InvUI
        exclude("org.jetbrains.kotlin", "*")
        exclude("org.jetbrains.kotlinx", "*")
    }
}