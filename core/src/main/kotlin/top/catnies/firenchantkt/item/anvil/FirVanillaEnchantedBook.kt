package top.catnies.firenchantkt.item.anvil

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.config.AnvilConfig
import top.catnies.firenchantkt.context.AnvilContext
import top.catnies.firenchantkt.util.ItemUtils.addRepairCost
import top.catnies.firenchantkt.util.TaskUtils

// 监听原版的附魔书应用事件, 然后根据配置取消.
class FirVanillaEnchantedBook: VanillaEnchantedBook {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = AnvilConfig.instance
    }

    override fun matches(itemStack: ItemStack): Boolean {
        if (!config.VEB_DENY_USE || FirEnchantAPI.getSettingsByItemStack(itemStack) != null) { // 如果启用原版附魔书, 则禁用此处理器, 转为原版逻辑
            return false
        }
        return itemStack.type == Material.ENCHANTED_BOOK // 类型不对是无效物品
    }

    override fun onPrepareResult(
        event: PrepareResultEvent,
        context: AnvilContext
    ) {
        // 忽略创造模式
        if (context.viewer.gameMode == GameMode.CREATIVE) return

        // 进入处理器逻辑
        event.result = null
    }
}