package top.catnies.firenchantkt.compatibility.provider.item

import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.integration.ItemProvider
import javax.annotation.Nullable

class MythicMobsItemProvider private constructor(
    override val enabled: Boolean
): ItemProvider {
    constructor(): this(Bukkit.getPluginManager().getPlugin("MythicMobs") != null)

    var mythicBukkit: MythicBukkit? = null

    @Nullable
    override fun getItemById(id: String): ItemStack? {
        if (mythicBukkit == null || mythicBukkit?.isClosed == true) this.mythicBukkit = MythicBukkit.inst()
        return mythicBukkit?.itemManager?.getItemStack(id);
    }
    @Nullable
    override fun getIdByItem(item: ItemStack): String? {
        if (mythicBukkit == null || mythicBukkit?.isClosed == true) this.mythicBukkit = MythicBukkit.inst()
        return mythicBukkit?.itemManager?.getMythicTypeFromItem(item);
    }

    override fun toString(): String {
        return "MythicMobs"
    }

}