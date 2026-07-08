package top.catnies.firenchantkt.compatibility.provider.item

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.ItemProvider
import javax.annotation.Nullable

class ItemsAdderItemProvider private constructor(
    override val enabled: Boolean
): ItemProvider {
    constructor(): this(Bukkit.getPluginManager().getPlugin("ItemsAdder") != null)

    @Nullable
    override fun getItemById(id: String): ItemStack? {
        return CustomStack.getInstance(id)?.itemStack
    }

    @Nullable
    override fun getIdByItem(item: ItemStack): String? {
        return CustomStack.byItemStack(item)?.id
    }

    override fun toString(): String {
        return "ItemsAdder"
    }
}