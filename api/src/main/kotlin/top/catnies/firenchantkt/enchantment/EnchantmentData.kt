package top.catnies.firenchantkt.enchantment

import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.ItemProvider

/**
 * 原版附魔数据的包装类, 额外记录了一些构建插件附魔书需要的信息.
 * 请不要自行构建此对象, 获取请通过API方法获取已存在的对象.
 */
data class EnchantmentData (
    val originEnchantment: Enchantment,
    val key: Key = originEnchantment.key,
    val maxLevel: Int = originEnchantment.maxLevel,

    val itemProvider: ItemProvider,
    val hookedID: String,

    // 物品的其他属性
    val itemName: String,
    val customName: String?,
    val itemLore: List<String>?,
    val glint: Boolean,

    // 理论上加载和重载时便会填充刷新，不应为空。
    var cacheItem: ItemStack? = null
)