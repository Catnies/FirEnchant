package top.catnies.firenchantkt.listener

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.LootGenerateEvent
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.config.LootOverrideConfig

class LootOverrideListener: Listener {

    @EventHandler
    fun onEnchantmentBookLoot(event: LootGenerateEvent) {
        if (!LootOverrideConfig.instance.ENABLE) return
        val loot = event.loot

        for (i in loot.indices) {
            val item = loot[i]
            if (item.type != Material.ENCHANTED_BOOK) continue

            val stored = item.getData(DataComponentTypes.STORED_ENCHANTMENTS) ?: continue
            val entries = stored.enchantments().entries
            if (entries.isEmpty()) continue

            val (enchantKey, level) = entries.random().let { it.key to it.value }
            val failure = LootOverrideConfig.instance.FAILURE_RANGE.random()

            val newItem = FirEnchantAPI.getSettingsByData(enchantKey.key(), level, failure)
                ?.toItemStack()
                ?: continue

            // 替换回战利品列表
            loot[i] = newItem
        }
    }

}