package top.catnies.firenchantkt.compatibility.provider.item

import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.core.util.Key
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.ItemProvider
import javax.annotation.Nullable

class CraftEngineItemProvider private constructor(
    override val enabled: Boolean
): ItemProvider {
    constructor(): this(Bukkit.getPluginManager().getPlugin("CraftEngine") != null)

    @Nullable
    override fun getItemById(id: String): ItemStack? {
//        return CraftEngineItems.byId(Key.of(id))?.buildItemStack()
        val key = Key.of(id)
        val ceItem = CraftEngineItems.byId(key)
        val bukkitItem = ceItem?.buildBukkitItem()
        return bukkitItem
    }

    @Nullable
    override fun getIdByItem(item: ItemStack): String? {
        return CraftEngineItems.byItemStack(item)?.id().toString()
    }

    override fun toString(): String {
        return "CraftEngine"
    }
}