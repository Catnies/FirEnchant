package top.catnies.firenchantkt.config

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_MENU_STRUCTURE_ERROR
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.resource_wrapper.MenuItemData

@Suppress("PropertyName")
class ShowEnchantedBooksConfig private constructor():
    AbstractConfigFile("modules/show_enchantedbooks.yml")
{

    companion object {
        @JvmStatic
        val instance by lazy { ShowEnchantedBooksConfig().apply { loadConfig() } }
    }

    val fallbackMenuStructure = arrayOf(
        "X.......?",
        "IIIIIIIII",
        "IIIIIIIII",
        "IIIIIIIII",
        "IIIIIIIII",
        ".......<>"
    )

    // 菜单
    var MENU_TITLE: String by ConfigProperty("All EnchantedBook Menu")
    var MENU_STRUCTURE_ARRAY: Array<String> by ConfigProperty(fallbackMenuStructure)    // 菜单结构
    var MENU_CONTENT_SLOT: Char by ConfigProperty('I')                                  // 内容槽位

    var PREVIOUS_PAGE_ITEM: MenuItemData? = null
    var NEXT_PAGE_ITEM: MenuItemData? = null
    var MENU_CUSTOM_ITEMS: Set<MenuItemData> by ConfigProperty(mutableSetOf())
    var ENCHANTED_BOOKS: List<EnchantmentSetting> by ConfigProperty(mutableListOf())     // 隐藏不显示的魔咒

    // 加载数据
    override fun loadConfig() {
        MENU_TITLE = config().getString("menu-setting.title", "All EnchantedBook Menu")!!
        try { config().getStringList("menu-setting.structure").toTypedArray()
            .also {
//                Structure(*it);
                MENU_STRUCTURE_ARRAY = it } // 测试合法性然后再赋值
        } catch (exception: Exception) {
            Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_MENU_STRUCTURE_ERROR, fileName) }
        MENU_CONTENT_SLOT = config().getString("menu-setting.content-slot", "I")?.first() ?: 'I'
        // 上一页物品
        config().getConfigurationSection("menu-setting.previous-page")?.also { section ->
            val slot = section.getString("slot")?.first() ?: 'P'
            PREVIOUS_PAGE_ITEM = MenuItemData.getMenuItemDataBySection(section, slot, fileName)
        }
        // 下一页物品
        config().getConfigurationSection("menu-setting.next-page")?.let { section ->
            val slot = section.getString("slot")?.first() ?: 'N'
            NEXT_PAGE_ITEM = MenuItemData.getMenuItemDataBySection(section, slot, fileName)
        }
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
        // 其他配置
        val hideEnchantments = config().getStringList("hide-enchantments")
        ENCHANTED_BOOKS = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
            .fold(mutableListOf()) { acc, enchantment ->
                if (hideEnchantments.contains(enchantment.key.asString())) return@fold acc
                val setting = FirEnchantAPI.getSettingsByData(enchantment.key, enchantment.maxLevel, 0, 0)
                setting?.let { acc.add(it) }
                return@fold acc
            }
    }

}