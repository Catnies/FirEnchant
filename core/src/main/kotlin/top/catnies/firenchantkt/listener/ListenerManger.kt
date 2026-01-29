package top.catnies.firenchantkt.listener

import org.bukkit.Bukkit
import top.catnies.firenchantkt.FirEnchantPlugin

class ListenerManger private constructor() {
    val plugin get() = FirEnchantPlugin.instance
    val logger get() = plugin.logger

    companion object {
        val instance: ListenerManger by lazy { ListenerManger().apply { load() } }
    }

    // 初始化
    private fun load() {
        Bukkit.getPluginManager().registerEvents(RecipeListener(), plugin)
        Bukkit.getPluginManager().registerEvents(CacheListener(), plugin)
        Bukkit.getPluginManager().registerEvents(BrokenItemListener(), plugin)
        Bukkit.getPluginManager().registerEvents(EnchantingTableListener(), plugin)
        Bukkit.getPluginManager().registerEvents(LootOverrideListener(), plugin)
    }

}