package top.catnies.firenchantkt.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.compatibility.aiyatsbus.getAvailableEnchantments
import top.catnies.firenchantkt.config.SettingsConfig
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import java.util.concurrent.ConcurrentHashMap

object EnchantmentUtils {

    // 适配魔咒缓存
    val ENCHANT_CACHE: MutableMap<Material, Set<Enchantment>> = ConcurrentHashMap()

    // 检查物品的所有可应用魔咒
    fun getApplicableEnchants(item: ItemStack): Set<Enchantment> {
        val enchantmentsCache = ENCHANT_CACHE[item.type]
        if (enchantmentsCache != null) return enchantmentsCache

        // 简单粗暴的方法
        val tableEnchantmentList = if (Bukkit.getServer().pluginManager.isPluginEnabled("Aiyatsbus")) {
            // 使用aiya的api获取物品可用附魔列表
            getAvailableEnchantments(item).toSet()
        } else {
            NMSHandlerHolder.getNMSHandler()
                .getEnchantmentTableEnchantmentList(
                    Bukkit.getWorlds().first(),
                    SettingsConfig.instance.REGISTRY
                )
        }

        if (item.type == Material.BOOK) {
            ENCHANT_CACHE[item.type] = tableEnchantmentList
            return tableEnchantmentList
        }

        val enchantments = tableEnchantmentList.filter { it.canEnchantItem(item) }.toSet()
        ENCHANT_CACHE[item.type] = enchantments
        return enchantments
    }

}