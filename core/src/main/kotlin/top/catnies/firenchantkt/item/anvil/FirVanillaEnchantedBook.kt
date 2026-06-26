package top.catnies.firenchantkt.item.anvil

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.config.AnvilConfig
import top.catnies.firenchantkt.context.AnvilContext

// 监听原版的附魔书应用事件, 然后根据配置取消.
class FirVanillaEnchantedBook: VanillaEnchantedBook {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = AnvilConfig.instance
    }

    override fun matches(itemStack: ItemStack): Boolean {
        // 功能开关
        if (config.VEB_DENY_USE) return false
        // 无效物品
        if (itemStack.type != Material.ENCHANTED_BOOK) return false
        return true
    }

    override fun onPrepareResult(
        event: PrepareResultEvent,
        context: AnvilContext
    ) {
        // 忽略创造模式
        if (context.viewer.gameMode == GameMode.CREATIVE) return
        // 取消结果
        event.result = null
    }
}