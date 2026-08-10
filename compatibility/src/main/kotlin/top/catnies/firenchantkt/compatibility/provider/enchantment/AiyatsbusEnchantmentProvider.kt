package top.catnies.firenchantkt.compatibility.provider.enchantment

import cc.polarastrum.aiyatsbus.core.etsAvailable
import cc.polarastrum.aiyatsbus.core.data.CheckType
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.EnchantmentProvider

class AiyatsbusEnchantmentProvider(
    override val enabled: Boolean
): EnchantmentProvider {
    constructor(): this(Bukkit.getPluginManager().getPlugin("Aiyatsbus") != null)

    override fun getEnchantList(item: ItemStack): Set<Enchantment> = item.etsAvailable(CheckType.ATTAIN, null).map { it.enchantment }.toSet()
}