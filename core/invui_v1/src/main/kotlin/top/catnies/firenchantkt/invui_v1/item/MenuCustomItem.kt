package top.catnies.firenchantkt.invui_v1.item

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

class MenuCustomItem (
    private val menuItemProvider: ItemProvider,
    val actionTemplates: List<ConfigActionTemplate>
): AbstractItem() {

    constructor(itemProvider: ItemProvider): this(itemProvider, emptyList())
    constructor(itemProvider: ItemProvider, actionTemplate: ConfigActionTemplate): this(itemProvider, listOf(actionTemplate))

    override fun getItemProvider(): ItemProvider = menuItemProvider

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        val args = mutableMapOf<String, Any?>()
        args["player"] = player
        args["clickType"] = clickType
        args["event"] = event
        actionTemplates.forEach {
            it.executeIfAllowed(args)
        }
    }

}