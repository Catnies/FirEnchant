package top.catnies.firenchantkt.item.enchantingtable.origin_book

import org.bukkit.enchantments.Enchantment

/**
 * 记录可在附魔台使用的物品数据类
 */
data class OriginalBookData(
    val hookedPlugin: String,
    val hookedID: String,
    val rollStrategy: RollStrategy,
    val rollStrategyData: RollStrategyData,
)


// 抽取策略
enum class RollStrategy {
    VANILLA, CUSTOM
}

interface RollStrategyData

/**
 * Vanilla 模式下, 附魔的配置
 */
class VanillaRollStrategyData(
    val enchantmentList: Set<Enchantment>
) : RollStrategyData


/**
 * CUSTOM 模式下, 每个行的配置
 */


