package top.catnies.firenchantkt.gui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.config.FixTableConfig
import top.catnies.firenchantkt.database.FirConnectionManager
import top.catnies.firenchantkt.database.dao.ItemRepairData
import top.catnies.firenchantkt.database.entity.ItemRepairTable
import top.catnies.firenchantkt.item.fixtable.FirBrokenGear
import top.catnies.firenchantkt.util.ItemUtils.deserializeFromBytes
import top.catnies.firenchantkt.util.ItemUtils.getEnchantmentValue
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.ItemUtils.replacePlaceholder
import top.catnies.firenchantkt.util.ItemUtils.serializeToBytes
import top.catnies.firenchantkt.util.PlayerUtils.giveOrDropList
import top.catnies.firenchantkt.util.TaskUtils
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.gui.structure.Structure
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class FirFixTableMenu(
    val player: Player
): FixTableMenu {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val config = FixTableConfig.instance
        val brokenGear = FirBrokenGear.instance
    }

    /*配置文件缓存*/
    val titleDeny = config.MENU_TITLE_DENY
    val titleAccept = config.MENU_TITLE_ACCEPT
    val structureArray = config.MENU_STRUCTURE_ARRAY
    val inputSlot = config.MENU_INPUT_SLOT
    val outputSlot = config.MENU_OUTPUT_SLOT
    val fixSlot = config.MENU_FIX_SLOT
    val fixSlotItem = config.MENU_FIX_SLOT_ITEM
    val previousPageSlot = config.MENU_PREPAGE_SLOT
    val previousPageItem = config.MENU_PREPAGE_SLOT_ITEM
    val nextPageSlot = config.MENU_NEXTPAGE_SLOT
    val nextPageItem = config.MENU_NEXTPAGE_SLOT_ITEM
    val customItems = config.MENU_CUSTOM_ITEMS

    val outputUpdateTime = config.MENU_OUTPUT_UPDATE_TIME
    val activeAdditionLores = config.MENU_OUTPUT_ACTIVE_ADDITION_LORE
    val completedAdditionLores = config.MENU_OUTPUT_COMPLETED_ADDITION_LORE

    /*构建时对象*/
    private lateinit var gui: PagedGui<Item>
    private lateinit var window: Window
    private lateinit var inputInventory: VirtualInventory
    private lateinit var confirmBottom: SimpleItem
    private lateinit var previousPageBottom: PageItem
    private lateinit var nextPageBottom: PageItem

    var closeHandlers: MutableList<Runnable> = mutableListOf() // 关闭菜单时触发
    var showBottom: Boolean = false

    /*数据对象*/
    val itemRepairData: ItemRepairData = FirConnectionManager.getInstance().itemRepairData
    val repairList: MutableList<Item> = mutableListOf() // 展示在菜单的修复的列表

    // 打开菜单
    override fun openMenu(data: Map<String, Any>, async: Boolean) {
        if (async) {
            TaskUtils.runAsyncTaskWithSyncCallback(
                async = {
                    initRepairItems() // 读取玩家修复列表, 构建列表
                    buildInputInventory()
                    buildConfirmItem()
                    buildPageItem()
                    buildGuiAndWindow()
                },
                callback = { window.open() }
            )
        } else {
            buildConfirmItem() // 读取玩家修复列表, 构建列表
            buildPageItem()
            buildGuiAndWindow()
            window.open()
        }
    }

    // 创建 GUI & Window
    private fun buildGuiAndWindow() {
        gui = PagedGui.items()
            .setStructure(Structure(*structureArray))
            .addIngredient(outputSlot, Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient(inputSlot, inputInventory)
            .addIngredient(fixSlot, confirmBottom)
            .addIngredient(previousPageSlot, previousPageBottom)
            .addIngredient(nextPageSlot, nextPageBottom)
            // 翻页内容
            .setContent(repairList)
            // 自定义物品
            .also {
                customItems.filter { customItem -> getMarkCount(customItem.key) > 0 }
                    .forEach { (char, pair) ->
                        val menuCustomItem = MenuCustomItem({ s -> pair.first!! }, pair.second)
                        it.addIngredient(char, menuCustomItem)
                    }
            }
            .build()
        // 如果关闭菜单则返回输入框里的所有物品.
        closeHandlers.add {
            val itemStacks = inputInventory.items.toList().filterNotNull()
            player.giveOrDropList(itemStacks)
        }

        window = Window.single {
            it.setViewer(player)
            it.setTitle(titleDeny)
            it.setCloseHandlers(closeHandlers)
            it.setGui(gui)
            it.build()
        }
    }

    // 添加输入虚拟背包
    private fun buildInputInventory() {
        inputInventory = VirtualInventory(1)
        // 禁止非破损物品放入
        inputInventory.preUpdateHandler = Consumer { event ->
            if (event.isAdd && !brokenGear.isBrokenGear(event.newItem)) {
                event.isCancelled = true
                return@Consumer
            }
        }
        // 刷新菜单标题
        inputInventory.postUpdateHandler = Consumer { event ->
            if (!event.newItem.nullOrAir()) {
                window.changeTitle(titleAccept)
                showBottom = true
                confirmBottom.notifyWindows()
            } else {
                window.changeTitle(titleDeny)
                showBottom = false
                confirmBottom.notifyWindows()
            }
        }
    }

    // 构建确认按钮
    private fun buildConfirmItem() {
        confirmBottom = SimpleItem({
            // 未放入破损物品时,显示空气
            if (!showBottom)
                return@SimpleItem ItemStack(Material.AIR)
            //
            else {
                // 显示修复所需时间
                val show = fixSlotItem?.clone() // 需要使用clone以防止占位符丢失
                show?.replacePlaceholder(mutableMapOf(
                    "cost_time" to "${getCostTime(inputInventory.items.first())}"
                ))
                return@SimpleItem show ?: ItemStack(Material.AIR)
            }
        }) {
            // 点击事件 -> 转移损坏物品到修复队列中
            val inputItem = inputInventory.items.first() ?: return@SimpleItem
            if (!brokenGear.isBrokenGear(inputItem)) return@SimpleItem

            val repairTime = getCostTime(inputItem) * 1000L // 计算物品修复时间
            val repairTable = ItemRepairTable(player.uniqueId, inputItem.serializeToBytes(), repairTime)
            itemRepairData.insert(repairTable) // 操作数据库
            // 安全将玩家放入的破碎物品移出
            // Notice! 此方法似乎不会触发inputInventory的两个 UpdateHandler
            inputInventory.removeIf(UpdateReason.SUPPRESSED) { !it.nullOrAir() }
            // 往修复队列 repairList 中添加UI元素
            addDataToRepairList(repairTable)
            // 刷新自己(确认按钮)和修复队列
            gui.setContent(repairList)
            showBottom = false // 因此得手动设置旗标变量为 false
            confirmBottom.notifyWindows()
        }
    }

    // 从数据库中读取并构建修复队列物品
    private fun initRepairItems() {
        val activeData = itemRepairData.getByPlayerActiveAndCompletedList(player.uniqueId)
        activeData.removeIf { it.received }
        activeData.forEach { addDataToRepairList(it) }
    }

    // 根据给予的ItemRepairTable数据, 向 修复队列 中添加UI元素
    private fun addDataToRepairList(itemRepairTable: ItemRepairTable) {
        if (itemRepairTable.received) return

        val originItem = itemRepairTable.itemData.deserializeFromBytes()
        // TODO 修正 lore 颜色, 或者寻找合适的方法刷新 lore
        val provider = ItemProvider {
//            player.sendMessage("1")
            val item = originItem.clone()

            val added : List<Component>
            // 完成lore
            if (itemRepairTable.isCompleted)  added = completedAdditionLores.map { return@map Component.text(it) }
            // 未完成lore
            else added = activeAdditionLores.map { return@map Component.text(it) }
            if (item.lore() != null)
                item.lore()!!.addAll(added)
            else {
                item.lore(added)
            }
            // TODO 补全付费提示
            item.replacePlaceholder(mutableMapOf(
                "remain_time" to "${itemRepairTable.remainingTime / 1000}"
            ))
            return@ItemProvider item
        }
        val autoUpdateItem = MenuRepairItem(
            data = itemRepairTable,
            period = outputUpdateTime,
            originItem = originItem,
            showItem = provider,
            clickHandler = { click ->
            // 点击事件 -> 取出物品
            // 检查物品状态是完成or未完成
            if (itemRepairTable.isCompleted) {
                // 给予玩家修复后物品
                val item = itemRepairTable.repairedItem
                click.player.give(item)
                // TODO 正确提示玩家
                click.player.sendMessage("该物品修复完成!")
            // 未完成
            } else {
                // TODO 增加付费加速功能
                // 给予玩家修复后物品
                val item = itemRepairTable.getBrokenItem()
                click.player.give(item)
                // TODO 正确提示玩家
                click.player.sendMessage ("取消修复物品!")
            }
            // 更新数据库
            itemRepairTable.received = true
            itemRepairData.insert(itemRepairTable)
            // 删除修复队列中的UI
            removeDataFromRepairList(itemRepairTable)
            gui.setContent(repairList)
            // 刷新gui
            repairList.forEach { item ->  item.notifyWindows() }

            true
        })

        repairList.add(autoUpdateItem)

    }

    // 上一页 和 下一页
    private fun buildPageItem() {
        previousPageBottom = object :PageItem(false) {
            override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
                // 需要使用Clone
                val itemStack = previousPageItem!!.clone()
                itemStack.replacePlaceholder(mutableMapOf(
                    "currentPage" to "${gui.currentPage}",
                    "pageAmount" to "${gui.pageAmount}",
                    "previousPage" to "${max(0, gui.currentPage - 1)}",
                    "nextPage" to "${min(gui.pageAmount, gui.currentPage + 1)}"
                ))
                return ItemBuilder(itemStack)
            }
        }
        nextPageBottom = object :PageItem(true) {
            override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
                val itemStack = nextPageItem!!.clone()
                itemStack.replacePlaceholder(mutableMapOf(
                    "currentPage" to "${gui.currentPage}",
                    "pageAmount" to "${gui.pageAmount}",
                    "previousPage" to "${max(0, gui.currentPage - 1)}",
                    "nextPage" to "${min(gui.pageAmount, gui.currentPage + 1)}"
                ))
                return ItemBuilder(itemStack)
            }
        }
    }

    // 从修复队列里删除数据
    private fun removeDataFromRepairList(itemRepairTable: ItemRepairTable) {
        repairList.removeIf { (it as? MenuRepairItem)?.data?.id == itemRepairTable.id }
    }

    // 统计 Structure 里有多少个某种 Slot 字符
    private fun getMarkCount(char: Char) = structureArray.sumOf { it.count { c -> c == char } }

    // 返回修复所需秒
    fun getCostTime(item: ItemStack?) : Int {
        var result = 1800
        // 根据魔咒等级额外增加时间
        item?.let { result += (it.getEnchantmentValue() * 600).toInt() }
        return result
    }
}