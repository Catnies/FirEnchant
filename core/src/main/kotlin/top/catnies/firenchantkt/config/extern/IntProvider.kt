package top.catnies.firenchantkt.config.extern

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 工厂类
 * 读取 yaml 转化为 IntProvider 实现类
 */
object IntProviderFactory {
    fun fromNode(yamlValue: Any): IntProvider? {
        val node = Node.adapt(yamlValue)
        return when (val type = node.get("int-provider")) {
            "uniform" -> {
                val min = node.get("min") as? Int
                val max = node.get("max") as? Int
                if (min == null || max == null) null
                else UniformRandomIntProvider(min, max)
            }

            "normal" -> {
                val mean = node.get("mean") as? Double
                val stdDev = node.get("deviation") as? Double
                if (mean == null || stdDev == null) null
                else GaussianIntProvider(mean, stdDev)
            }

            "const" -> {
                val value = node.get("value") as? Int
                if (value == null) null
                else ConstIntProvider(value)
            }

            "weighted" -> {
                val obj = node.get("entries")
                val weightedValueEntries = Node.adapt(obj)
                val list = mutableListOf<WeightedValue>()
                weightedValueEntries.keySet().forEach { vk ->
                    val value = vk.toInt()
                    val weight = weightedValueEntries.get(vk).toString().toInt()
                    list += WeightedValue(
                        weight = weight,
                        value = value
                    )
                }
                WeightedRandomIntProvider(list)
            }

            else -> throw RuntimeException("未知的整型数字提供器类型: $type")
        }
    }
}

// 数提供器接口
interface IntProvider {
    fun value(randomSource: Random? = null): Int
}


// 常数提供器
class ConstIntProvider(val value: Int) : IntProvider {
    override fun value(randomSource: Random?) = value

    override fun toString(): String = "Const($value)"
}

class WeightedValue(
    val weight: Int,
    val value: Int
)

// 加权随机数提供器
class WeightedRandomIntProvider(
    private val weightedValues: List<WeightedValue>,
) : IntProvider {
    private val accumulatedWeights: List<Int>
    private val totalWeight: Int

    init {
        require(weightedValues.isNotEmpty()) { "加权数值不能为空" }
        require(weightedValues.all { it.weight > 0 }) { "权重必须为正整数" }

        accumulatedWeights = weightedValues.runningFold(0) { acc, wv ->
            acc + wv.weight
        }.drop(1)
        totalWeight = accumulatedWeights.last()
    }

    override fun value(randomSource: Random?): Int {
        requireNotNull(randomSource)
        val randomValue = randomSource.nextInt(totalWeight)
        val index = accumulatedWeights.indexOfFirst { it > randomValue }
        return weightedValues[index].value
    }

    override fun toString(): String =
        "WeightedRandom${weightedValues.map { "${it.value}:${it.weight}" }}"
}

/**
 * 均匀随机数提供器
 * i -> [min, max]
 */
class UniformRandomIntProvider(
    private val min: Int,
    private val max: Int,
) : IntProvider {
    init {
        require(min <= max) { "最大值必须大于等于最小值" }
    }

    override fun value(randomSource: Random?) = requireNotNull(randomSource).nextInt(min, max + 1)

    override fun toString(): String = "UniformRandom[$min,$max]"
}

// 高斯（正态）分布随机数提供器
class GaussianIntProvider(
    private val mean: Double,
    private val stdDev: Double,
) : IntProvider {

    init {
        require(stdDev > 0) { "Standard deviation must be positive" }
    }

    override fun value(randomSource: Random?): Int {
        requireNotNull(randomSource)
        val gaussian = randomSource.nextDouble().let { u1 ->
            val u2 = randomSource.nextDouble()
            sqrt(-2.0 * ln(u1)) * cos(2.0 * Math.PI * u2)
        }
        return (mean + gaussian * stdDev).roundToInt()
    }

    override fun toString(): String = "Gaussian(μ=$mean, σ=$stdDev)"
}

