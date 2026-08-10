package top.catnies.firenchantkt.util

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.Yaml
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_HOOK_ITEM_NOT_FOUND
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_HOOK_ITEM_PROVIDER_NOT_FOUND
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import java.io.StringReader

object YamlUtils {

    // 将 ListMap 转换成 List<YamlConfiguration>
    fun ConfigurationSection?.getConfigurationSectionList(key: String): List<YamlConfiguration> {
        val data: List<*> = this?.getList(key) ?: return ArrayList()
        return data.map { Yaml().dump(it) }
            .map { StringReader(it) }
            .map { YamlConfiguration.loadConfiguration(it) }
    }

    // 尝试构建物品
    fun tryBuildItem(provider: String?, id: String?, fileName: String, path: String): ItemStack? {
        if (provider == null) {
            Bukkit.getConsoleSender()
                .sendTranslatableComponent(RESOURCE_HOOK_ITEM_PROVIDER_NOT_FOUND, fileName, path, "null")
            return null
        }
        val itemProvider = FirEnchantAPI.itemProviderRegistry().getItemProvider(provider)
        if (itemProvider == null) {
            Bukkit.getConsoleSender()
                .sendTranslatableComponent(RESOURCE_HOOK_ITEM_PROVIDER_NOT_FOUND, fileName, path, provider)
            return null
        }
        if (id == null) {
            Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_HOOK_ITEM_NOT_FOUND, fileName, path, "null")
            return null
        }
        val result = itemProvider.getItemById(id)
        if (result.nullOrAir()) {
            Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_HOOK_ITEM_NOT_FOUND, fileName, path, id)
            return null
        }
        return result
    }

}