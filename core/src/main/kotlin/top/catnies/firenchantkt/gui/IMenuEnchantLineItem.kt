package top.catnies.firenchantkt.gui


import com.saicone.rtag.RtagItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.database.FirCacheManager
import top.catnies.firenchantkt.database.FirConnectionManager
import top.catnies.firenchantkt.database.entity.EnchantingHistoryTable
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.engine.ConfigConditionTemplate
import top.catnies.firenchantkt.util.ItemUtils.serializeToBytes
import top.catnies.firenchantkt.util.TaskUtils
import top.catnies.firenchantkt.util.resource_wrapper.ItemRender


interface IMenuEnchantLineItem {
    val tableMenu: AbstractFirEnchantMenu
    var conditions: List<ConfigConditionTemplate>
    var functions: List<ConfigActionTemplate>
    val lineIndex: Int
    var onlineRender: ItemRender
    var offlineRender: ItemRender
    var isBook: Boolean

    var canEnchant: Boolean

    // 渲染显示物品
    fun renderOnlineItem(itemStack: ItemStack): ItemStack = onlineRender.renderItem(itemStack).also { item ->
        // 去除CE的ID, 防止发包给我盖了
        if (!isBook) RtagItem.edit(item) { it.remove("craftengine:id") }
    }

    // 渲染不显示物品
    fun renderOfflineItem(itemStack: ItemStack): ItemStack = offlineRender.renderItem(itemStack).also { item ->
        RtagItem.edit(item) {
            // 去除数据信息, 防止偷窥具体结果
            it.remove("FirEnchant")
            // 去除CE的ID, 防止发包给我盖了
            if (!isBook) it.remove("craftengine:id")
        }
    }

    // 异步记录附魔历史
    fun recordEnchantingHistoryAsync(player: Player, inputItem: ItemStack, setting: EnchantmentSetting) {
        val historyTable = EnchantingHistoryTable().apply {
            playerId = player.uniqueId
            inputItemData = inputItem.serializeToBytes()
            seed = player.enchantmentSeed
            bookShelfCount = tableMenu.bookShelves
            enchantable = tableMenu.enchantable
            enchantment = setting.data.key.asString()
            enchantmentLevel = setting.level
            enchantmentFailure = setting.failure
            timestamp = System.currentTimeMillis()
        }
        TaskUtils.runAsyncTask {
            FirCacheManager.getInstance().addEnchantingHistory(historyTable)
            FirConnectionManager.getInstance().enchantingHistoryData.create(historyTable)
        }
    }

    // 自定义策略覆写
    var overrideActions: List<ConfigActionTemplate>?
    var overrideActiveItem: ItemRender?
    var overrideInactiveItem: ItemRender?

    fun overrideItem(
        actions: List<ConfigActionTemplate>?,
        activeItem: ItemRender?,
        inactiveItem: ItemRender?,
    ) {
        actions?.let { this.overrideActions = it }
        activeItem?.let { this.overrideActiveItem = it }
        inactiveItem?.let { this.overrideInactiveItem = it }
    }

    fun clearOverride() {
        overrideActions = null
        overrideActiveItem = null
        overrideInactiveItem = null
    }

}