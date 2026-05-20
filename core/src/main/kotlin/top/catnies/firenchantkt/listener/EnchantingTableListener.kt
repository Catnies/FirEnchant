package top.catnies.firenchantkt.listener

import com.github.iamnot23.gui_v2.SimpleMenu
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import top.catnies.firenchantkt.config.EnchantingTableConfig
import top.catnies.firenchantkt.gui.FirEnchantingTableMenu
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import top.catnies.firenchantkt.util.VersionHelper

class EnchantingTableListener : Listener {

    val config = EnchantingTableConfig.instance

    // 右键附魔台替换成打开插件附魔台;
    // 创造模式+shift右键打开原版附魔台;
    @EventHandler(ignoreCancelled = true)
    fun onEnchantingTableClick(event: PlayerInteractEvent) {
        if (!config.REPLACE_VANILLA_ENCHANTMENT_TABLE) return               // 检查功能是否打开
        if (event.action != Action.RIGHT_CLICK_BLOCK) return                // 不是打开则不管
        if (event.clickedBlock?.type != Material.ENCHANTING_TABLE) return   // 不是附魔台不管
        if (event.player.gameMode == GameMode.CREATIVE && event.player.isSneaking) return // 创造模式+shift右键打开不管

        event.isCancelled = true
        val nmsHandler = NMSHandlerHolder.getNMSHandler()
        val bookShelves = nmsHandler.getEnchantmentTableBookShelf(event.clickedBlock!!.location)

        if (VersionHelper.isOrAbove26_1()) {
            SimpleMenu(player = event.player).open()
        } else {
            FirEnchantingTableMenu(event.player, bookShelves).openMenu(emptyMap(), true)
        }
    }

}