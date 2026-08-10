package top.catnies.firenchantkt.engine

import org.bukkit.Bukkit
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.language.MessageConstants.CONFIG_ACTION_RUNTIME_ARGS_CAST_FAIL
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

abstract class AbstractAction(
    open val args: Map<String, Any?>
) : Action {

    init {
        this::class.declaredMemberProperties.forEach { prop ->
            val annotation = prop.findAnnotation<ArgumentKey>() ?: return@forEach

            // 从注解的参数里找到第一个args里有的参数
            val value = annotation.args.firstNotNullOfOrNull { this.args[it] } ?: return@forEach

            // 注入字段
            prop.isAccessible = true
            if (prop is KMutableProperty<*>) {
                try {
                    val convertedValue = convertValue(value, prop.returnType)
                    prop.setter.call(this, convertedValue)
                } catch (e: Exception) {
                    // 如果转换失败，尝试直接注入原值
                    try {
                        prop.setter.call(this, value)
                    } catch (innerE: Exception) {
                        // 记录错误信息
                        val conditionName = FirEnchantAPI.actionRegistry().getActionName(this::class.java)
                        val argKey = prop.findAnnotation<ArgumentKey>()?.args?.joinToString(" , ")
                        Bukkit.getConsoleSender().sendTranslatableComponent(
                            CONFIG_ACTION_RUNTIME_ARGS_CAST_FAIL,
                            conditionName ?: "Unknown",
                            argKey ?: "Unknown"
                        )
                    }
                }
            }
        }
    }

    // 类型转换函数，支持常见类型的转换
    private fun convertValue(value: Any?, targetType: KType): Any? {
        if (value == null) return null

        // 如果类型已经匹配，直接返回
        val targetClass = targetType.classifier as? kotlin.reflect.KClass<*> ?: return value
        if (targetClass.isInstance(value)) return value

        // 支持的类型转换
        return when (targetClass) {
            String::class -> value.toString()
            else -> value // 对于其他类型，尝试直接使用原值
        }
    }
}