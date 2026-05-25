package top.catnies.firenchantkt.invui_v1

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.config.ShowEnchantedBooksConfig
import top.catnies.firenchantkt.gui.ShowEnchantedBooksMenu
import top.catnies.firenchantkt.invui_v1.ext.wrapTitle
import top.catnies.firenchantkt.invui_v1.item.MenuCustomItem
import top.catnies.firenchantkt.invui_v1.item.MenuPageItem
import top.catnies.firenchantkt.util.ItemUtils.replacePlaceholder
import top.catnies.firenchantkt.util.TaskUtils
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.gui.structure.Structure
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window
import kotlin.math.max
import kotlin.math.min

class FirShowEnchantedBooksMenu(
    val player: Player
): ShowEnchantedBooksMenu {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val config = ShowEnchantedBooksConfig.instance
    }

    /*配置文件缓存*/
    val title = config.MENU_TITLE
    val structureArray = config.MENU_STRUCTURE_ARRAY
    val contentSlot = config.MENU_CONTENT_SLOT
    val previousPageItem = config.PREVIOUS_PAGE_ITEM
    val nextPageItem = config.NEXT_PAGE_ITEM
    val customItems = config.MENU_CUSTOM_ITEMS
    val enchantedBooks = config.ENCHANTED_BOOKS

    /*构建时对象*/
    lateinit var gui: PagedGui<Item>
    lateinit var window: Window
    lateinit var previousPageBottom: MenuPageItem
    lateinit var nextPageBottom: MenuPageItem

    override fun openMenu(data: Map<String, Any>, async: Boolean) {
        val buildTask = {
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

    // 上一页 和 下一页
    private fun buildPageItem() {
        previousPageItem?.let {
            previousPageBottom = MenuPageItem(false, it.action) { s ->
                if (gui.currentPage == 0) return@MenuPageItem ItemStack.empty()

                val itemStack = previousPageItem.item.renderItem(player)
                itemStack.replacePlaceholder(
                    mutableMapOf(
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
            nextPageBottom = MenuPageItem(true, it.action) { s ->
                if (gui.pageAmount == 0) return@MenuPageItem ItemStack.empty() // 总页数为0代表目前没有正在修复的装备
                if (gui.currentPage == gui.pageAmount - 1) return@MenuPageItem ItemStack.empty() // 如果当前页数 = (总页数 - 1)就代表是最后一页

                val itemStack = nextPageItem.item.renderItem(player)
                itemStack.replacePlaceholder(
                    mutableMapOf(
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
        // 翻页按钮
        previousPageItem?.let { builder.addIngredient(it.slot, previousPageBottom) }
        nextPageItem?.let { builder.addIngredient(it.slot, nextPageBottom) }
        // 菜单内容
        builder.addIngredient(contentSlot, Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.setContent(enchantedBooks.map { SimpleItem(it.toItemStack()) })
        // 自定义物品
        customItems.filter { customItem -> getMarkCount(customItem.slot) > 0 }
            .forEach { data ->
                val menuCustomItem = MenuCustomItem({ _ -> data.item.renderItem(player) }, data.action)
                builder.addIngredient(data.slot, menuCustomItem)
            }
        gui = builder.build()

        window = Window.single {
            it.setViewer(player)
            it.setTitle(title.wrapTitle(player))
            it.setGui(gui)
            it.build()
        }
    }

    // 统计 Structure 里有多少个某种 Slot 字符
    private fun getMarkCount(char: Char) = structureArray.sumOf { it.count { c -> c == char } }
}