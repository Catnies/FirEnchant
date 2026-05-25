package top.catnies.firenchantkt.invui_v1

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.event.repairtable.BrokenItemConfirmRepairEvent
import top.catnies.firenchantkt.api.event.repairtable.BrokenItemInputEvent
import top.catnies.firenchantkt.api.event.repairtable.RepairingItemCancelEvent
import top.catnies.firenchantkt.api.event.repairtable.RepairingItemReceiveEvent
import top.catnies.firenchantkt.config.RepairTableConfig
import top.catnies.firenchantkt.context.RepairTableContext
import top.catnies.firenchantkt.database.FirConnectionManager
import top.catnies.firenchantkt.database.dao.ItemRepairData
import top.catnies.firenchantkt.database.entity.ItemRepairTable
import top.catnies.firenchantkt.gui.FirRepairCostHelper
import top.catnies.firenchantkt.gui.RepairTableMenu
import top.catnies.firenchantkt.invui_v1.ext.wrapTitle
import top.catnies.firenchantkt.invui_v1.item.MenuCustomItem
import top.catnies.firenchantkt.invui_v1.item.MenuPageItem
import top.catnies.firenchantkt.invui_v1.item.MenuRepairItem
import top.catnies.firenchantkt.item.FirRepairTableItemRegistry
import top.catnies.firenchantkt.item.brokengear.FirBrokenGear
import top.catnies.firenchantkt.language.MessageConstants
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.ItemUtils.serializeToBytes
import top.catnies.firenchantkt.util.MessageUtils.renderToComponent
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.PlayerUtils.giveOrDrop
import top.catnies.firenchantkt.util.PlayerUtils.giveOrDropList
import top.catnies.firenchantkt.util.TaskUtils
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.gui.structure.Structure
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.Click
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

@Suppress("UnstableApiUsage")
class FirRepairTableMenu(
    val player: Player
): RepairTableMenu {

    companion object {
        val plugin = FirEnchantPlugin.Companion.instance
        val config = RepairTableConfig.Companion.instance
        val brokenGear = FirBrokenGear.Companion.instance
    }

    /*配置文件缓存*/
    val titleDeny = config.MENU_TITLE_DENY
    val titleAccept = config.MENU_TITLE_ACCEPT
    val structureArray = config.MENU_STRUCTURE_ARRAY
    val inputSlot = config.MENU_INPUT_SLOT
    val outputSlot = config.MENU_OUTPUT_SLOT
    val repairItem = config.MENU_REPAIR_ITEM!!
    val previousPageItem = config.MENU_PREVIOUS_PAGE_ITEM
    val nextPageItem = config.MENU_NEXT_PAGE_ITEM
    val customItems = config.MENU_CUSTOM_ITEMS

    val outputUpdateTime = config.MENU_OUTPUT_UPDATE_TIME
    val activeAdditionLores = config.MENU_OUTPUT_ACTIVE_ADDITION_LORE
    val completedAdditionLores = config.MENU_OUTPUT_COMPLETED_ADDITION_LORE

    /*构建时对象*/
    lateinit var gui: PagedGui<Item>
    lateinit var window: Window
    lateinit var inputInventory: VirtualInventory
    lateinit var confirmBottom: SimpleItem
    lateinit var previousPageBottom: MenuPageItem
    lateinit var nextPageBottom: MenuPageItem

    var closeHandlers: MutableList<Runnable> = mutableListOf() // 关闭菜单时触发
    var showBottom: Boolean = false

    /*数据对象*/
    val itemRepairData: ItemRepairData = FirConnectionManager.getInstance().itemRepairData
    var maxRepairItemQueueSize: Int = FirRepairCostHelper.getRepairArraySize(player) // 最大修复队列长度
    val repairList: MutableList<Item> = mutableListOf() // 展示在菜单的修复的列表

    var repairTime: Long? = null // 当前放入的破损物品的修复时间

    // 打开菜单
    override fun openMenu(data: Map<String, Any>, async: Boolean) {
        val buildTask = {
            initRepairItems() // 读取玩家修复列表, 构建列表
            buildInputInventory()
            buildConfirmItem()
            buildPageItem()
            buildGuiAndWindow()
        }
        if (async) {
            TaskUtils.runAsyncTaskWithSyncCallback(
                async = buildTask,
                callback = { window.open() }
            )
        } else {
            buildTask()
            window.open()
        }
    }

    // 添加输入虚拟背包
    private fun buildInputInventory() {
        inputInventory = VirtualInventory(1)

        // 禁止非破损物品放入, 同时刷新菜单标题.
        inputInventory.preUpdateHandler = Consumer { event ->
            val isInputBrokenGear = brokenGear.isBrokenGear(event.newItem)
            when {
                (event.isAdd || event.isSwap) && isInputBrokenGear -> {
                    // 计算修复时长
                    val repairTimeCost = FirRepairCostHelper.getRepairTimeCost(player, event.newItem!!) * 1000L
                    // 广播事件
                    val inputEvent = BrokenItemInputEvent(player, event.newItem, repairTimeCost)
                    Bukkit.getPluginManager().callEvent(inputEvent)
                    if (inputEvent.isCancelled) {
                        event.isCancelled = true
                        return@Consumer
                    }
                    // 修改属性
                    showBottom = true
                    repairTime = inputEvent.repairTime // 将修复时间传递到点击按钮上
                    confirmBottom.notifyWindows()
                    // 延迟刷新标题, 否则可能会把物品刷新给覆盖掉.
                    TaskUtils.runAsyncTasksLater(
                        { window.changeTitle(titleAccept.wrapTitle(player)) },
                        delay = 0L
                    )
                }

                event.isRemove -> {
                    TaskUtils.runAsyncTasksLater({ window.changeTitle(titleDeny.wrapTitle(player)) }, delay = 1L)
                    showBottom = false
                    repairTime = null
                    confirmBottom.notifyWindows()
                }

                else -> {
                    event.isCancelled = true
                    return@Consumer
                }
            }
        }
    }

    // 添加确认修复按钮
    private fun buildConfirmItem() {
        val itemProvider = ItemProvider {
            if (!showBottom) return@ItemProvider ItemStack(Material.AIR)
            return@ItemProvider repairItem.item.renderItem(
                player,
                mutableMapOf("cost_time" to "${repairTime!! / 1000}")
            )
        }
        val clickHandler = Consumer<Click> { click ->
            if (!showBottom) return@Consumer // 无显示时不做任何操作

            // 如果超出了队列上限
            if (repairList.size >= maxRepairItemQueueSize) {
                player.sendTranslatableComponent(
                    MessageConstants.REPAIR_TABLE_REPAIR_ITEM_QUEUE_FULL,
                    maxRepairItemQueueSize
                )
                return@Consumer
            }

            // 执行动作
            val args = mutableMapOf<String, Any?>()
            args["player"] = player
            args["clickType"] = click.clickType
            args["event"] = click.event
            repairItem.action.forEach { it.executeIfAllowed(args) }
            // 执行修复功能
            val inputItem = inputInventory.items.first() ?: return@Consumer
            if (!brokenGear.isBrokenGear(inputItem)) return@Consumer
            val repairTable = ItemRepairTable(player.uniqueId, inputItem.serializeToBytes(), repairTime!!)

            // 广播事件
            val repairEvent = BrokenItemConfirmRepairEvent(player, repairTable)
            Bukkit.getPluginManager().callEvent(repairEvent)
            if (repairEvent.isCancelled) return@Consumer

            // 将物品删除
            inputInventory.removeIf(UpdateReason.SUPPRESSED) { !it.nullOrAir() }

            // 插入队列, 刷新队列
            addDataToRepairList(repairTable)
            itemRepairData.insert(repairTable)
            gui.setContent(repairList)

            // 刷新自己
            showBottom = false
            repairTime = null
            confirmBottom.notifyWindows()

            // 延迟刷新菜单
            TaskUtils.runAsyncTasksLater(
                { window.changeTitle(titleDeny.wrapTitle(player)) },
                delay = 0L
            )
        }
        confirmBottom = SimpleItem(itemProvider, clickHandler)
    }

    // 上一页 和 下一页
    private fun buildPageItem() {
        previousPageItem?.let {
            previousPageBottom = MenuPageItem(false, it.action) { _ ->
                if (gui.currentPage == 0) return@MenuPageItem ItemStack.empty()

                val itemStack = it.item.renderItem(
                    player, mutableMapOf(
                        "currentPage" to "${gui.currentPage}",
                        "pageAmount" to "${gui.pageAmount}",
                        "previousPage" to "${max(0, gui.currentPage - 1)}",
                        "nextPage" to "${min(gui.pageAmount, gui.currentPage + 1)}"
                    )
                )
                return@MenuPageItem itemStack
            }
        }

        nextPageItem?.let {
            nextPageBottom = MenuPageItem(true, it.action) { _ ->
                if (gui.pageAmount == 0) return@MenuPageItem ItemStack.empty() // 总页数为0代表目前没有正在修复的装备
                if (gui.currentPage == gui.pageAmount - 1) return@MenuPageItem ItemStack.empty() // 如果当前页数 = (总页数 - 1)就代表是最后一页

                val itemStack = nextPageItem.item.renderItem(
                    player, mutableMapOf(
                        "currentPage" to "${gui.currentPage}",
                        "pageAmount" to "${gui.pageAmount}",
                        "previousPage" to "${max(0, gui.currentPage - 1)}",
                        "nextPage" to "${min(gui.pageAmount, gui.currentPage + 1)}"
                    )
                )
                return@MenuPageItem itemStack
            }
        }
    }

    // 创建 GUI & Window
    private fun buildGuiAndWindow() {
        val builder = PagedGui.items().setStructure(Structure(*structureArray))
        // 内容
        builder.addIngredient(outputSlot, Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.setContent(repairList)
        // 输入槽位
        builder.addIngredient(inputSlot, inputInventory)
        // 确认按钮
        builder.addIngredient(repairItem.slot, confirmBottom)
        // 翻页按钮
        previousPageItem?.let { builder.addIngredient(it.slot, previousPageBottom) }
        nextPageItem?.let { builder.addIngredient(it.slot, nextPageBottom) }
        // 自定义物品
        customItems.filter { customItem -> getMarkCount(customItem.slot) > 0 }
            .forEach { data ->
                val menuCustomItem = MenuCustomItem({ _ -> data.item.renderItem(player) }, data.action)
                builder.addIngredient(data.slot, menuCustomItem)
            }
        gui = builder.build()

        // 如果关闭菜单则返回输入框里的所有物品.
        closeHandlers.add {
            val itemStacks = inputInventory.items.toList().filterNotNull()
            player.giveOrDropList(itemStacks)
        }

        window = Window.single {
            it.setViewer(player)
            it.setTitle(titleDeny.wrapTitle(player))
            it.setCloseHandlers(closeHandlers)
            it.setGui(gui)
            it.build()
        }
    }

    // 创建修复队列
    private fun initRepairItems() {
        val activeData = itemRepairData.getByPlayerActiveAndCompletedList(player.uniqueId)
        activeData.forEach { addDataToRepairList(it) }
    }

    // 往修复队列里添加新的数据
    private fun addDataToRepairList(itemRepairTable: ItemRepairTable) {
        if (itemRepairTable.isReceived) return

        // 创建 ItemProvider
        val brokenItem = itemRepairTable.brokenItem
        val repairedItem = itemRepairTable.repairedItem
        val itemProvider = ItemProvider { _ ->
            val resultLore = brokenItem.getData(DataComponentTypes.LORE)?.lines()?.toMutableList()
            // 已修复
            if (itemRepairTable.isCompleted) {
                val components = completedAdditionLores.map { line -> line.renderToComponent(player) }
                val lore = ItemLore.lore().let { builder ->
                    resultLore?.let { builder.addLines(it) }
                    builder.addLines(components).build()
                }
                repairedItem.clone().apply { setData(DataComponentTypes.LORE, lore) }
            }
            // 未修复
            else {
                val components = activeAdditionLores.map { line ->
                    line.renderToComponent(
                        player,
                        mapOf("cost_time" to "${itemRepairTable.getRemainingTime() / 1000}")
                    )
                }
                val lore = ItemLore.lore().let { builder ->
                    resultLore?.let { builder.addLines(it) }
                    builder.addLines(components).build()
                }
                brokenItem.clone().apply { setData(DataComponentTypes.LORE, lore) }
            }
        }

        // 构建物品
        val autoUpdateItem = MenuRepairItem(itemRepairTable, outputUpdateTime, brokenItem, itemProvider, { click ->
            when {
                // 当装备已经被领取
                itemRepairTable.isReceived -> return@MenuRepairItem true
                // 当装备修复完成
                itemRepairTable.isCompleted -> {
                    // 广播事件
                    val event = RepairingItemReceiveEvent(player, itemRepairTable)
                    Bukkit.getPluginManager().callEvent(event)
                    if (event.isCancelled) return@MenuRepairItem true
                    // 执行
                    removeDataFromRepairList(itemRepairTable)
                    itemRepairData.insert(itemRepairTable.apply { isReceived = true })
                    gui.setContent(repairList)
                    player.giveOrDrop(itemRepairTable.repairedItem)
                    click.player.sendTranslatableComponent(MessageConstants.REPAIR_TABLE_REPAIR_ITEM_RECEIVE_SUCCESS)
                }
                // 当装备还在修复中, 使用道具
                (!itemRepairTable.isCompleted && !click.event.cursor.nullOrAir()) -> {
                    val applicableItem =
                        FirRepairTableItemRegistry.Companion.instance.findApplicableItem(click.event.cursor)
                            ?: return@MenuRepairItem true
                    applicableItem.onUse(click.event, RepairTableContext(player, click.event.cursor, itemRepairTable))
                }
                // 当装备还在修复中, 取消修复
                (!itemRepairTable.isCompleted && click.clickType == ClickType.SHIFT_LEFT) -> {
                    // 广播事件
                    val event = RepairingItemCancelEvent(player, itemRepairTable)
                    Bukkit.getPluginManager().callEvent(event)
                    if (event.isCancelled) return@MenuRepairItem true
                    // 执行
                    removeDataFromRepairList(itemRepairTable)
                    itemRepairData.remove(itemRepairTable)
                    gui.setContent(repairList)
                    player.giveOrDrop(itemRepairTable.brokenItem)
                    click.player.sendTranslatableComponent(MessageConstants.REPAIR_TABLE_REPAIR_ITEM_CANCEL_SUCCESS)
                }

            }
            true
        })

        repairList.add(autoUpdateItem)
    }

    // 从修复队列里删除数据
    private fun removeDataFromRepairList(itemRepairTable: ItemRepairTable) {
        repairList.removeIf { (it as? MenuRepairItem)?.data?.id == itemRepairTable.id }
    }

    // 统计 Structure 里有多少个某种 Slot 字符
    private fun getMarkCount(char: Char) = structureArray.sumOf { it.count { c -> c == char } }
}