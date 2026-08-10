package top.catnies.firenchantkt.integration

import org.bukkit.Bukkit
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.compatibility.auraskill.AuraSkillLoader
import top.catnies.firenchantkt.compatibility.customcrops.CustomCropsLoader
import top.catnies.firenchantkt.compatibility.customfishing.CustomFishingLoader
import top.catnies.firenchantkt.compatibility.enchantmentslots.EnchantmentSlotsLoader
import top.catnies.firenchantkt.config.AnvilConfig
import top.catnies.firenchantkt.language.MessageConstants.PLUGIN_COMPATIBILITY_HOOK_SUCCESS
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent

// 插件集成管理器
class IntegrationManager private constructor() {
    var plugin = FirEnchantPlugin.instance
    var logger = FirEnchantPlugin.instance.logger

    companion object {
        @JvmStatic
        val instance by lazy {
            IntegrationManager().apply {
                load()
            }
        }

        @JvmStatic
        val hookPlugins = mutableMapOf<String, HookPluginLoader>()
    }

    val hasAuraSkills: Boolean = Bukkit.getPluginManager().getPlugin("AuraSkills") != null
    val hasCustomFishing: Boolean = Bukkit.getPluginManager().getPlugin("CustomFishing") != null
    val hasCustomCrops: Boolean = Bukkit.getPluginManager().getPlugin("CustomCrops") != null
    val hasEnchantmentSlots: Boolean = Bukkit.getPluginManager().getPlugin("EnchantmentSlots") != null
    val hasPlaceholderAPI: Boolean = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null

    private fun load() {
        // AuraSkills
        if (hasAuraSkills) {
            hookPlugins["AuraSkills"] = AuraSkillLoader(plugin).also { it.load() }
            sendPluginHookedMessage("AuraSkills")
        }
        // CustomFishing
        if (hasCustomFishing) {
            hookPlugins["CustomFishing"] = CustomFishingLoader(plugin).also { it.load() }
            sendPluginHookedMessage("CustomFishing")
        }
        // CustomCrops
        if (hasCustomCrops) {
            hookPlugins["CustomCrops"] = CustomCropsLoader(plugin).also { it.load() }
            sendPluginHookedMessage("CustomCrops")
        }
        // EnchantmentSlots
        if (hasEnchantmentSlots) {
            hookPlugins["EnchantmentSlots"] = EnchantmentSlotsLoader(
                plugin,
                { AnvilConfig.instance.SLOT_RUNE_ENABLE },
                { AnvilConfig.instance.SLOT_RUNE_EXP },
                { AnvilConfig.instance.SLOT_RUNE_ITEM?.itemProvider },
                { AnvilConfig.instance.SLOT_RUNE_ITEM?.id }
            ).also { it.load() }
            sendPluginHookedMessage("EnchantmentSlots")
        }
        // PlaceholderAPI
        if (hasPlaceholderAPI) {
            sendPluginHookedMessage("PlaceholderAPI")
        }
    }

    fun reload() {
        hookPlugins.forEach { (_, loader) -> loader.reload() }
    }

    // 发送插件关联启动消息
    fun sendPluginHookedMessage(name: String) {
        Bukkit.getConsoleSender().sendTranslatableComponent(
            PLUGIN_COMPATIBILITY_HOOK_SUCCESS,
            name
        )
    }
}