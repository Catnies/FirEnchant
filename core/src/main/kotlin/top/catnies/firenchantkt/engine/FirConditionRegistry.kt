package top.catnies.firenchantkt.engine

import com.google.common.collect.HashBiMap
import org.bukkit.Bukkit
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.api.event.ConditionRegisterEvent
import top.catnies.firenchantkt.engine.condition.HasItemCondition
import top.catnies.firenchantkt.engine.condition.PermissionCondition
import top.catnies.firenchantkt.engine.condition.logic.*

class FirConditionRegistry private constructor() : ConditionRegistry {

    companion object {
        val instance by lazy {
            FirConditionRegistry().also {
                it.load()
            }
        }
    }

    // 双向映射表, 方便根据 string / class 查找对应的值.
    private val registry = HashBiMap.create<String, Class<out Condition>>()

    fun load() {
        registerCondition("<", LessThenCondition::class.java)
        registerCondition("<=", LessThenOrEqualCondition::class.java)
        registerCondition("==", EqualCondition::class.java)
        registerCondition(">=", GreaterThanOrEqualCondition::class.java)
        registerCondition(">", GreaterThanCondition::class.java)
        registerCondition("contains", ContainsCondition::class.java)
        registerCondition("equals_ignore_case", EqualsIgnoreCaseCondition::class.java)
        registerCondition("equals", EqualsCondition::class.java)
        registerCondition("&&", AndCondition::class.java)
        registerCondition("||", OrCondition::class.java)

        registerCondition("has_item", HasItemCondition::class.java)
        registerCondition("permission", PermissionCondition::class.java)

        ServiceContainer.register(ConditionRegistry::class.java, this)
        Bukkit.getPluginManager().callEvent(ConditionRegisterEvent(this))
    }

    override fun registerCondition(name: String, condition: Class<out Condition>) {
        registry.putIfAbsent(name, condition)
    }

    override fun unregisterCondition(name: String): Boolean {
        return registry.remove(name) != null
    }

    override fun getCondition(name: String): Class<out Condition>? {
        return registry[name]
    }

    override fun getConditionName(condition: Class<out Condition>): String? {
        return registry.inverse()[condition]
    }
}
