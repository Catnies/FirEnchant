package top.catnies.firenchantkt.listener

import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.database.FirCacheManager
import top.catnies.firenchantkt.database.FirConnectionManager

class CacheListener : Listener {

    val plugin = FirEnchantPlugin.instance

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.launch {
            // 铁砧附魔历史记录
            val anvilLogCache = FirConnectionManager.getInstance().enchantLogData.getByPlayerRecent(player.uniqueId, 20)
            anvilLogCache.forEach {
                FirCacheManager.getInstance().addEnchantLog(it)
            }
            // 附魔台历史记录
            val tableLogCache =
                FirConnectionManager.getInstance().enchantingHistoryData.getByPlayerRecent(player.uniqueId, 20)
            tableLogCache.forEach {
                FirCacheManager.getInstance().addEnchantingHistory(it)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.launch {
            FirCacheManager.getInstance().clearPlayerCache(player.uniqueId)
        }
    }
}