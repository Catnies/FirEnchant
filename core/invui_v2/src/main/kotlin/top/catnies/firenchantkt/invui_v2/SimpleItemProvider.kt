package top.catnies.firenchantkt.invui_v2

import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import java.util.Locale

/**
 * 对invui的包装
 * 函数式接口
 */
fun interface SimpleItemProvider: ItemProvider {
    override fun get(locale: Locale): ItemStack = get()

    override fun get(): ItemStack
}