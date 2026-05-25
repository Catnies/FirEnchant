package top.catnies.firenchantkt.util.resource_wrapper

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.integration.ItemProvider
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_HOOK_ITEM_NOT_FOUND
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_HOOK_ITEM_PROVIDER_NOT_FOUND
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent

/**
 * 物品数据
 */
class ItemStackData(
    val plugin: String,
    val id: String,
    val render: ItemRender
) {
    lateinit var itemProvider: ItemProvider
    lateinit var baseItem: ItemStack

    constructor(section: ConfigurationSection, render: ItemRender) : this (
        section.getString("hooked-plugin") ?: "null",
        section.getString("hooked-id") ?: "null",
        render
    )

    constructor(section: ConfigurationSection) : this (
        section.getString("hooked-plugin") ?: "null",
        section.getString("hooked-id") ?: "null",
        ItemRender(section)
    )

    // 验证物品是否存在, 并生成基础物品缓存;
    fun verifyItem(fileName: String, path: String): Boolean {
        itemProvider = FirEnchantAPI.itemProviderRegistry().getItemProvider(plugin) ?: run {
            Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_HOOK_ITEM_PROVIDER_NOT_FOUND, fileName, path, plugin)
            return false
        }
        baseItem = itemProvider.getItemById(id)
            ?.takeUnless { it.nullOrAir() }
            ?: run {
                Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_HOOK_ITEM_NOT_FOUND, fileName, path, id)
                return false
            }
        return true
    }

    // 获取渲染后的物品
    fun renderItem(ptr: Player? = null, args: Map<String, String> = mutableMapOf()): ItemStack {
        return render.renderItem(baseItem.clone(), args = args)
    }

    // 不走缓存获取基础物品
    fun renderItemNoCache(ptr: Player? = null, args: Map<String, String> = mutableMapOf()): ItemStack {
        return baseItemNoCache().apply { render.renderItem(this, ptr, args) }
    }

    // 不走缓存重新获取基础物品
    fun baseItemNoCache(): ItemStack {
        return itemProvider.getItemById(id)!!
    }

}


