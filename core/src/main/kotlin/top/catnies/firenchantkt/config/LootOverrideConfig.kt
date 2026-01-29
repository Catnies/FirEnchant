package top.catnies.firenchantkt.config

import kotlin.math.max
import kotlin.math.min

class LootOverrideConfig private constructor():
    AbstractConfigFile("modules/loot-override.yml")
{

    companion object {
        @JvmStatic
        val instance by lazy { LootOverrideConfig().apply { loadConfig() } }
    }

    var ENABLE: Boolean by ConfigProperty(false) // 是否启用
    var FAILURE_RANGE: IntRange by ConfigProperty(IntRange(20, 60))


    override fun loadConfig() {
        ENABLE = config().getBoolean("enable", false)
        val a = config().getInt("failure-min", 0)
        val b = config().getInt("failure-max", 100)
        FAILURE_RANGE = IntRange(min(a, b), max(a, b))
    }

}
