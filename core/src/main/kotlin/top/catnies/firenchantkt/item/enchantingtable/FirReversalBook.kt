package top.catnies.firenchantkt.item.enchantingtable

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.api.event.enchantingtable.ReversalBookUseEvent
import top.catnies.firenchantkt.config.EnchantingTableConfig
import top.catnies.firenchantkt.context.EnchantingTableContext
import top.catnies.firenchantkt.database.FirCacheManager
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.integration.ItemProvider
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import java.util.*
import java.util.concurrent.TimeUnit

class FirReversalBook: ReversalBook {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = EnchantingTableConfig.instance
    }

    var isEnabled: Boolean = false
    var itemProvider: ItemProvider? = null
    var itemID: String? = null
    var actions: List<ConfigActionTemplate> = emptyList()

    // TODO(不知道为啥, SHIFT + LC 放入物品会触发多次, 导致扣除多个物品; 但是一打断点就无法复现, 堪称奇葩.推测是INVUI的问题, 暂时先挂个防抖缓存)
    val cooldownCache: Cache<UUID, Long> = CacheBuilder.newBuilder().expireAfterWrite(500, TimeUnit.MILLISECONDS).build()

    init {
        load()
    }

    // 检查配置合法性
    override fun load() {
        isEnabled = config.REVERSAL_BOOK_ENABLE
        if (isEnabled) {
            actions = config.REVERSAL_BOOK_ACTIONS
            itemProvider = FirEnchantAPI.itemProviderRegistry().getItemProvider(config.REVERSAL_BOOK_ITEM_PROVIDER!!)
            itemID = config.REVERSAL_BOOK_ITEM_ID
        }
    }

    override fun reload() = load()

    override fun matches(itemStack: ItemStack): Boolean {
        if (!isEnabled) return false
        val itemProvider = FirEnchantAPI.itemProviderRegistry().getItemProvider(config.REVERSAL_BOOK_ITEM_PROVIDER!!)
        return itemProvider!!.getIdByItem(itemStack) == itemID
    }

    override fun onPreInput(itemStack: ItemStack, event: ItemPreUpdateEvent, context: EnchantingTableContext) {
        val player = context.player
        if (cooldownCache.getIfPresent(player.uniqueId) != null) {
            event.isCancelled = true
            return
        }
        cooldownCache.put(player.uniqueId, System.currentTimeMillis())

        // 检查是否有上次附魔的种子
        val lastEnchantingSeed = FirCacheManager.getInstance().getLastEnchantingTableSeed(player.uniqueId)
        if (lastEnchantingSeed == -1) {
            event.isCancelled = true
            return
        }

        // 触发事件
        val useEvent = ReversalBookUseEvent(player, lastEnchantingSeed)
        Bukkit.getPluginManager().callEvent(useEvent)
        if (useEvent.isCancelled) {
            event.isCancelled = true
            return
        }

        // 回滚上次附魔的种子
        player.enchantmentSeed = useEvent.backSeed
        context.menu.clearInputInventory()
        actions.forEach { action ->
            action.executeIfAllowed(mapOf("player" to player))
        }
    }

    override fun onPostInput(itemStack: ItemStack, event: ItemPostUpdateEvent, context: EnchantingTableContext) {
        context.menu.clearInputInventory()
    }
}