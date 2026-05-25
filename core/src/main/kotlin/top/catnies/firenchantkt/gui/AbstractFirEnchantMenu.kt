package top.catnies.firenchantkt.gui

import org.bukkit.entity.Player
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.config.EnchantingTableConfig
import top.catnies.firenchantkt.context.EnchantingTableContext
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.engine.ConfigConditionTemplate
import top.catnies.firenchantkt.util.resource_wrapper.ItemRender

abstract class AbstractFirEnchantMenu(
    open val player: Player,
    open var bookShelves: Int = 0
) : EnchantingTableMenu {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val config = EnchantingTableConfig.instance
    }

    protected val titleMap by lazy {
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

    val enchantmentLine1 = config.MENU_SHOW_ENCHANTMENT_LINE_1!!
    val enchantmentLine2 = config.MENU_SHOW_ENCHANTMENT_LINE_2!!
    val enchantmentLine3 = config.MENU_SHOW_ENCHANTMENT_LINE_3!!
    val enchantmentBook1 = config.MENU_SHOW_ENCHANTMENT_BOOK_1!!
    val enchantmentBook2 = config.MENU_SHOW_ENCHANTMENT_BOOK_2!!
    val enchantmentBook3 = config.MENU_SHOW_ENCHANTMENT_BOOK_3!!
    var conditionLine1 = config.ENCHANT_COST_LINE_1_CONDITIONS
    var conditionLine2 = config.ENCHANT_COST_LINE_2_CONDITIONS
    var conditionLine3 = config.ENCHANT_COST_LINE_3_CONDITIONS
    var actionLine1 = config.ENCHANT_COST_LINE_1_ACTIONS
    var actionLine2 = config.ENCHANT_COST_LINE_2_ACTIONS
    var actionLine3 = config.ENCHANT_COST_LINE_3_ACTIONS

    var enchantable = 0
    var activeLine = -1
    var lineStatus: String = "222"
    protected open val enchantmentSettings = arrayOfNulls<EnchantmentSetting>(3)
    protected open val enchantingTableContext by lazy { EnchantingTableContext(player, bookShelves, this) }

    // 如果关闭菜单则返回输入框里的所有物品.
    protected abstract val closeHandlers: MutableList<out Any>
    abstract val lineBottoms: List<IMenuEnchantLineItem>
    abstract val bookBottoms: List<IMenuEnchantLineItem>


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
        //
        val conditions = arrayOf(
            overrideConditions[0] ?: conditionLine1,
            overrideConditions[1] ?: conditionLine2,
            overrideConditions[2] ?: conditionLine3
        )

        // 找到第一个不满足条件的栏位
        activeLine = conditions.indexOfFirst { condition ->
            condition.any { !it.check(args) }
        }.let { if (it == -1) 3 else it }

        // 优化状态计算
        lineStatus = calculateLineStatus()
        return activeLine
    }

    // 计算菜单状态逻辑
    protected fun calculateLineStatus(): String {
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


    // 统计 Structure 里有多少个某种 Slot 字符
    protected fun getMarkCount(char: Char) = structureArray.sumOf { it.count { c -> c == char } }


    // 覆写模式
    var overrideConditions: MutableMap<Int, List<ConfigConditionTemplate>> = mutableMapOf()

    fun clearOverrideConditions() {
        overrideConditions.clear()
    }

    /**
     * 根据起源子书的数据
     * 额外覆写
     */
    fun overrideSlot(
        slot: Int,
        actions: List<ConfigActionTemplate>?,
        activeItem: ItemRender?,
        inactiveItem: ItemRender?,
        overrideConditions: List<ConfigConditionTemplate>?
    ) {
        require(slot in 0..2)

        overrideConditions?.let { this.overrideConditions[slot] = it }

        // 行按钮覆写
        lineBottoms[slot].overrideItem(
            actions,
            activeItem,
            inactiveItem,
        )

        // 书按钮覆写
        bookBottoms[slot].offlineRender
        val newActiveItem = if (activeItem != null) {
            bookBottoms[slot].onlineRender.lore = activeItem.lore
            bookBottoms[slot].onlineRender
        } else activeItem

        val newInactiveItem = if (inactiveItem != null) {
            bookBottoms[slot].offlineRender.lore = inactiveItem.lore
            bookBottoms[slot].offlineRender
        } else inactiveItem

        bookBottoms[slot].overrideItem(
            actions,
            newActiveItem,
            newInactiveItem,
        )
    }

}

