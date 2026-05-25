package top.catnies.firenchantkt.util.resource_wrapper

import org.bukkit.configuration.ConfigurationSection

data class ETMenuItemData(
    val slot: Char,
    val onlineRender: ItemRender,
    val offlineRender: ItemRender,
    val isBookSlot: Boolean
) {
    companion object {
        fun getETMenuItemDataBySection(
            section: ConfigurationSection,
            defaultSlot: Char,
            isBookSlot: Boolean,
            fileName: String = "unknow"
        ): ETMenuItemData {
            // TODO 更优雅的处理检查空异常?
            val slot = section.getString("slot")?.first() ?: defaultSlot
            val onlineRender = ItemRender(section.getConfigurationSection("online")!!)
            val offlineRender = ItemRender(section.getConfigurationSection("offline")!!)
            return ETMenuItemData(slot, onlineRender, offlineRender, isBookSlot)
        }
    }
}