package top.catnies.firenchantkt.config.extern

import org.bukkit.configuration.ConfigurationSection

/**
 * 类图接口
 * 旨在统一适配 Map 和 ConfigurationSection
 */
interface Node {
    fun get(key: String): Any?

    fun keySet(): Set<String>

    fun values(): Collection<Any?>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun adapt(obj: Any?): Node {
             val mapLike = when(obj) {
                is ConfigurationSection -> SectionNode(obj)
                is Map<*, *> -> {
                    val stringKeyMap = obj.entries.associate { it.key.toString() to it.value }
                    MapNode(stringKeyMap)
                }
                else -> throw IllegalArgumentException("不支持的类型!此节点应为ConfigurationSection或Map类")
            }
            return mapLike
        }
    }
}

/**
 *
 */
class SectionNode(private val node: ConfigurationSection): Node {
    override fun get(key: String): Any? {
        return node.get(key)
    }

    override fun keySet(): Set<String> {
        return node.getKeys(false)
    }

    override fun values(): Collection<Any?> {
        return node.getValues(false).values
    }

}

class MapNode(private val node: Map<String, *>): Node {
    override fun get(key: String): Any? {
        return node[key]
    }

    override fun keySet(): Set<String> {
        return node.keys
    }

    override fun values(): Collection<Any?> {
        return node.values
    }
}