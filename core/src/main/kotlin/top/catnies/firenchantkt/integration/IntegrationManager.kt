package top.catnies.firenchantkt.integration

import org.bukkit.Bukkit
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.compatibility.auraskill.EnchantEventListener
import top.catnies.firenchantkt.compatibility.customcrops.CustomCropsLoader
import top.catnies.firenchantkt.compatibility.customfishing.CustomFishingLoader
import top.catnies.firenchantkt.language.MessageConstants.PLUGIN_COMPATIBILITY_HOOK_SUCCESS
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent

// 插件集成管理器
class IntegrationManager private constructor() {
    var plugin = FirEnchantPlugin.instance
    var logger = FirEnchantPlugin.instance.logger

    companion object {
        @JvmStatic
        val instance by lazy { IntegrationManager().apply {
            load()
        }}
    }

    val hasAuraSkills: Boolean = Bukkit.getPluginManager().getPlugin("AuraSkills") != null
    val hasCustomFishing: Boolean = Bukkit.getPluginManager().getPlugin("CustomFishing") != null
    val hasCustomCrops: Boolean = Bukkit.getPluginManager().getPlugin("CustomCrops") != null
    val hasEnchantmentSlots: Boolean = Bukkit.getPluginManager().getPlugin("EnchantmentSlots") != null
    val hasPlaceholderAPI: Boolean = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null

    private fun load() {
        // AuraSkills
        if (hasAuraSkills) {
            Bukkit.getPluginManager().registerEvents(EnchantEventListener(), plugin)
            sendPluginHookedMessage("AuraSkills")
        }
        // CustomFishing
        if (hasCustomFishing) {
            CustomFishingLoader.getInstance()
            sendPluginHookedMessage("CustomFishing")
        }
        // CustomCrops
        if (hasCustomCrops) {
            CustomCropsLoader.getInstance()
            sendPluginHookedMessage("CustomCrops")
        }
        // EnchantmentSlots
        if (hasEnchantmentSlots) {
            EnchantmentSlotsLoaderImpl.instance
            sendPluginHookedMessage("EnchantmentSlots")
        }
        // PlaceholderAPI
        if (hasPlaceholderAPI) {
            sendPluginHookedMessage("PlaceholderAPI")
        }
    }

    fun reload() {}

    // 发送插件关联启动消息
    fun sendPluginHookedMessage(name: String) {
        Bukkit.getConsoleSender().sendTranslatableComponent(
            PLUGIN_COMPATIBILITY_HOOK_SUCCESS,
            name
        )
    }
}