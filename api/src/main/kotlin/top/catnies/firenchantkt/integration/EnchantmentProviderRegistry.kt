package top.catnies.firenchantkt.integration

import org.jetbrains.annotations.NotNull
import javax.annotation.Nullable

/**
 * 第三方插件物品注册管理器, 当你监听到事件时, 插件已经将默认支持的EnchantmentProvider已经注册完成.
 */
interface EnchantmentProviderRegistry {

    @Nullable
    fun registerEnchantmentProvider(plugin: String, provider: EnchantmentProvider): EnchantmentProvider?

    @Nullable
    fun unregisterEnchantmentProvider(plugin: String): EnchantmentProvider?

    @NotNull
    fun getEnchantmentProviders(): List<EnchantmentProvider>

    @Nullable
    fun getEnchantmentProvider(plugin: String): EnchantmentProvider?

}