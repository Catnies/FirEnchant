package top.catnies.firenchantkt.enchantment

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.integration.FirItemProviderRegistry
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_ENCHANTMENT_FILE_ERROR
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_ENCHANTMENT_FILE_ITEM_NOT_FOUND
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_ENCHANTMENT_FILE_PROVIDER_NOT_FOUND
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.ResourceCopyUtils

class FirEnchantmentManager private constructor(): EnchantmentManager {
    val plugin = FirEnchantPlugin.instance
    val logger = plugin.logger
    val provider = FirItemProviderRegistry.instance

    private val enchantments = mutableMapOf<NamespacedKey, EnchantmentData>()

    companion object {
        val instance by lazy { FirEnchantmentManager().also {
            it.load()
        } }
    }


    private fun load() {
        readRegisteredEnchantments()
        ServiceContainer.register(EnchantmentManager::class.java, this)
        ServiceContainer.register(EnchantmentSettingFactory::class.java, FirEnchantmentSettingFactory)
    }

    fun reload() {
        enchantments.clear() // 清理注册的附魔数据
        readRegisteredEnchantments()
    }

    // 获取魔咒数据
    override fun getEnchantmentData(key: String): EnchantmentData? {
        return getEnchantmentData(Key.key(key))
    }
    override fun getEnchantmentData(key: Key): EnchantmentData? {
        return enchantments.keys.find { key.asString() == it.asString() }?.let { enchantments[it] }
    }
    override fun getAllEnchantmentData(): List<EnchantmentData> {
        return enchantments.values.toList()
    }

    // 检查魔咒数据
    override fun hasEnchantment(key: String): Boolean {
        return hasEnchantment(Key.key(key))
    }
    override fun hasEnchantment(key: Key): Boolean {
        return enchantments.keys.find { it.key == key.toString() } != null
    }

    // 从注册表读取魔咒
    private fun readRegisteredEnchantments() {
        val registeredEnchantments = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        registeredEnchantments
            .filterNot { enchantments.containsKey(it.key) }
            .forEach { em ->
                val cfg = searchEnchantmentFile(em)
                // 获取和检查必要值
                val hookedPlugin = cfg.getString("hooked-plugin")
                val hookedID = cfg.getString("hooked-id")
                if (hookedPlugin == null || hookedID == null) {
                    Bukkit.getServer().sendTranslatableComponent(RESOURCE_ENCHANTMENT_FILE_ERROR, em.key.toString())
                    return@forEach
                }
                val itemProvider = provider.getItemProvider(hookedPlugin)
                if (itemProvider == null) {
                    Bukkit.getServer().sendTranslatableComponent(RESOURCE_ENCHANTMENT_FILE_PROVIDER_NOT_FOUND, em.key.toString(), hookedPlugin)
                    return@forEach
                }
                val hookedItem = itemProvider.getItemById(hookedID)
                if (hookedItem.nullOrAir()) {
                    Bukkit.getServer().sendTranslatableComponent(RESOURCE_ENCHANTMENT_FILE_ITEM_NOT_FOUND, em.key.toString(), hookedPlugin)
                    return@forEach
                }
                // 创建魔咒对象
                enchantments[em.key] = EnchantmentData(
                    originEnchantment = em,
                    itemProvider = itemProvider,
                    hookedID = hookedID,
                    itemName = cfg.getString("item-name", "<firenchant:enchantment> <firenchant:level_roman>")!!,
                    customName = cfg.getString("custom-name"),
                    itemLore = cfg.getStringList("item-lore"),
                    glint = cfg.getBoolean("glint", false),
                    cacheItem = hookedItem // 把物品缓存一下
                )
            }
    }

    // 检查并创建魔咒对应的配置文件
    private fun searchEnchantmentFile(enchantment: Enchantment): YamlConfiguration {
        val fileName = enchantment.key.toString().replaceFirst(":", "$")
        val file = plugin.dataFolder.resolve("enchantments").resolve("$fileName.yml")
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            ResourceCopyUtils.copyFile(plugin, "templates/enchantment.yml", file)
        }
        return YamlConfiguration.loadConfiguration(file)
    }
}