package top.catnies.firenchantkt.enchantment

import com.saicone.rtag.RtagItem
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.enchantment.EnchantmentSettingFactory

// 附魔配置工厂类
object FirEnchantmentSettingFactoryImpl: EnchantmentSettingFactory {

    // 将物品转换成一个 FirEnchantmentSetting 对象.
    override fun fromItemStack(item: ItemStack): EnchantmentSetting? {
        val tag = RtagItem.of(item)
        val enchantmentKey: String = tag.get("FirEnchant", "Enchantment") ?: return null
        val data = FirEnchantmentManager.instance.getEnchantmentData(enchantmentKey) ?: return null

        val level: Int = when (val result: Any = tag.get("FirEnchant", "Level")) {
            is Int -> result
            is String -> result.toIntOrNull()
            else -> null
        } ?: return null

        val failure: Int = when (val result: Any = tag.get("FirEnchant", "Failure")) {
            is Int -> result
            is String -> result.toIntOrNull()
            else -> null
        } ?: return null

        val usedDustTime: Int = when (val result: Any = tag.get("FirEnchant", "DustTimes")) {
            is Int -> result
            is String -> result.toIntOrNull()
            else -> null
        } ?: return null

        return FirEnchantmentSetting(data, level, failure, usedDustTime)
    }


    // 使用数据构建一个 FirEnchantmentSetting 对象.
    override fun fromData(data: EnchantmentData, level: Int, failure: Int, usedDustTime: Int): EnchantmentSetting {
        return FirEnchantmentSetting(data, level, failure, usedDustTime)
    }
}