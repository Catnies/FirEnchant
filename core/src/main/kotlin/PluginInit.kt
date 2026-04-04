import org.bukkit.Bukkit
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.language.MessageConstants.PLUGIN_COMPATIBILITY_HOOK_SUCCESS
import top.catnies.firenchantkt.lazyinit.CraftEngineLoadListener
import top.catnies.firenchantkt.lazyinit.ItemsAdderLoadListener
import top.catnies.firenchantkt.lazyinit.NexoLoadListener
import top.catnies.firenchantkt.lazyinit.OraxenLoadListener
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent

object PluginInit {

    // 延迟初始化注册表实现
    fun registerLateInitListener(plugin: FirEnchantPlugin) {
        when {
            // CraftEngine
            Bukkit.getPluginManager().getPlugin("CraftEngine") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(CraftEngineLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "CraftEngine")
                }
            }
            // Nexo
            Bukkit.getPluginManager().getPlugin("Nexo") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(NexoLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "Nexo")
                }
            }
            // Oraxen
            Bukkit.getPluginManager().getPlugin("Oraxen") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(OraxenLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "Oraxen")
                }
            }
            // ItemsAdder
            Bukkit.getPluginManager().getPlugin("ItemsAdder") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(ItemsAdderLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "ItemsAdder")
                }
            }
            else -> {
                plugin.initRegistry()
            }
        }
    }

}