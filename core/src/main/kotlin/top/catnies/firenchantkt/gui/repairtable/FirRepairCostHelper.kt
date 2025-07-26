package top.catnies.firenchantkt.gui.repairtable

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.config.RepairTableConfig
import top.catnies.firenchantkt.util.ItemUtils.sumAllEnchantmentCount
import top.catnies.firenchantkt.util.ItemUtils.sumAllEnchantmentLevel
import kotlin.math.ceil

object FirRepairCostHelper{

    /**
     * 获取一件装备修复所需的时间;
     */
    fun getRepairTimeCost(player: Player, item: ItemStack): Long {
        val magnification = getPlayerMagnification(player)
        val config = RepairTableConfig.instance

        val baseCost = when (config.REPAIR_TIMERULE_RULE) {
            "static" -> config.REPAIR_TIMERULE_STATIC_TIME

            "level" -> {
                val level = item.sumAllEnchantmentLevel()
                if (level > 0) level * config.REPAIR_TIMERULE_LEVEL_TIME
                else config.REPAIR_TIMERULE_LEVEL_FALLBACK
            }

            "count" -> {
                val count = item.sumAllEnchantmentCount()
                if (count > 0) count * config.REPAIR_TIMERULE_COUNT_TIME
                else config.REPAIR_TIMERULE_COUNT_FALLBACK
            }

            else -> error("不支持的修复时间规则: ${config.REPAIR_TIMERULE_RULE}")
        }

        return ceil(baseCost * magnification).toLong()
    }


    /**
     * 获取玩家修复装备时的折扣力度
     */
    fun getPlayerMagnification(player: Player): Double {
        RepairTableConfig.instance.REPAIR_MAGNIFICATION_RULE.forEach {
            if (player.hasPermission(it.key)) {
                return it.value
            }
        }
        return 1.0
    }

}