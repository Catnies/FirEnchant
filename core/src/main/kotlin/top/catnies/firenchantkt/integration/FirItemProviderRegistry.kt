package top.catnies.firenchantkt.integration

import org.bukkit.Bukkit
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.api.event.ItemProviderRegisterEvent
import top.catnies.firenchantkt.compatibility.provider.item.CraftEngineItemProvider
import top.catnies.firenchantkt.compatibility.provider.item.ItemsAdderItemProvider
import top.catnies.firenchantkt.compatibility.provider.item.MythicMobsItemProvider
import top.catnies.firenchantkt.compatibility.provider.item.NexoItemProvider
import top.catnies.firenchantkt.compatibility.provider.item.VanillaItemProvider

class FirItemProviderRegistry private constructor(): ItemProviderRegistry {

    var plugin = FirEnchantPlugin.instance
    var logger = FirEnchantPlugin.instance.logger

    companion object {
        @JvmStatic
        val instance by lazy { FirItemProviderRegistry().apply {
            load()
        } }

        @JvmStatic
        private val itemProviderMap = mutableMapOf<String, ItemProvider>()
    }

    fun load() {
        // Item Hook
        registerItemProvider("Vanilla", VanillaItemProvider())
        registerItemProvider("ItemsAdder", ItemsAdderItemProvider())
        registerItemProvider("CraftEngine", CraftEngineItemProvider())
        registerItemProvider("MythicMobs", MythicMobsItemProvider())
        registerItemProvider("Nexo", NexoItemProvider())
        ServiceContainer.register(ItemProviderRegistry::class.java, this)
        Bukkit.getPluginManager().callEvent(ItemProviderRegisterEvent(this))
    }

    // ItemProviders
    override fun registerItemProvider(plugin: String, provider: ItemProvider) = itemProviderMap.put(plugin.lowercase(), provider)
    override fun unregisterItemProvider(plugin: String) = itemProviderMap.remove(plugin)
    override fun getItemProviders() = itemProviderMap.values.filter { it.enabled }
    override fun getItemProvider(plugin: String): ItemProvider? {
        itemProviderMap[plugin.lowercase()]?.let { if (it.enabled) return it }
        return null
    }
}