package top.catnies.firenchantkt.config.extern

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.engine.ConfigConditionTemplate
import top.catnies.firenchantkt.item.enchantingtable.origin_book.RollStrategyData
import top.catnies.firenchantkt.util.ConfigParser
import top.catnies.firenchantkt.util.YamlUtils.getConfigurationSectionList
import top.catnies.firenchantkt.util.resource_wrapper.ItemRender
import kotlin.collections.mapNotNull
import kotlin.random.Random


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

    data class SlotData(
        /**
         * 附魔池
         */
        val enchantmentPool: List<EnchantmentConfigData>,
        /**
         * 覆写, 可选, 可附魔结果展示
         */
        val activeItem: ItemRender?,
        /**
         * 覆写, 可选, 不可附魔结果展示
         */
        val inactiveItem: ItemRender?,
        /**
         * 覆写, 可选, 附魔后动作
         */
        val afterEnchantAction: List<ConfigActionTemplate>?,
        /**
         * 覆写, 可选, 附魔前判断
         */
        val conditions: List<ConfigConditionTemplate>?
    ) {
        companion object {
            const val KEY_ACTIONS = "actions"
            const val KEY_ACTIVE_SLOT_ITEM = "active-slot-item"
            const val KEY_INACTIVE_SLOT_ITEM = "inactive-slot-item"
            const val KEY_PRECONDITIONS = "preconditions"

            fun fromYaml(section: ConfigurationSection): SlotData? {
                val actions = section.getConfigurationSectionList(KEY_ACTIONS)
                    .mapNotNull {
                        ConfigParser.parseActionTemplate(it, "TODO", KEY_ACTIONS)
                    }.ifEmpty { null }

                val activeItem = section.getConfigurationSection(KEY_ACTIVE_SLOT_ITEM)?.let {
                    ItemRender(it)
                }

                val inactiveItem = section.getConfigurationSection(KEY_INACTIVE_SLOT_ITEM)?.let {
                    ItemRender(it)
                }

                val conditions = section.getConfigurationSectionList(KEY_PRECONDITIONS)
                .mapNotNull {
                    ConfigParser.parseConditionTemplate(it, "TODO", KEY_PRECONDITIONS)
                }.ifEmpty { null }

                val enchantmentPool: List<EnchantmentConfigData> =
                    section.getMapList("enchantment-pool").mapNotNull {
                        EnchantmentConfigData.fromYaml(it)
                    }
                return if (enchantmentPool.isEmpty()) null
                else SlotData(
                    enchantmentPool,
                    activeItem,
                    inactiveItem,
                    actions,
                    conditions
                    )
            }
        }

        /**
         * 附魔池抽取附魔函数, 抽1
         */
        fun roll(randomSource: Random): EnchantmentConfigData {
            // 累加权重
            val accumulatedWeights = enchantmentPool.runningFold(0) { acc, e ->
                acc + e.weight
            }.drop(1)
            // 总权重
            val totalWeight = accumulatedWeights.last()
            // 随机数
            val randomValue = randomSource.nextInt(totalWeight)
            // 执行抽取
            val index = accumulatedWeights.indexOfFirst { it > randomValue }
            return enchantmentPool[index]
        }


        class EnchantCostData(
            val level: IntProvider = ConstIntProvider(1),
            val lapis: IntProvider = ConstIntProvider(1)
        ) {

            companion object {
                fun fromYaml(section: ConfigurationSection): EnchantCostData? {

                    val level = section.getInt("level").takeIf { section.contains("level") }
                    val lapis = section.getInt("lapis").takeIf { section.contains("lapis") }
                    return if (level == null || lapis == null) null
                    else EnchantCostData(ConstIntProvider(level), ConstIntProvider(lapis))
                }
            }


            fun fulfill(): Boolean {
                TODO()
            }


        }

        data class EnchantmentConfigData(
            val id: String,
            val enchantment: Enchantment,
            val weight: Int,
            private val failure: IntProvider,
            private val level: IntProvider,
            ) {
            companion object {
                fun fromYaml(section: Map<*, *>): EnchantmentConfigData? {
                    // 读取配置键
                    val id = section["id"] as? String
                        ?: run {
                            TODO("缺键处理")
                        }
                    val weight = section["weight"] as? Int
                        ?: run {
                            TODO("缺键处理")
                        }
                    val levelMap = section["level"] as? Map<*, *>
                        ?: run {
                            TODO("缺键处理")
                        }
                    val failureMap = section["failure"] as? Map<*, *>
                        ?: run {
                            TODO("缺键处理")
                        }

                    // 读取服务器上的附魔大全
                    val enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                    // 检测配置中读取的附魔id是否在其中
                    val enchantment = enchantmentRegistry.get(NamespacedKey.fromString(id)!!)
                        ?: run {
                            return null
                        }

                    // 读取等级和失败率的配置, 构建为随机数提供器
                    val level = IntProviderFactory.fromNode(levelMap)
                    val failure = IntProviderFactory.fromNode(failureMap)

                    if (level == null || failure == null) return null

                    return EnchantmentConfigData(
                        id,
                        enchantment,
                        weight,
                        failure,
                        level
                    )
                }
            }

            fun rollFailure(randomSource: Random) : Int {
                return failure.value(randomSource)
            }

            fun rollLevel(randomSource: Random): Int {
                return level.value(randomSource)
            }
        }
    }
}


