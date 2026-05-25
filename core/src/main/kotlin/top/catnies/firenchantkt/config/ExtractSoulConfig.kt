package top.catnies.firenchantkt.config

import org.bukkit.Bukkit
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_MENU_STRUCTURE_ERROR
import top.catnies.firenchantkt.util.ConfigParser
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.YamlUtils.getConfigurationSectionList
import top.catnies.firenchantkt.util.resource_wrapper.ItemStackData
import top.catnies.firenchantkt.util.resource_wrapper.MenuItemData

class ExtractSoulConfig private constructor():
    AbstractConfigFile("modules/extract_soul.yml")
{

    companion object {
        @JvmStatic
        val instance by lazy { ExtractSoulConfig().apply { loadConfig() } }
    }

    val fallbackMenuStructure = arrayOf(
        "X.......?",
        "IIIIIIIII",
        "IIIIIIIII",
        "IIIIIIIII",
        ".........",
        "....O...."
    )

    /*菜单设置*/
    var MENU_TITLE: String by ConfigProperty("Extract Soul Menu")
    var MENU_STRUCTURE_ARRAY: Array<String> by ConfigProperty(fallbackMenuStructure)
    var MENU_INPUT_SLOT: Char by ConfigProperty('I')
    var MENU_OUTPUT_SLOT: Char by ConfigProperty('O')

    var MENU_CUSTOM_ITEMS: Set<MenuItemData> by ConfigProperty(mutableSetOf())
    var MENU_RESULT_ITEM: ItemStackData? = null
    var MENU_RESULT_ITEM_CLICK_ACTIONS: List<ConfigActionTemplate>? by ConfigProperty(null)

    // 加载数据
    override fun loadConfig() {
        // 菜单信息
        MENU_TITLE = config().getString("menu-setting.title", "Extract Soul Menu")!!
        try { config().getStringList("menu-setting.structure").toTypedArray()
            .also {
                // 不测了
//                Structure(*it);
                MENU_STRUCTURE_ARRAY = it
            } // 测试合法性然后再赋值
        } catch (exception: Exception) {
            Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_MENU_STRUCTURE_ERROR, fileName) }
        MENU_INPUT_SLOT = config().getString("menu-setting.input-slot", "I")?.first() ?: 'I'
        MENU_OUTPUT_SLOT = config().getString("menu-setting.output-slot", "O")?.first() ?: 'O'
        // 自定义物品
        config().getConfigurationSection("menu-setting.custom-items")?.let { customItemsSection ->
            val customItems = mutableSetOf<MenuItemData>() // 创建结果列表
            customItemsSection.getKeys(false).forEach { itemSectionKey ->
                // 解析物品节点如 'X', '?' 等节点
                val itemSections = customItemsSection.getConfigurationSection(itemSectionKey) // 这些 key 就是 如 'X', '?' 等
                itemSections?.let { section ->
                    // 保存到结果列表里
                    customItems.add(
                        MenuItemData.getMenuItemDataBySection(section, itemSectionKey.first(), fileName)
                    )
                }
            }
            MENU_CUSTOM_ITEMS = customItems
        }
        // 构建结果物品
        config().getConfigurationSection("extract-item")?.let { section ->
            MENU_RESULT_ITEM = ItemStackData(section).apply { verifyItem(fileName, section.currentPath!!) }
        }
        // 构建结果物品点击事件
        config().getConfigurationSectionList("extract-item.click-actions").let { actionList ->
            MENU_RESULT_ITEM_CLICK_ACTIONS = actionList
                .mapNotNull { actionNode ->
                    ConfigParser.parseActionTemplate(actionNode, fileName, "extract-item.click-actions")
                }
        }
    }

}