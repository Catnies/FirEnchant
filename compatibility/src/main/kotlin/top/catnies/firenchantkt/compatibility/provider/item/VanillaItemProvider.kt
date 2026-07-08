package top.catnies.firenchantkt.compatibility.provider.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.ItemProvider
import javax.annotation.Nullable

class VanillaItemProvider (
    override val enabled: Boolean = true
): ItemProvider {

    @Nullable
    override fun getItemById(id: String): ItemStack? {
        val material = Material.getMaterial(id.uppercase()) ?: return null
        return ItemStack(material)
    }

    @Nullable
    override fun getIdByItem(item: ItemStack): String? {
        return item.type.toString()
    }

    override fun toString(): String {
        return "Vanilla"
    }

}