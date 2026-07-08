package top.catnies.firenchantkt.item.brokengear

import com.saicone.rtag.RtagItem
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.config.RepairTableConfig
import top.catnies.firenchantkt.util.ItemUtils.deserializeFromBytes
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.ItemUtils.serializeToBytes
import top.catnies.firenchantkt.util.resource_wrapper.ItemStackData

@Suppress("UnstableApiUsage")
class FirBrokenGear : BrokenGear {

    companion object {
        @JvmStatic
        val instance: FirBrokenGear by lazy {
            FirBrokenGear().also {
                ServiceContainer.register(BrokenGear::class.java, it)
            }
        }
    }

    val config = RepairTableConfig.instance
    val fallback: () -> ItemStackData = { config.BROKEN_FALLBACK_WRAPPER_ITEM!! }
    val matches = config.BROKEN_MATCHES

    override fun isBrokenGear(item: ItemStack?): Boolean {
        if (item.nullOrAir()) return false
        return RtagItem.of(item).hasTag("FirEnchant", "FixType")
    }

    override fun toBrokenGear(item: ItemStack?): ItemStack? {
        if (item.nullOrAir() || isBrokenGear(item)) return null
        val wrapperItem = matches.find { it.matchItem(item) }?.wrapperItem ?: fallback()

        // 保存原物品
        val wrapper = wrapperItem.renderItem()
        val bytes = item.serializeToBytes()
        RtagItem.edit(wrapper) { it.set(bytes, "FirEnchant", "FixType") }

        // 迁移原物品的数据

        item.getData(DataComponentTypes.ITEM_NAME)?.let { wrapper.setData(DataComponentTypes.ITEM_NAME, it) }
//        // 旧版修改lore
//        // 如果原来的物品没有lore, 跳过损坏物品的lore, 否则将损坏物品的lore设置为原来物品的lore
//        item.getData(DataComponentTypes.LORE)?.let { wrapper.setData(DataComponentTypes.LORE, it) }
        // 新版修改lore
        val sourceItemLore = item.lore().orEmpty()
        wrapper.editMeta { wrapperItemMeta ->
            val brokenLore = wrapperItemMeta.lore().orEmpty()
            wrapperItemMeta.lore(sourceItemLore + brokenLore)
        }
        item.getData(DataComponentTypes.ENCHANTMENTS)?.let { wrapper.setData(DataComponentTypes.ENCHANTMENTS, it) }

        // 不可堆叠喵
        return wrapper.apply { setData(DataComponentTypes.MAX_STACK_SIZE, 1) }
    }

    override fun repairBrokenGear(item: ItemStack?): ItemStack? {
        if (!isBrokenGear(item)) return null

        val bytes = RtagItem.of(item).get<ByteArray>("FirEnchant", "FixType") ?: return item
        return bytes.deserializeFromBytes()
    }
}