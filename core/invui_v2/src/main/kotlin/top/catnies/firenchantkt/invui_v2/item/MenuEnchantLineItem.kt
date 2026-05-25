package top.catnies.firenchantkt.invui_v2.item

import com.saicone.rtag.RtagItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.event.enchantingtable.EnchantItemEvent
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.engine.ConfigConditionTemplate
import top.catnies.firenchantkt.gui.IMenuEnchantLineItem
import top.catnies.firenchantkt.invui_v2.FirEnchantingTableMenu
import top.catnies.firenchantkt.util.TaskUtils
import top.catnies.firenchantkt.util.resource_wrapper.ItemRender
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.item.AbstractItem
import xyz.xenondevs.invui.item.ItemProvider
import java.util.Locale

class MenuEnchantLineItem(
    override val tableMenu: FirEnchantingTableMenu,
    override var conditions: List<ConfigConditionTemplate>,
    override var functions: List<ConfigActionTemplate>,
    override val lineIndex: Int,
    override var onlineRender: ItemRender,
    override var offlineRender: ItemRender,
    override var isBook: Boolean = false
): AbstractItem(), IMenuEnchantLineItem {

    override var canEnchant: Boolean = false


    val itemProvider = object : ItemProvider {
        override fun get(locale: Locale): ItemStack = get()

        override fun get(): ItemStack {
            // 未设置时直接返回空
            val enchantmentSetting = tableMenu.getEnchantmentSettingByLine(lineIndex) ?: return ItemStack.empty().also {
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
                return overrideItem ?: renderOnlineItem(itemStack)
            }
            // 如果条件不符合
            canEnchant = false
            //
            val overrideItem = overrideInactiveItem?.renderItem(itemStack)?.also { item ->
                if (!isBook) RtagItem.edit(item) { it.remove("craftengine:id") }
            }
            return overrideItem ?: renderOfflineItem(itemStack)
        }
    }

    override fun getItemProvider(viewer: Player): ItemProvider = itemProvider

    override fun handleClick(
        clickType: ClickType,
        player: Player,
        click: Click
    ) {

        // 光标持有物品点击则不处理
        // TODO: 这个函数没有了, 为啥把这个改了,不知道有什么可以替代
//        if (!event.cursor.nullOrAir()) return


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
        val toExe =  overrideActions ?: functions
        toExe.forEach { action ->
            action.executeIfAllowed(mapOf("player" to player))
        }
        //
        clearOverride()
    }

    // 自定义策略覆写
    override var overrideActions: List<ConfigActionTemplate>? = null
    override var overrideActiveItem: ItemRender? = null
    override var overrideInactiveItem: ItemRender? = null
}