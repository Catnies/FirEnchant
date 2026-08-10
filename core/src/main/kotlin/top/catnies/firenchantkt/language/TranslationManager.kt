package top.catnies.firenchantkt.language

import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.configuration.file.YamlConfiguration
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.util.ResourceCopyUtils
import java.util.*

class TranslationManager private constructor() {
    val plugin get() = FirEnchantPlugin.instance
    val logger get() = plugin.logger

    val languagesDirectory = plugin.dataFolder.resolve("languages")
    var translator = MessageTranslator

    companion object {
        val instance: TranslationManager by lazy { TranslationManager().apply { load() } }
    }

    // 初始化
    private fun load() {
        ensureLanguageFilesExist()
        loadFromFileSystem()
    }

    fun reload() {
        ensureLanguageFilesExist()
        translator.clearTranslations()
        loadFromFileSystem()
    }

    // 确保语言文件存在，如果不存在，就从资源文件里复制
    private fun ensureLanguageFilesExist() {
        if (!languagesDirectory.exists() || languagesDirectory.listFiles()?.isEmpty() == true) {
            ResourceCopyUtils.copyFolder(plugin, "languages", plugin.dataFolder)
        }
    }

    // 将系统语言文件夹下的内容加载到TranslationStore中.
    private fun loadFromFileSystem() {
        languagesDirectory.walkTopDown()
            .maxDepth(1)
            .filter { it.isFile && it.extension == "yml" }
            .forEach { file ->
                val yaml = YamlConfiguration.loadConfiguration(file)
                val lang = file.nameWithoutExtension
                val langVersion = yaml.getInt("lang-version")
                val locale = Locale.forLanguageTag(lang)

                yaml.getKeys(true)
                    .filterNot { it.equals("lang-version") }
                    .filterNot { yaml.isConfigurationSection(it) }
                    .forEach { key ->
                        val message = yaml.getString(key)!!
                        translator.addTranslation(key, locale, message)
                    }

                logger.info("Loaded language $lang ($langVersion)")
            }

        // 注册到全局翻译器
        GlobalTranslator.translator().addSource(translator)
    }
}