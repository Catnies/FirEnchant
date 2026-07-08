package top.catnies.firenchantkt.compatibility.provider.enchantment

import cc.polarastrum.aiyatsbus.core.AiyatsbusEnchantment
import cc.polarastrum.aiyatsbus.module.ingame.mechanics.EnchantingTableSupport
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.EnchantmentProvider

class AiyatsbusEnchantmentProvider(
    override val enabled: Boolean
): EnchantmentProvider {
    constructor(): this(Bukkit.getPluginManager().getPlugin("Aiyatsbus") != null)

    override fun prepareEnchant(bonus: Int, player: Player, item: ItemStack): List<Map<Enchantment, Int>> {
        val data = EnchantingTableSupport::class.java
            .getDeclaredMethod(
                "doPrepareEnchant",
                Player::class.java, ItemStack::class.java, Int::class.java
            ).apply { isAccessible = true }
            .invoke(
                EnchantingTableSupport::class.java.getField("INSTANCE").get(null),
                player, item, bonus
            ) as Map<Int, Pair<AiyatsbusEnchantment, Int>>

        val list = mutableListOf<Map<Enchantment, Int>>()
        data.forEach { key, value ->
            val map = mutableMapOf<Enchantment, Int>()
            map[value.first.enchantment] = value.second
            list.add(map)
        }
        return list
    }
}