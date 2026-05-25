package top.catnies.firenchantkt.invui_v1.item

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem

class MenuPageItem(
    var forward: Boolean = true,
    private val actionTemplates: List<ConfigActionTemplate>,
    private val menuItemProvider: ItemProvider
): ControlItem<PagedGui<*>>() {

    override fun getItemProvider(gui: PagedGui<*>?) = menuItemProvider

    override fun handleClick(
        clickType: ClickType,
        player: Player,
        event: InventoryClickEvent
    ) {
        if (menuItemProvider.get() == ItemStack.empty()) return // 无显示时不做任何操作

        if (clickType == ClickType.LEFT) {
            if (forward) gui.goForward() else gui.goBack()

            val args = mutableMapOf<String, Any?>()
            args["player"] = player
            args["clickType"] = clickType
            args["event"] = event
            actionTemplates.forEach {
                it.executeIfAllowed(args)
            }
        }
    }
}