package top.catnies.firenchantkt.invui_v2.item

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.invui_v2.SimpleItemProvider
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.item.AbstractItem
import xyz.xenondevs.invui.item.ItemProvider

class MenuCustomItem(
    val actions: List<ConfigActionTemplate>,
    private val menuItemProvider: SimpleItemProvider,
) : AbstractItem() {

    constructor(itemProvider: SimpleItemProvider) : this(emptyList(), itemProvider)
    constructor(actionTemplate: ConfigActionTemplate, itemProvider: SimpleItemProvider) : this(
        listOf(actionTemplate),
        itemProvider
    )

    override fun getItemProvider(viewer: Player): ItemProvider = menuItemProvider

    override fun handleClick(clickType: ClickType, player: Player, click: Click) {
        val args = mutableMapOf<String, Any?>()
        args["player"] = player
        args["clickType"] = clickType
        args["event"] = null // TODO event 没有了
        actions.forEach {
            it.executeIfAllowed(args)
        }
    }

}