package top.catnies.firenchantkt.util

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object PlayerUtils {

    // 给予物品或掉落在地上
    fun Player.giveOrDrop(vararg items: ItemStack) {
        this.giveOrDropList(items.toList())
    }

    fun Player.giveOrDropList(items: List<ItemStack>) {
        val drop = this.inventory.addItem(*items.toTypedArray())
        if (drop.isNotEmpty()) {
            drop.values.forEach { this.world.dropItem(this.location, it) }
        }
    }

}