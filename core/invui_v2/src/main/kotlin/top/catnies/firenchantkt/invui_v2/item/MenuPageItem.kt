package top.catnies.firenchantkt.invui_v2.item

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.invui_v2.SimpleItemProvider
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem
import xyz.xenondevs.invui.item.ItemProvider

class MenuPageItem(
    var forward: Boolean = true,
    private val actionTemplates: List<ConfigActionTemplate>,
    private val menuItemProvider: SimpleItemProvider
): AbstractPagedGuiBoundItem() {

    override fun getItemProvider(viewer: Player): ItemProvider = menuItemProvider

    override fun handleClick(
        clickType: ClickType,
        player: Player,
        click: Click
    ) {
        if (menuItemProvider.get() == ItemStack.empty()) return // 无显示时不做任何操作

        if (clickType == ClickType.LEFT) {
            if (forward) gui.page++ else gui.page--

            // TODO event 没有了
            actionTemplates.forEach {
                it.executeIfAllowed(mapOf(
                    "player" to player,
                    "clickType" to clickType,
                ))
            }
        }
    }

}