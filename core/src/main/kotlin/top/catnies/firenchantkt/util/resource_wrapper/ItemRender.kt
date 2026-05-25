package top.catnies.firenchantkt.util.resource_wrapper

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.util.MessageUtils.renderToComponent

class ItemRender(
    var itemName: String? = null,
    var lore: List<String>? = null,
    var itemModel: String? = null,
    var customModelData: Int? = null,
    var amount: Int = 1,
    var damage: Int = 0
) {

    constructor(section: ConfigurationSection) : this (
        itemName = section.getString("item-name"),
        lore = section.getStringList("lore"),
        itemModel = section.getString("item-model"),
        customModelData = section.getInt("custom-model-data"),
        amount = section.getInt("amount", 1),
        damage = section.getInt("damage", 0)
    )

    // 渲染物品
    fun renderItem(item: ItemStack, ptr: Player? = null, args: Map<String, String> = mutableMapOf()): ItemStack {
        // 物品名
        itemName?.let {
            val renderedComponent = it.renderToComponent(ptr, args)
                .replaceText { builder ->
                    builder
                        .matchLiteral("{original_name}")
                        .replacement(item.getData(DataComponentTypes.ITEM_NAME))
                }
            item.setData(DataComponentTypes.ITEM_NAME, renderedComponent)
        }
        // 物品描述
        lore?.let {
            if (it.isEmpty()) return@let // 空则返回
            val originalLore = item.getData(DataComponentTypes.LORE)?.lines()
            val resultLore = it.fold(mutableListOf<Component>()) { acc, line ->
                if (line.contains("{original_lore}") && originalLore != null) acc.addAll(originalLore)
                else acc.add(line.renderToComponent(ptr, args))
                acc
            }
            item.setData(DataComponentTypes.LORE, ItemLore.lore(resultLore))
        }
        // 物品模型
        itemModel?.let {
            item.setData(DataComponentTypes.ITEM_MODEL, Key.key(it))
        }
        // 物品模型数据
        customModelData?.let {
            item.editMeta { meta -> meta.setCustomModelData(it) }
        }
        // 物品数量
        amount.takeIf { it > 0 }?.let { item.amount = it }
        // 物品损伤
        damage.takeIf { it > 0 }?.let { item.setData(DataComponentTypes.DAMAGE, it) }

        return item
    }

}