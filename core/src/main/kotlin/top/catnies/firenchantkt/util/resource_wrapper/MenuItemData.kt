package top.catnies.firenchantkt.util.resource_wrapper

import org.bukkit.configuration.ConfigurationSection
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.util.ConfigParser
import top.catnies.firenchantkt.util.YamlUtils.getConfigurationSectionList

/**
 * 菜单按钮事件
 * @param slot 对应的字符
 *
 */
data class MenuItemData(
    val slot: Char,
    val item: ItemStackData,
    val action: List<ConfigActionTemplate>
) {
    companion object {
        // 根据 ConfigurationSection 获取 MenuItemData, 其中Slot手动指定传入;
        fun getMenuItemDataBySection(section: ConfigurationSection, slot: Char, fileName: String = "unknow"): MenuItemData {
            // 使用节点构建物品
            val item = ItemStackData(section).apply {
                verifyItem(fileName, section.currentPath!!)
            }
            // 获取动作节点, 解析动作
            val action = section.getConfigurationSectionList("click-actions")
                .mapNotNull {
                    ConfigParser.parseActionTemplate(it, fileName, "${section.currentPath}.click-actions")
                }
            return MenuItemData(slot, item, action)
        }
    }
}



