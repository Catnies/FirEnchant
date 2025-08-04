package top.catnies.firenchantkt.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.config.EnchantingTableConfig
import top.catnies.firenchantkt.context.EnchantingTableContext
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.gui.item.MenuCustomItem
import top.catnies.firenchantkt.gui.item.MenuEnchantLineItem
import top.catnies.firenchantkt.item.FirEnchantingTableRegistry
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.MessageUtils.wrapTitle
import top.catnies.firenchantkt.util.PlayerUtils.giveOrDropList
import top.catnies.firenchantkt.util.TaskUtils
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.structure.Structure
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer

class FirEnchantingTableMenu(
    val player: Player,
    var bookShelves: Int = 0
): EnchantingTableMenu {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val config = EnchantingTableConfig.instance
    }

    private val titleMap by lazy {
        mapOf(
            "000" to config.MENU_TITLE_000,
            "100" to config.MENU_TITLE_100,
            "110" to config.MENU_TITLE_110,
            "111" to config.MENU_TITLE_111,
            "222" to config.MENU_TITLE_222,
            "122" to config.MENU_TITLE_122,
            "112" to config.MENU_TITLE_112,
            "022" to config.MENU_TITLE_022,
            "002" to config.MENU_TITLE_002,
            "102" to config.MENU_TITLE_102
        )
    }

    val structureArray = config.MENU_STRUCTURE_ARRAY
    val inputSlot = config.MENU_INPUT_SLOT
    val customItems = config.MENU_CUSTOM_ITEMS

    val enchantmentLine1Slot = config.MENU_SHOW_ENCHANTMENT_LINE_1_SLOT
    val enchantmentLine2Slot = config.MENU_SHOW_ENCHANTMENT_LINE_2_SLOT
    val enchantmentLine3Slot = config.MENU_SHOW_ENCHANTMENT_LINE_3_SLOT
    val enchantmentBook1Slot = config.MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_SLOT
    val enchantmentBook2Slot = config.MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_SLOT
    val enchantmentBook3Slot = config.MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_SLOT

    var conditionLine1 = config.ENCHANT_COST_LINE_1_CONDITIONS
    var conditionLine2 = config.ENCHANT_COST_LINE_2_CONDITIONS
    var conditionLine3 = config.ENCHANT_COST_LINE_3_CONDITIONS
    var actionLine1 = config.ENCHANT_COST_LINE_1_ACTIONS
    var actionLine2 = config.ENCHANT_COST_LINE_2_ACTIONS
    var actionLine3 = config.ENCHANT_COST_LINE_3_ACTIONS

    val enchantmentOnline1 = config.MENU_SHOW_ENCHANTMENT_LINE_1_ONLINE
    val enchantmentOnline2 = config.MENU_SHOW_ENCHANTMENT_LINE_2_ONLINE
    val enchantmentOnline3 = config.MENU_SHOW_ENCHANTMENT_LINE_3_ONLINE
    val enchantmentOffline1 = config.MENU_SHOW_ENCHANTMENT_LINE_1_OFFLINE
    val enchantmentOffline2 = config.MENU_SHOW_ENCHANTMENT_LINE_2_OFFLINE
    val enchantmentOffline3 = config.MENU_SHOW_ENCHANTMENT_LINE_3_OFFLINE
    val enchantmentBookOnline1 = config.MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_ONLINE
    val enchantmentBookOnline2 = config.MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_ONLINE
    val enchantmentBookOnline3 = config.MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_ONLINE
    val enchantmentBookOffline1 = config.MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_OFFLINE
    val enchantmentBookOffline2 = config.MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_OFFLINE
    val enchantmentBookOffline3 = config.MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_OFFLINE

    var enchantable = 0
    var activeLine = -1
    var lineStatus: String = "000"

    private val enchantmentSettings = arrayOfNulls<EnchantmentSetting>(3)
    var settingLine1: EnchantmentSetting?
        get() = enchantmentSettings[0]
        set(value) { enchantmentSettings[0] = value }
    var settingLine2: EnchantmentSetting?
        get() = enchantmentSettings[1]
        set(value) { enchantmentSettings[1] = value }
    var settingLine3: EnchantmentSetting?
        get() = enchantmentSettings[2]
        set(value) { enchantmentSettings[2] = value }

    lateinit var gui: Gui
    lateinit var window: Window
    lateinit var inputInventory: VirtualInventory
    private lateinit var selectBottoms: Array<MenuEnchantLineItem>
    private lateinit var bookBottoms: Array<MenuEnchantLineItem>

    private val enchantingTableContext by lazy { EnchantingTableContext(player, bookShelves, this) }

    // 关闭菜单时触发
    var closeHandlers: MutableList<Runnable> = mutableListOf()

    // 创建并且打开菜单
    override fun openMenu(data: Map<String, Any>, async: Boolean) {
        val buildTask = {
            buildBaseComponents()
            buildLineBottoms()
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

    // 创建基础组件
    private fun buildBaseComponents() {
        inputInventory = VirtualInventory(1)
        inputInventory.setMaxStackSize(0, 1)
        inputInventory.preUpdateHandler = Consumer { event ->
            // 添加了新物品, 执行检查
            if (event.isAdd || event.isSwap) {
                val applicableItem = FirEnchantingTableRegistry.instance.findApplicableItem(event.newItem!!)
                applicableItem?.onPreInput(event.newItem!!, event, enchantingTableContext)
            }
        }
        inputInventory.postUpdateHandler = Consumer { event ->
            // 添加了新物品, 执行检查
            if (event.isAdd || event.isSwap) {
                val applicableItem = FirEnchantingTableRegistry.instance.findApplicableItem(event.newItem!!)
                applicableItem?.onPostInput(event.newItem!!, event, enchantingTableContext)
            }
            // 移除物品, 重置菜单
            if (event.isRemove) {
                clearEnchantmentMenu()
            }
        }

        // 如果关闭菜单则返回输入框里的所有物品.
        closeHandlers.add {
            val itemStacks = inputInventory.items.toList().filterNotNull()
            player.giveOrDropList(itemStacks)
        }
    }

    // 创建附魔栏物品
    private fun buildLineBottoms() {
        // 配置数据数组，便于批量创建
        val lineConfigs = arrayOf(
            Triple(conditionLine1, actionLine1, 1),
            Triple(conditionLine2, actionLine2, 2),
            Triple(conditionLine3, actionLine3, 3)
        )

        val onlineConfigs = arrayOf(enchantmentOnline1, enchantmentOnline2, enchantmentOnline3)
        val offlineConfigs = arrayOf(enchantmentOffline1, enchantmentOffline2, enchantmentOffline3)
        val bookOnlineConfigs = arrayOf(enchantmentBookOnline1, enchantmentBookOnline2, enchantmentBookOnline3)
        val bookOfflineConfigs = arrayOf(enchantmentBookOffline1, enchantmentBookOffline2, enchantmentBookOffline3)

        selectBottoms = Array(3) { i ->
            MenuEnchantLineItem(this, lineConfigs[i].third, lineConfigs[i].first,
                lineConfigs[i].second, onlineConfigs[i], offlineConfigs[i])
        }

        bookBottoms = Array(3) { i ->
            MenuEnchantLineItem(this, lineConfigs[i].third, lineConfigs[i].first,
                lineConfigs[i].second, bookOnlineConfigs[i], bookOfflineConfigs[i])
        }
    }

    // 创建 GUI & Window
    private fun buildGuiAndWindow() {
        gui = Gui.normal()
            .setStructure(Structure(*structureArray))
            .addIngredient(inputSlot, inputInventory)

            .addIngredient(enchantmentLine1Slot, selectBottoms[0])
            .addIngredient(enchantmentLine2Slot, selectBottoms[1])
            .addIngredient(enchantmentLine3Slot, selectBottoms[2])
            .addIngredient(enchantmentBook1Slot, bookBottoms[0])
            .addIngredient(enchantmentBook2Slot, bookBottoms[1])
            .addIngredient(enchantmentBook3Slot, bookBottoms[2])

            .apply { addCustomItems(this) }
            .build()

        window = Window.single {
            it.setViewer(player)
            it.setTitle(titleMap["222"]!!.wrapTitle(player))
            it.setCloseHandlers(closeHandlers)
            it.setGui(gui)
            it.build()
        }
    }

    // 添加自定义物品
    private fun addCustomItems(building: Gui.Builder.Normal) {
        customItems.asSequence()
            .filterNot { it.value.first.nullOrAir() }
            .filter { getMarkCount(it.key) > 0 }
            .forEach { (char, pair) ->
                val menuCustomItem = MenuCustomItem({ s -> pair.first!! }, pair.second)
                building.addIngredient(char, menuCustomItem)
            }
    }

    // 设置附魔台的结果显示
    override fun setEnchantmentResult(list: List<EnchantmentSetting>) {
        enchantmentSettings.fill(null)
        list.forEachIndexed { index, setting ->
            if (index < 3) {
                enchantmentSettings[index] = setting
            }
        }
    }

    // 检查玩家能亮几个附魔栏位
    override fun refreshCanLight(): Int {
        val args = mapOf("player" to player)
        val conditions = arrayOf(conditionLine1, conditionLine2, conditionLine3)

        // 找到第一个不满足条件的栏位
        activeLine = conditions.indexOfFirst { condition ->
            condition.any { !it.check(args) }
        }.let { if (it == -1) 3 else it }

        // 优化状态计算
        lineStatus = calculateLineStatus()

        return activeLine
    }

    // 计算菜单状态逻辑
    private fun calculateLineStatus(): String {
        val hasSettings = enchantmentSettings.map { it != null }

        return when {
            hasSettings.all { it } -> when (activeLine) {
                0 -> "000"
                1 -> "100"
                2 -> "110"
                else -> "111"
            }
            hasSettings.none { it } -> "222"
            !hasSettings[1] -> if (activeLine < 1) "022" else "122"
            !hasSettings[2] -> when {
                activeLine < 1 -> "002"
                activeLine < 2 -> "102"
                else -> "112"
            }
            else -> "000"
        }
    }

    // 刷新附魔栏位
    override fun refreshLine() {
        titleMap[lineStatus]?.let { window.changeTitle(it.wrapTitle(player)) }
        selectBottoms.forEach { it.notifyWindows() }
        bookBottoms.forEach { it.notifyWindows() }
    }

    // 获取指定栏位的附魔书数据
    override fun getEnchantmentSettingByLine(line: Int): EnchantmentSetting? {
        require(line in 1..3) { "获取索引只能是1~3." }
        return enchantmentSettings[line - 1]
    }

    // 清空附魔台状态
    fun clearEnchantmentMenu() {
        enchantmentSettings.fill(null)
        enchantable = 0
        lineStatus = "222"
        refreshLine()
    }

    // 设置记录的物品附魔力
    override fun setRecordEnchantable(enchantable: Int) {
        this.enchantable = enchantable
    }

    // 获取容器内的物品
    override fun getInputInventoryItem(): ItemStack? {
        return inputInventory.items.firstOrNull()
    }

    // 清空容器
    override fun clearInputInventory() {
        if (inputInventory.isEmpty) return
        inputInventory.removeIf(UpdateReason.SUPPRESSED) { true }
    }

    // 统计 Structure 里有多少个某种 Slot 字符
    private fun getMarkCount(char: Char) = structureArray.sumOf { it.count { c -> c == char } }
}