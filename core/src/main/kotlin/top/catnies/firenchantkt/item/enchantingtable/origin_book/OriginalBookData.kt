package top.catnies.firenchantkt.item.enchantingtable.origin_book

import org.bukkit.enchantments.Enchantment

/**
 * 记录可在附魔台使用的物品数据类
 */
//data class OriginalBookData(
//    val hookedPlugin: String,
//    val hookedID: String,
//    val rollStrategy: RollStrategy,
//    val enchantmentList: Set<Enchantment>
//)
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
class VanillaRollStrategyData : RollStrategyData


/**
 * CUSTOM 模式下, 每个行的配置
 */
class CustomRollStrategyData : RollStrategyData


/**
 * CUSTOM 模式下, 每个魔咒节点
 */
data class EnchantmentRollEntry(
    val enchantment: Enchantment,
    val weight: Int,
    var minLevel: Int = enchantment.startLevel,
    var maxLevel: Int = enchantment.maxLevel,
    var minFailure: Int = -1,
    var maxFailure: Int = -1
)

