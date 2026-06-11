package top.catnies.firenchantkt.integration

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface EnchantmentProvider {
    val enabled: Boolean

    fun prepareEnchant(bonus: Int, player: Player, item: ItemStack): List<Map<Enchantment, Int>>
}
