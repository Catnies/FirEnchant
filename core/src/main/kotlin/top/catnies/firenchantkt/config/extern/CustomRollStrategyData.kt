package top.catnies.firenchantkt.config.extern

import org.bukkit.configuration.ConfigurationSection
import top.catnies.firenchantkt.item.enchantingtable.origin_book.RollStrategyData

/**
 * 自定义策略数据
 */
class CustomRollStrategyData(
    /**
     * 槽位自定义设置
     * 通常是3个, 对应3行附魔槽
     */
    val slotData: List<SlotData?>
) : RollStrategyData {

    companion object {
        fun fromYamlSection(section: ConfigurationSection): CustomRollStrategyData? {
            val slotData1: SlotData? = section.getConfigurationSection("first-slot")?.let { SlotData.fromYaml(it) }
            val slotData2: SlotData? = section.getConfigurationSection("second-slot")?.let { SlotData.fromYaml(it) }
            val slotData3: SlotData? = section.getConfigurationSection("third-slot")?.let { SlotData.fromYaml(it) }
            return if (slotData1 == null && slotData2 == null && slotData3 == null) null
            else CustomRollStrategyData(listOf(slotData1, slotData2, slotData3))
        }
    }

}