package top.catnies.firenchantkt.invui_v1

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.gui.AbstractFirEnchantMenu
import top.catnies.firenchantkt.invui_v1.ext.wrapTitle
import top.catnies.firenchantkt.invui_v1.item.MenuCustomItem
import top.catnies.firenchantkt.invui_v1.item.MenuEnchantLineItem
import top.catnies.firenchantkt.invui_v1.wrapper.InventoryPostEventWrapperV1
import top.catnies.firenchantkt.item.FirEnchantingTableRegistry
import top.catnies.firenchantkt.util.MessageUtils.renderToComponent
import top.catnies.firenchantkt.util.PlayerUtils.giveOrDropList
import top.catnies.firenchantkt.util.TaskUtils
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.structure.Structure
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer

/**
 * 负责接入invuiV1的部分
 */
class FirEnchantingTableMenu(
    override val player: Player,
    override var bookShelves: Int = 0
) : AbstractFirEnchantMenu(player, bookShelves) {

    // 如果关闭菜单则返回输入框里的所有物品.
    override val closeHandlers: MutableList<Runnable> = mutableListOf(
        Runnable {
            val itemStacks = inputInventory.items.toList().filterNotNull()
            player.giveOrDropList(itemStacks)
        }
    )

    lateinit var gui: Gui
    lateinit var window: Window
    lateinit var inputInventory: VirtualInventory
    override lateinit var lineBottoms: List<MenuEnchantLineItem>
    override lateinit var bookBottoms: List<MenuEnchantLineItem>



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
                applicableItem?.onPreInput(event.newItem!!,
                    {
                        event.isCancelled = true
                    }
                    , enchantingTableContext)
            }
        }
        inputInventory.postUpdateHandler = Consumer { event ->
            // 添加了新物品, 执行检查
            if (event.isAdd || event.isSwap) {
                val applicableItem = FirEnchantingTableRegistry.instance.findApplicableItem(event.newItem!!)
                applicableItem?.onPostInput(
                    event.newItem!!,
                    InventoryPostEventWrapperV1(player, event),
                    enchantingTableContext
                )

            }
            // 移除物品, 重置菜单
            if (event.isRemove) {
                clearEnchantmentMenu()
            }
        }
    }

    // 创建附魔栏物品
    private fun buildLineBottoms() {
        lineBottoms = listOf(
            MenuEnchantLineItem(
                this,
                conditionLine1,
                actionLine1,
                1,
                enchantmentLine1.onlineRender,
                enchantmentLine1.offlineRender,
                false
            ),
            MenuEnchantLineItem(
                this,
                conditionLine2,
                actionLine2,
                2,
                enchantmentLine2.onlineRender,
                enchantmentLine2.offlineRender,
                false
            ),
            MenuEnchantLineItem(
                this,
                conditionLine3,
                actionLine3,
                3,
                enchantmentLine3.onlineRender,
                enchantmentLine3.offlineRender,
                false
            )
        )
        bookBottoms = listOf(
            MenuEnchantLineItem(
                this,
                conditionLine1,
                actionLine1,
                1,
                enchantmentBook1.onlineRender,
                enchantmentBook1.offlineRender,
                true
            ),
            MenuEnchantLineItem(
                this,
                conditionLine2,
                actionLine2,
                2,
                enchantmentBook2.onlineRender,
                enchantmentBook2.offlineRender,
                true
            ),
            MenuEnchantLineItem(
                this,
                conditionLine3,
                actionLine3,
                3,
                enchantmentBook3.onlineRender,
                enchantmentBook3.offlineRender,
                true
            )
        )
    }

    // 创建 GUI & Window
    private fun buildGuiAndWindow() {
        gui = Gui.normal()
            .setStructure(Structure(*structureArray))
            .addIngredient(inputSlot, inputInventory)
            .addIngredient(enchantmentLine1.slot, lineBottoms[0])
            .addIngredient(enchantmentLine2.slot, lineBottoms[1])
            .addIngredient(enchantmentLine3.slot, lineBottoms[2])
            .addIngredient(enchantmentBook1.slot, bookBottoms[0])
            .addIngredient(enchantmentBook2.slot, bookBottoms[1])
            .addIngredient(enchantmentBook3.slot, bookBottoms[2])
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

    // 添加用户自定义物品按钮
    private fun addCustomItems(building: Gui.Builder.Normal) {
        customItems.asSequence()
            .filter { getMarkCount(it.slot) > 0 }
            .forEach { data ->
                val menuCustomItem = MenuCustomItem({ _ -> data.item.renderItem(player) }, data.action)
                building.addIngredient(data.slot, menuCustomItem)
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


    // 刷新附魔栏位
    override fun refreshLine() {
        // 清除覆写
        clearOverrideConditions()
        titleMap[lineStatus]?.let { currentTitle->
            val title = AdventureComponentWrapper(currentTitle.renderToComponent(player))
            window.changeTitle(title)
        }
        lineBottoms.forEach { it.notifyWindows() }
        bookBottoms.forEach { it.notifyWindows() }
    }

    // 获取指定栏位的附魔书数据
    override fun getEnchantmentSettingByLine(line: Int): EnchantmentSetting? {
        require(line in 1..3) { "获取索引只能是1~3." }
        return enchantmentSettings[line - 1]
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



}


