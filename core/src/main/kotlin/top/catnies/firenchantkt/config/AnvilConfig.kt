package top.catnies.firenchantkt.config

import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.YamlUtils
import top.catnies.firenchantkt.util.resource_wrapper.ItemRender
import top.catnies.firenchantkt.util.resource_wrapper.ItemStackData


@Suppress("PropertyName")
class AnvilConfig private constructor():
    AbstractConfigFile("modules/anvil.yml")
{

    companion object {
        @JvmStatic
        val instance by lazy { AnvilConfig().apply { loadConfig() } }
    }

    /*原版附魔书设置*/
    var VEB_DENY_USE: Boolean by ConfigProperty(false)      // 是否禁用原版附魔书.

    /*附魔书设置*/
    var EB_USE_EXP_COST_MODE: String by ConfigProperty("FIXED")    // 经验值消耗模式
    var EB_USE_EXP_FIXED_VALUE: Int by ConfigProperty(18)         // 固定模式的值
    var EB_FAILURE_CORRECTION_MINMAX_ENABLED: Boolean by ConfigProperty(false)    // 最大最小失败率强制控制功能
    var EB_FAILURE_CORRECTION_MINMAX_MIN: Int by ConfigProperty(15)               // 低于此失败率的附魔书将必定成功
    var EB_FAILURE_CORRECTION_MINMAX_MAX: Int by ConfigProperty(90)               // 高于此失败率的附魔书将必定失败
    var EB_FAILURE_CORRECTION_HISTORY_ENABLE: Boolean by ConfigProperty(false)    // 附魔书的成功/失败历史记录功能
    var EB_BREAK_FAILED_ITEM: Boolean by ConfigProperty(true)                     // 使用附魔书失败时, 是否会使装备进入破损状态?
    var EB_FAIL_BACK_ENABLE: Boolean by ConfigProperty(false)           // 使用附魔书失败时是否给予补偿物品
    var EB_FAIL_BACK_ITEM: ItemStackData? by ConfigProperty(null)       // 使用附魔书失败时给予补偿物品
    var EB_MERGE_FAILURE_INHERITANCE: String by ConfigProperty("DEFAULT")   // 当两本附魔书合并的结果附魔书的失败率处理模式
    var EB_MERGE_EXP_COST_MODE: String by ConfigProperty("FIXED")           // 经验值消耗模式
    var EB_MERGE_EXP_FIXED_VALUE: Int by ConfigProperty(18)                 // 固定模式的值

    /*魔咒之魂设置*/
    var ENCHANT_SOUL_ENABLE: Boolean by ConfigProperty(false)             // 开启魔咒之魂道具
    var ENCHANT_SOUL_ITEM: ItemStackData? by ConfigProperty(null)         // 魔咒之魂的道具
    var ENCHANT_SOUL_EXP: Int by ConfigProperty(3)                        // 魔咒之魂消耗的经验等级
    var ENCHANT_SOUL_REDUCE_FAILURE: Int by ConfigProperty(3)             // 每个魔咒之魂可降低附魔书的失败率
    var ENCHANT_SOUL_MIN_FAILURE: Int by ConfigProperty(5)                // 附魔书最多可以使用魔咒之魂降低到的失败率
    var ENCHANT_SOUL_MAX_USE_SOULS: Int by ConfigProperty(12)             // 附魔书最多可以使用的魔咒之魂数量

    /*保护符文设置*/
    var PROTECTION_RUNE_ENABLE: Boolean by ConfigProperty(false)             // 开启保护符文道具
    var PROTECTION_RUNE_ITEM: ItemStackData? by ConfigProperty(null)         // 保护符文的道具
    var PROTECTION_RUNE_EXP: Int by ConfigProperty(18)                       // 保护符文消耗的经验等级

    /*升级符文设置*/
    var POWER_RUNE_ENABLE: Boolean by ConfigProperty(false)              // 开启升级符文道具
    var POWER_RUNE_BREAK_FAILED_ITEM: Boolean by ConfigProperty(false)   // 升级符文失败时损坏装备
    var POWER_RUNE_EXP: Int by ConfigProperty(25)                        // 升级符文消耗的经验等级

    /*拓展符文设置*/
    var SLOT_RUNE_ENABLE: Boolean by ConfigProperty(false)               // 开启拓展符文道具
    var SLOT_RUNE_ITEM: ItemStackData? by ConfigProperty(null)           // 开启拓展符文道具
    var SLOT_RUNE_EXP: Int by ConfigProperty(12)                         // 拓展符文消耗的经验等级


    override fun loadConfig() {
        /*原版附魔书设置*/
        VEB_DENY_USE = config().getBoolean("vanilla-enchanted-book.deny-use", false)

        /*附魔书设置*/
        EB_USE_EXP_COST_MODE = config().getString("enchanted-book.use-enchanted-book.exp.cost-mode", "FIXED")!!
        EB_USE_EXP_FIXED_VALUE = config().getInt("enchanted-book.use-enchanted-book.exp.fixed-value", 18)
        EB_FAILURE_CORRECTION_MINMAX_ENABLED = config().getBoolean("enchanted-book.use-enchanted-book.failure-correction.min-max-limit.enable", false)
        EB_FAILURE_CORRECTION_MINMAX_MIN = config().getInt("enchanted-book.use-enchanted-book.failure-correction.min-max-limit.min-success", 15)
        EB_FAILURE_CORRECTION_MINMAX_MAX = config().getInt("enchanted-book.use-enchanted-book.failure-correction.min-max-limit.max-failure", 90)
        EB_FAILURE_CORRECTION_HISTORY_ENABLE = config().getBoolean("enchanted-book.use-enchanted-book.failure-correction.history-compensation.enable", false)
        EB_BREAK_FAILED_ITEM = config().getBoolean("enchanted-book.use-enchanted-book.break-failed-item", true)
        EB_FAIL_BACK_ENABLE = config().getBoolean("enchanted-book.use-enchanted-book.fail-back-item.enable", false)
        EB_FAIL_BACK_ITEM = EB_FAIL_BACK_ENABLE.takeIf { it }?.let {
            ItemStackData(config().getConfigurationSection("enchanted-book.use-enchanted-book.fail-back-item")!!, ItemRender()).also { item ->
                if (!item.verifyItem(fileName, "enchanted-book.use-enchanted-book.fail-back-item")) {
                    EB_FAIL_BACK_ENABLE = false
                }
            }
        }
        EB_MERGE_FAILURE_INHERITANCE = config().getString("enchanted-book.use-enchanted-book.failure-inheritance", "DEFAULT")!!
        EB_MERGE_EXP_COST_MODE = config().getString("enchanted-book.use-enchanted-book.exp.cost-mode", "FIXED")!!
        EB_MERGE_EXP_FIXED_VALUE = config().getInt("enchanted-book.use-enchanted-book.exp.fixed-value", 18)

        /*魔咒之魂*/
        ENCHANT_SOUL_ENABLE = config().getBoolean("enchant-soul.enable", false)
        if (ENCHANT_SOUL_ENABLE) {
            ENCHANT_SOUL_ITEM = ItemStackData(config().getConfigurationSection("enchant-soul")!!, ItemRender()).also {
                    if (!it.verifyItem(fileName, "enchant-soul")) {
                        ENCHANT_SOUL_ENABLE = false
                    }
                }
            ENCHANT_SOUL_EXP = config().getInt("enchant-soul.exp", 3)
            ENCHANT_SOUL_REDUCE_FAILURE = config().getInt("enchant-soul.reduce-failure", 3)
            ENCHANT_SOUL_MIN_FAILURE = config().getInt("enchant-soul.min-failure", 5)
            ENCHANT_SOUL_MAX_USE_SOULS = config().getInt("enchant-soul.max-use-souls", 12)
        }

        /*保护符文*/
        PROTECTION_RUNE_ENABLE = config().getBoolean("protection-rune.enable", false)
        if (PROTECTION_RUNE_ENABLE) {
            PROTECTION_RUNE_ITEM = ItemStackData(config().getConfigurationSection("protection-rune")!!, ItemRender()).also {
                    if (!it.verifyItem(fileName, "protection-rune")) {
                        PROTECTION_RUNE_ENABLE = false
                    }
                }
            PROTECTION_RUNE_EXP = config().getInt("protection-rune.exp", 18)
        }

        /*升级符文*/
        POWER_RUNE_ENABLE = config().getBoolean("power-rune.enable", false)
        if (POWER_RUNE_ENABLE) {
            POWER_RUNE_BREAK_FAILED_ITEM = config().getBoolean("power-rune.break-failed-item", false)
            POWER_RUNE_EXP = config().getInt("power-rune.exp", 25)
        }

        /*拓展符文*/
        SLOT_RUNE_ENABLE = config().getBoolean("slot-rune.enable", false)
        if (SLOT_RUNE_ENABLE) {
            SLOT_RUNE_ITEM = ItemStackData(config().getConfigurationSection("slot-rune")!!, ItemRender()).also {
                if (!it.verifyItem(fileName, "slot-rune")) {
                    SLOT_RUNE_ENABLE = false
                }
            }
            SLOT_RUNE_EXP = config().getInt("slot-rune.exp", 12)
        }
    }
}