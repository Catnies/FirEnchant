dependencies {
    implementation(project(":api"))
    compileOnly(rootProject.libs.bundles.itemproviders) // 物品库

    // 拓展功能
    compileOnly(rootProject.libs.auraskills) // AuraSkills
    compileOnly(rootProject.libs.customcrops) // CustomCrops
    compileOnly(rootProject.libs.customfishing) // CustomFishing
    compileOnly(files("libs/EnchantmentSlots-4.6.10.jar")) // EnchantmentSlots
    compileOnly(files("libs/Aiyatsbus-1.3.0-dev-9.jar")) // Aiyatsbus
}