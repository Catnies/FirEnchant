package top.catnies.firenchantkt.config

class ConfigManager private constructor() {

    companion object {
        val instance by lazy { ConfigManager() }
    }

    // 配置文件
    private val configs = listOf(
        SettingsConfig.instance,
        AnvilConfig.instance,
        ExtractSoulSetting.instance
    )

    // 重载所有配置
    fun reload() {
        configs.forEach { it.reload() }
    }
}