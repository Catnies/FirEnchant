package top.catnies.firenchantkt.invui_v2.wrapper

import org.bukkit.entity.Player
import top.catnies.firenchantkt.gui.wrapper.InventoryPostEventWrapper
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent

class InventoryPostEventWrapperV1(
    override val player: Player,
    event: ItemPostUpdateEvent
) : InventoryPostEventWrapper {
    override val slot = event.slot
    override val isAdd = event.isAdd
    override val newItem = event.newItem
    override val previousItem = event.previousItem
}