package top.catnies.firenchantkt.item.repairtable

import com.saicone.rtag.RtagItem
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.event.repairtable.ReductionCardUseEvent
import top.catnies.firenchantkt.config.RepairTableConfig
import top.catnies.firenchantkt.context.RepairTableContext
import top.catnies.firenchantkt.database.FirConnectionManager
import top.catnies.firenchantkt.database.dao.ItemRepairData
import top.catnies.firenchantkt.database.entity.ItemRepairTable
import kotlin.math.max

class FirReductionCard: ReductionCard {

    override fun matches(itemStack: ItemStack): Boolean {
        val tag = RtagItem.of(itemStack)
        val typeStr = tag.get<String>("FirEnchant", "RepairType") ?: return false
        val type = ReductionType.entries.find { it.name.equals(typeStr, true) } ?: return false

        // 根据不同type采取不同方式读取value，避免类型转换错误
        return when (type) {
            ReductionType.PERCENT -> (tag.get<Number>("FirEnchant", "RepairValue")?.toFloat() != null)
            ReductionType.STATIC -> (tag.get<Number>("FirEnchant", "RepairValue")?.toInt() != null)
        }
    }

    // TODO Click没有事件了 event?.click event: InventoryClickEvent?
    override fun onUse(event: InventoryClickEvent?, context: RepairTableContext) {
        val tag = RtagItem.of(context.cursor)
        val typeStr = tag.get<String>("FirEnchant", "RepairType") ?: return
        val type = ReductionType.entries.find { it.name.equals(typeStr, true) } ?: return

        // 根据具体type安全地读取value
        val value: Double = when (type) {
            ReductionType.PERCENT -> tag.get<Number>("FirEnchant", "RepairValue")?.toDouble() ?: return
            ReductionType.STATIC -> tag.get<Number>("FirEnchant", "RepairValue")?.toInt()?.toDouble() ?: return
        }

        // 广播事件
        event?.let {
            val useEvent = ReductionCardUseEvent(
                context.player,
                event,
                context.cursor,
                context.itemRepairTable,
                type,
                value
            )
            Bukkit.getPluginManager().callEvent(useEvent)
            if (useEvent.isCancelled) return@onUse
        }

        // 根据类型减少修复时间
        when (type) {
            ReductionType.PERCENT -> {
                val itemRepairTable = context.itemRepairTable
                val lng = (context.itemRepairTable.remainingTime * value).toLong()
                context.itemRepairTable.duration -= lng
            }
            ReductionType.STATIC -> {
                val remaining = context.itemRepairTable.remainingTime - (value * 1000L)
                context.itemRepairTable.duration = max(0L, remaining.toLong())
            }
        }

        // 触发额外动作
        RepairTableConfig.instance.REPAIR_QUICK_TRIGGER_ACTION.forEach { action ->
            action.executeIfAllowed(
                mapOf(
                    "player" to context.player,
                    "clickType" to event?.click,
                    "event" to event
                )
            )
        }

        // 减少玩家的光标物品
        context.cursor.apply { amount -= 1 }

        // 更新历史
        refreshItemRepairDataAsync(context.itemRepairTable)
    }

    // 异步记录附魔历史
    private fun refreshItemRepairDataAsync(itemRepairTable: ItemRepairTable) {
        val itemRepairData: ItemRepairData = FirConnectionManager.getInstance().itemRepairData
        itemRepairData.update(itemRepairTable, true)
    }
}