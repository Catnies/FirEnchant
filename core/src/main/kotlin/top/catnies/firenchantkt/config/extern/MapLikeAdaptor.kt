package top.catnies.firenchantkt.config.extern

import org.bukkit.configuration.ConfigurationSection

/**
 * 类图接口
 * 旨在统一适配 Map 和 ConfigurationSection
 */
interface MapLike {
    fun get(key: String): Any?

    fun keySet(): Set<String>

    fun values(): Collection<Any?>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun adapt(obj: Any?): MapLike? {
            return when(obj) {
                is ConfigurationSection -> SectionAdaptor(obj)
                is Map<*, *> -> MapAdaptor(obj as Map<String, *>)
                else -> null
            }
        }
    }
}

/**
 *
 */
class SectionAdaptor(val section: ConfigurationSection): MapLike {
    override fun get(key: String): Any? {
        return section.get(key)
    }

    override fun keySet(): Set<String> {
        return section.getKeys(false)
    }

    override fun values(): Collection<Any?> {
        return section.getValues(false).values
    }

}

class MapAdaptor(val map: Map<String, *>): MapLike {
    override fun get(key: String): Any? {
        return map[key]
    }

    override fun keySet(): Set<String> {
        return map.keys
    }

    override fun values(): Collection<Any?> {
        return map.values
    }
}