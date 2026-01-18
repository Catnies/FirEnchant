package top.catnies.firenchantkt.gui.item

import com.saicone.rtag.RtagItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.event.enchantingtable.EnchantItemEvent
import top.catnies.firenchantkt.database.FirCacheManager
import top.catnies.firenchantkt.database.FirConnectionManager
import top.catnies.firenchantkt.database.entity.EnchantingHistoryTable
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.engine.ConfigConditionTemplate
import top.catnies.firenchantkt.gui.FirEnchantingTableMenu
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.ItemUtils.serializeToBytes
import top.catnies.firenchantkt.util.TaskUtils
import top.catnies.firenchantkt.util.resource_wrapper.ItemRender
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

class MenuEnchantLineItem(
    val tableMenu: FirEnchantingTableMenu,
    var conditions: List<ConfigConditionTemplate>,
    var functions: List<ConfigActionTemplate>,
    val lineIndex: Int,
    var onlineRender: ItemRender,
    var offlineRender: ItemRender,
    var isBook: Boolean = false
): AbstractItem() {

    var canEnchant: Boolean = false

    override fun getItemProvider() = ItemProvider{ lang ->
        // 未设置时直接返回空
        val enchantmentSetting = tableMenu.getEnchantmentSettingByLine(lineIndex) ?: return@ItemProvider ItemStack.empty().also {
            canEnchant = false
        }
        // 如果条件符合
        val itemStack = enchantmentSetting.toItemStack()
        if (tableMenu.activeLine >= lineIndex) {
            canEnchant = true
            //
            val overrideItem = overrideActiveItem?.renderItem(itemStack)?.also { item ->
                if (!isBook) RtagItem.edit(item) { it.remove("craftengine:id") }
            }
            return@ItemProvider overrideItem ?: renderOnlineItem(itemStack)
        }
        // 如果条件不符合
        canEnchant = false
        //
        val overrideItem = overrideInactiveItem?.renderItem(itemStack)?.also { item ->
            if (!isBook) RtagItem.edit(item) { it.remove("craftengine:id") }
        }
        return@ItemProvider overrideItem ?: renderOfflineItem(itemStack)
    }

    override fun handleClick(
        clickType: ClickType,
        player: Player,
        event: InventoryClickEvent
    ) {
        // 光标持有物品点击则不处理
        if (!event.cursor.nullOrAir()) return
        // 如果没有记录 或 可点亮栏位少于索引, 则代表条件现在已经不符合要求了
        if (!canEnchant || tableMenu.refreshCanLight() < lineIndex) {
            tableMenu.refreshLine()
            return
        }

        // 获取所需变量
        val inputItem = tableMenu.getInputInventoryItem() ?: return
        val setting = tableMenu.getEnchantmentSettingByLine(lineIndex)!!

        // 广播事件
        val enchantItemEvent = EnchantItemEvent(player, inputItem, setting, lineIndex)
        Bukkit.getPluginManager().callEvent(enchantItemEvent)
        if (enchantItemEvent.isCancelled) return

        // 记录缓存和数据
        recordEnchantingHistoryAsync(player, inputItem, setting)

        // 执行附魔
        tableMenu.clearInputInventory() // 扣除物品
        TaskUtils.runAsyncTasksLater(tableMenu::clearEnchantmentMenu, delay = 0L) // 延迟刷新菜单状态
        player.setItemOnCursor(setting.toItemStack())
        player.enchantmentSeed = (0..Int.MAX_VALUE).random()

        // 执行动作
        val toExe = if (overrideActions.isEmpty()) overrideActions else functions
        toExe.forEach { action ->
            action.executeIfAllowed(mapOf("player" to player))
        }
        //
        clearOverride()
    }

    // 渲染显示物品
    private fun renderOnlineItem(itemStack: ItemStack): ItemStack = onlineRender.renderItem(itemStack).also { item ->
        // 去除CE的ID, 防止发包给我盖了
        if (!isBook) RtagItem.edit(item) { it.remove("craftengine:id") }
    }

    // 渲染不显示物品
    private fun renderOfflineItem(itemStack: ItemStack): ItemStack = offlineRender.renderItem(itemStack).also { item ->
        RtagItem.edit(item) {
            // 去除数据信息, 防止偷窥具体结果
            it.remove("FirEnchant")
            // 去除CE的ID, 防止发包给我盖了
            if (!isBook) it.remove("craftengine:id")
        }
    }

    // 异步记录附魔历史
    private fun recordEnchantingHistoryAsync(player: Player, inputItem: ItemStack, setting: EnchantmentSetting) {
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

    private var overrideActions: List<ConfigActionTemplate> = emptyList()
    private var overrideActiveItem: ItemRender? = null
    private var overrideInactiveItem: ItemRender? = null

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
        overrideActions = emptyList()
        overrideActiveItem = null
        overrideInactiveItem = null
    }

}