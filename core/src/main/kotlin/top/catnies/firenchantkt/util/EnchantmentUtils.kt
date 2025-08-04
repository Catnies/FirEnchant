package top.catnies.firenchantkt.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import java.util.concurrent.ConcurrentHashMap

object EnchantmentUtils {

    // 适配魔咒缓存
    val ENCHANT_CACHE: MutableMap<Material, Set<Enchantment>> = ConcurrentHashMap()

    // 检查物品的所有可应用魔咒
    fun getApplicableEnchants(item: ItemStack): Set<Enchantment> {
        val enchantmentsCache = ENCHANT_CACHE[item.type]
        if (enchantmentsCache != null) return enchantmentsCache

        val tableEnchantmentList = NMSHandlerHolder.getNMSHandler().getEnchantmentTableEnchantmentList(Bukkit.getWorlds().first())
        if (item.type == Material.BOOK) {
            ENCHANT_CACHE[item.type] = tableEnchantmentList
            return tableEnchantmentList
        }

        val enchantments = tableEnchantmentList.filter { it.canEnchantItem(item) }.toSet()
        ENCHANT_CACHE[item.type] = enchantments
        return enchantments
    }

}