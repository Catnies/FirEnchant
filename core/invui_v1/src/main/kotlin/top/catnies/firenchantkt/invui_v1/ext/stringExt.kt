package top.catnies.firenchantkt.invui_v1.ext

import org.bukkit.entity.Player
import top.catnies.firenchantkt.util.MessageUtils.renderToComponent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper


// 将字符串包装成菜单标题
fun String.wrapTitle(player: Player?): AdventureComponentWrapper {
    val component = this.renderToComponent(player)
    return AdventureComponentWrapper(component)
}