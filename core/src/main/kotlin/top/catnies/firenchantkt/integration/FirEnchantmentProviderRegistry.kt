package top.catnies.firenchantkt.integration

import org.bukkit.Bukkit
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.api.event.EnchantmentProviderRegisterEvent
import top.catnies.firenchantkt.compatibility.provider.enchantment.AiyatsbusEnchantmentProvider

class FirEnchantmentProviderRegistry private constructor(): EnchantmentProviderRegistry {

    var plugin = FirEnchantPlugin.instance
    var logger = FirEnchantPlugin.instance.logger

    companion object {
        @JvmStatic
        val instance by lazy { FirEnchantmentProviderRegistry().apply {
            load()
        } }

        @JvmStatic
        private val EnchantmentProviderMap = mutableMapOf<String, EnchantmentProvider>()
    }

    fun load() {
        // Plugin Hook
        registerEnchantmentProvider("Aiyatsbus", AiyatsbusEnchantmentProvider())
        ServiceContainer.register(EnchantmentProviderRegistry::class.java, this)
        Bukkit.getPluginManager().callEvent(EnchantmentProviderRegisterEvent(this))
    }

    // EnchantmentProviders
    override fun registerEnchantmentProvider(plugin: String, provider: EnchantmentProvider) = EnchantmentProviderMap.put(plugin.lowercase(), provider)
    override fun unregisterEnchantmentProvider(plugin: String) = EnchantmentProviderMap.remove(plugin)
    override fun getEnchantmentProviders() = EnchantmentProviderMap.values.filter { it.enabled }
    override fun getEnchantmentProvider(plugin: String): EnchantmentProvider? {
        EnchantmentProviderMap[plugin.lowercase()]?.let { if (it.enabled) return it }
        return null
    }
}