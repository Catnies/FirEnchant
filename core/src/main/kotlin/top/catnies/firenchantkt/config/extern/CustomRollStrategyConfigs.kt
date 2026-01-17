package top.catnies.firenchantkt.config.extern

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import top.catnies.firenchantkt.item.enchantingtable.origin_book.RollStrategyData
import kotlin.random.Random


class CustomRollStrategyData(
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
        val costData: EnchantCostData,
        val enchantmentPool: List<EnchantmentConfigData>
    ) {
        companion object {
            fun fromYaml(section: ConfigurationSection): SlotData? {

                val costData: EnchantCostData? = section.getConfigurationSection("cost")
                    ?.let { EnchantCostData.fromYaml(it) }
                val enchantmentPool: List<EnchantmentConfigData> =
                    section.getMapList("enchantment-pool").mapNotNull {
                        EnchantmentConfigData.fromYaml(it)
                    }
                return if (costData == null || enchantmentPool.isEmpty()) null
                else SlotData(costData, enchantmentPool)
            }
        }

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
                    val id = section["id"] as? String
                        ?: run {
                            TODO()
                        }
                    val weight = section["weight"] as? Int
                        ?: run {
                            TODO()
                        }
                    val levelMap = section["level"] as? Map<*, *>
                        ?: run {
                            TODO()
                        }
                    val failureMap = section["failure"] as? Map<*, *>
                        ?: run {
                            TODO()
                        }

                    val enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                    val enchantment = enchantmentRegistry.get(NamespacedKey.fromString(id)!!)
                        ?: run {
                            return null
                        }

                    val level = IntProviderFactory.fromMap(levelMap)
                    val failure = IntProviderFactory.fromMap(failureMap)

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


