package top.catnies.firenchantkt.config.extern

import org.bukkit.configuration.ConfigurationSection
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
    fun fromYaml(section: ConfigurationSection): IntProvider? {
        return build(MapLike.adapt(section))
    }

    fun fromMap(map: Map<*, *>): IntProvider? {
        return build(MapLike.adapt(map))
    }

    fun build(mapLike: MapLike?): IntProvider? {
        if (mapLike == null) return null
        val type = mapLike.get("int-provider")
        return when (type) {
            "uniform" -> {
                val min = mapLike.get("min") as? Int
                val max = mapLike.get("max") as? Int
                if (min == null || max == null) null
                else UniformRandomIntProvider(min, max)
            }
            "normal" -> {
                val mean = mapLike.get("mean") as? Double
                val stdDev = mapLike.get("deviation") as? Double
                if (mean == null || stdDev == null) null
                else GaussianIntProvider(mean, stdDev)
            }
            "const" -> {
                val value = mapLike.get("value") as? Int
                if (value == null ) null
                else ConstIntProvider(value)
            }
            // TODO
//            "weighted"-> {
//                val list =  mapLike.get("values") as? List<>
//                if ()
//                    elsse
//                WeightedRandomIntProvider(list)
//            }
            else -> null
        }
    }
}

// 数提供器接口
interface IntProvider {
    fun value(randomSource: Random? = null): Int
}



// 常数提供器
class ConstIntProvider(val value: Int): IntProvider {
    override fun value(randomSource: Random?) = value

    override fun toString(): String = "Const($value)"
}

// 加权随机数提供器
class WeightedRandomIntProvider (
    private val weightedValues: List<Pair<Int, Int>>,
) : IntProvider {
    private val accumulatedWeights: List<Int>
    private val totalWeight: Int

    init {
        require(weightedValues.isNotEmpty()) { "Weighted values list cannot be empty" }
        require(weightedValues.all { it.second > 0 }) { "All weights must be positive" }

        accumulatedWeights = weightedValues.runningFold(0) { acc, (_, weight) ->
            acc + weight
        }.drop(1)
        totalWeight = accumulatedWeights.last()
    }

    override fun value(randomSource: Random?): Int {
        requireNotNull(randomSource)
        val randomValue = randomSource.nextInt(totalWeight)
        val index = accumulatedWeights.indexOfFirst { it > randomValue }
        return weightedValues[index].first
    }

    override fun toString(): String =
        "WeightedRandom${weightedValues.map { "${it.first}:${it.second}" }}"
}

/**
 * 均匀随机数提供器
 * i -> [min, max]
  */
class UniformRandomIntProvider (
    private val min: Int,
    private val max: Int,
) : IntProvider {
    init {
        require(min <= max) { "Min must be less than or equal to max" }
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

