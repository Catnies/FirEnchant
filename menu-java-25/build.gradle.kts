
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {

}

//relocate("xyz.xenondevs.invui", "xyz.xenondevs.invui2") {
//    include("xyz.xenondevs.invui:${libs.versions.invui2.get()}")
//    include("xyz.xenondevs.invui-kotlin:${libs.versions.invui2.get()}")
//}