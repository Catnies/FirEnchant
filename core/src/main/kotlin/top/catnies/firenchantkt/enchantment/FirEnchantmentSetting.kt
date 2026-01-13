package top.catnies.firenchantkt.enchantment

import com.saicone.rtag.RtagItem
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.language.tags.FirEnchantTag
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.MessageUtils.convertLegacyColorToMiniMessage
import top.catnies.firenchantkt.util.MessageUtils.parsePlaceholderAPI

class FirEnchantmentSetting(
    override var data: EnchantmentData,
    override var level: Int,
    override var failure: Int,
    override var consumedSouls: Int = 0
): EnchantmentSetting {

    // 转换成 ItemStack
    override fun toItemStack(): ItemStack {
        // 查找缓存获取物品
        if (data.cacheItem?.nullOrAir() == true) {
            val itemProvider = data.itemProvider
            data.cacheItem = itemProvider.getItemById(data.hookedID) ?:
                    // 不应出现, 在 load 时就检查过.
                    throw IllegalArgumentException("Can't found item ${data.hookedID} from plugin ${data.itemProvider}!")
        }
        var result = data.cacheItem!!.clone()
        result = injectCustomData(result)
        result = renderItem(result)
        return result
    }

    // 注入自定义数据
    @Suppress("UnstableApiUsage")
    private fun injectCustomData(item: ItemStack): ItemStack {
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1)
        RtagItem.of(item). let { tag ->
            tag.set(data.key.asString(), "FirEnchant", "Enchantment")
            tag.set(level, "FirEnchant", "Level")
            tag.set(failure, "FirEnchant", "Failure")
            tag.set(consumedSouls, "FirEnchant", "consumedSouls")
            return tag.load()
        }
    }

    // 渲染物品
    private fun renderItem(item: ItemStack): ItemStack {
        val itemName = data.itemName.parsePlaceholderAPI().convertLegacyColorToMiniMessage().let { MiniMessage.miniMessage().deserialize(it, FirEnchantTag(this)) }
        item.setData(DataComponentTypes.ITEM_NAME, itemName)

        val itemLore = data.itemLore?.parsePlaceholderAPI()?.convertLegacyColorToMiniMessage()?.map { MiniMessage.miniMessage().deserialize(it, FirEnchantTag(this)) }
        itemLore?.let { item.setData(DataComponentTypes.LORE, ItemLore.lore(it)) }

        val customName = data.customName?.parsePlaceholderAPI()?.convertLegacyColorToMiniMessage()?.let { MiniMessage.miniMessage().deserialize(it, FirEnchantTag(this)) }
        customName?.let { item.setData(DataComponentTypes.CUSTOM_NAME, it) }

        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, data.glint)
        return item
    }

}