package top.catnies.firenchantkt.util

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.engine.*
import top.catnies.firenchantkt.language.MessageConstants
import top.catnies.firenchantkt.language.MessageConstants.CONFIG_ACTION_INVALID_ARGS
import top.catnies.firenchantkt.language.MessageConstants.CONFIG_CONDITION_INVALID_ARGS
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.YamlUtils.getConfigurationSectionList
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object ConfigParser {

    // 解析Action模板
    fun parseActionTemplate(
        config: ConfigurationSection,
        fileName: String = "none",
        node: String = "none"
    ): ConfigActionTemplate? {
        val type = config.getString("type") ?: return null
        val actionClass = FirEnchantAPI.actionRegistry().getAction(type) ?: run {
            Bukkit.getConsoleSender()
                .sendTranslatableComponent(MessageConstants.CONFIG_ACTION_TYPE_UNKNOWN, fileName, node, type)
            return null
        }

        // 提取静态参数（排除conditions）
        val staticArgs = config.getKeys(false)
            .filter { it != "conditions" }
            .associateWith { config.get(it)!! }
            .toMutableMap()

        // 解析条件
        val conditions = config.getConfigurationSectionList("conditions")
            .mapNotNull { parseConditionTemplate(it, fileName, node) }

        // 验证必需参数
        if (validateRequiredArguments(actionClass, staticArgs, fileName, node)) {
            return ConfigActionTemplate(
                type = type,
                actionClass = actionClass,
                staticArgs = staticArgs,
                conditions = conditions
            )
        }

        return null
    }

    // 解析Condition模板
    fun parseConditionTemplate(
        config: ConfigurationSection,
        fileName: String = "none",
        node: String = "none"
    ): ConfigConditionTemplate? {
        val type = config.getString("type") ?: return null
        val conditionClass = FirEnchantAPI.conditionRegistry().getCondition(type) ?: run {
            Bukkit.getConsoleSender()
                .sendTranslatableComponent(MessageConstants.CONFIG_CONDITION_TYPE_UNKNOWN, fileName, node, type)
            return null
        }

        val staticArgs = config.getKeys(false)
            .filter { it != "conditions" } // 排除嵌套条件字段
            .associateWith { config.get(it)!! }
            .toMutableMap()

        // 处理嵌套条件（如 AndCondition, OrCondition 等）
        val nestedConditions = mutableListOf<ConfigConditionTemplate>()
        // 读取扫描嵌套条件.
        config.getConfigurationSectionList("conditions").forEach { nestedConfig ->
            parseConditionTemplate(nestedConfig, fileName, node)?.let { nestedConditions.add(it) }
        }
        // 如果有嵌套条件，将其添加到静态参数中
        if (nestedConditions.isNotEmpty()) staticArgs["conditions"] = nestedConditions

        if (validateRequiredArguments(conditionClass, staticArgs, fileName, node)) {
            return ConfigConditionTemplate(
                type = type,
                conditionClass = conditionClass,
                staticArgs = staticArgs,
            )
        }

        return null
    }

    // 验证必需参数
    private fun validateRequiredArguments(
        clazz: Class<*>,
        args: Map<String, Any?>,
        fileName: String = "none",
        node: String = "none"
    ): Boolean {
        var isValid = true

        try {
            // 使用 Kotlin 反射获取类的属性
            val kotlinClass = clazz.kotlin
            kotlinClass.declaredMemberProperties.forEach { property ->
                val anno = property.findAnnotation<ArgumentKey>() ?: return@forEach

                // 如果并非自动注入字段 && require == true, 则视为必填参数
                if (!anno.autoInject && anno.required) {
                    val hasArg = anno.args.any { args.containsKey(it) }
                    if (!hasArg) {
                        isValid = false
                        val missingArgs = anno.args.joinToString(" or ")
                        if (Action::class.java.isAssignableFrom(clazz)) Bukkit.getConsoleSender()
                            .sendTranslatableComponent(CONFIG_ACTION_INVALID_ARGS, fileName, node, missingArgs)
                        else if (Condition::class.java.isAssignableFrom(clazz)) Bukkit.getConsoleSender()
                            .sendTranslatableComponent(CONFIG_CONDITION_INVALID_ARGS, fileName, node, missingArgs)
                    }
                }
            }
        } catch (e: Exception) {
            // 如果 Kotlin 反射失败，回退到 Java 反射
            clazz.declaredFields.forEach { field ->
                val anno = field.getAnnotation(ArgumentKey::class.java)
                if (anno != null && !anno.autoInject && anno.required) {
                    val hasArg = anno.args.any { args.containsKey(it) }
                    if (!hasArg) {
                        isValid = false
                        val missingArgs = anno.args.joinToString(" or ")
                        if (Action::class.java.isAssignableFrom(clazz)) Bukkit.getConsoleSender()
                            .sendTranslatableComponent(CONFIG_ACTION_INVALID_ARGS, fileName, node, missingArgs)
                        else if (Condition::class.java.isAssignableFrom(clazz)) Bukkit.getConsoleSender()
                            .sendTranslatableComponent(CONFIG_CONDITION_INVALID_ARGS, fileName, node, missingArgs)
                    }
                }
            }
        }

        return isValid
    }

}