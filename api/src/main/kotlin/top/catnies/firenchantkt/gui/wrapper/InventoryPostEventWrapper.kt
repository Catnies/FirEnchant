package top.catnies.firenchantkt.gui.wrapper

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface InventoryPostEventWrapper {
    val player: Player
    val slot: Int
    val isAdd: Boolean
    val newItem: ItemStack?
    val previousItem: ItemStack?
}