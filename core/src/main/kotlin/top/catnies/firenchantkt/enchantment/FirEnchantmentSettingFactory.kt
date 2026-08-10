package top.catnies.firenchantkt.enchantment

import com.saicone.rtag.RtagItem
import org.bukkit.inventory.ItemStack

// 附魔配置工厂类
object FirEnchantmentSettingFactory : EnchantmentSettingFactory {

    // 将物品转换成一个 FirEnchantmentSetting 对象.
    override fun fromItemStack(item: ItemStack): EnchantmentSetting? {
        val tag = RtagItem.of(item)
        val enchantmentKey: String = tag.get("FirEnchant", "Enchantment") ?: return null
        val data = FirEnchantmentManager.instance.getEnchantmentData(enchantmentKey) ?: return null

        // 在FirEnchant 2.0 版本, NBT内的记录是String类型的, 所以需要额外向下兼容处理.
        val level: Int = try {
            tag.get("FirEnchant", "Level") as? Int ?: tag.get<String>("FirEnchant", "Level")?.toInt() ?: return null
        } catch (e: NumberFormatException) {
            return null
        }
        val failure: Int = try {
            tag.get("FirEnchant", "Failure") as? Int ?: tag.get<String>("FirEnchant", "Failure")?.toInt() ?: return null
        } catch (e: NumberFormatException) {
            return null
        }
        val usedDustTime: Int = try {
            tag.get("FirEnchant", "consumedSouls") as? Int ?: tag.get<String>("FirEnchant", "DustTimes")?.toInt() ?: 0
        } catch (e: NumberFormatException) {
            0
        }

        return FirEnchantmentSetting(data, level, failure, usedDustTime)
    }


    // 使用数据构建一个 FirEnchantmentSetting 对象.
    override fun fromData(data: EnchantmentData, level: Int, failure: Int, usedDustTime: Int): EnchantmentSetting {
        return FirEnchantmentSetting(data, level, failure, usedDustTime)
    }

    // 复制一份对象
    override fun fromAnother(setting: EnchantmentSetting): EnchantmentSetting = fromData(
        setting.data, setting.level, setting.failure, setting.consumedSouls
    )
}