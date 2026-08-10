package top.catnies.firenchantkt.item.brokengear

import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.FirItemProviderRegistry
import top.catnies.firenchantkt.util.resource_wrapper.ItemStackData

// 单一捕捉规则
class BrokenMatchRule(
    val matchIdSorter: MutableList<String> = mutableListOf(), // 存储捕捉的IDs
    val wrapperItem: ItemStackData // 包装的物品
) {

    // 捕捉物品是否是符合规则的
    fun matchItem(item: ItemStack): Boolean {
        matchIdSorter.forEach {
            val split = it.split("|")
            val itemProviderID = if (split.size >= 2) split[0] else "Vanilla"
            val itemProvider = FirItemProviderRegistry.instance.getItemProvider(itemProviderID) ?: return@forEach
            val itemID = if (split.size >= 2) split[1] else it
            if (itemProvider.getIdByItem(item).equals(itemID, true)) return true
        }
        return false
    }

}