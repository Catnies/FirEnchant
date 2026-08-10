package top.catnies.firenchantkt.integration

import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

interface EnchantmentProvider {
    val enabled: Boolean

    fun getEnchantList(item: ItemStack): Set<Enchantment>
}
