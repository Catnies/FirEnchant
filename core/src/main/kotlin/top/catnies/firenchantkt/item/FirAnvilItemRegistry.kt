package top.catnies.firenchantkt.item

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.api.event.anvil.AnvilApplicableItemRegisterEvent
import top.catnies.firenchantkt.item.anvil.*


class FirAnvilItemRegistry : AnvilItemRegistry {

    private val items = mutableListOf<AnvilApplicable>()

    companion object {
        @JvmStatic
        val instance: FirAnvilItemRegistry by lazy {
            FirAnvilItemRegistry().apply {
                load()
            }
        }
    }

    // 初始化注册物品
    private fun load() {
        registerItem(FirVanillaEnchantedBook())
        registerItem(FirEnchantedBook())
        registerItem(FirProtectionRune())
        registerItem(FirEnchantSoul())
        registerItem(FirPowerRune())
        ServiceContainer.register(AnvilItemRegistry::class.java, this)
        Bukkit.getPluginManager().callEvent(AnvilApplicableItemRegisterEvent(this))
    }

    // 重载物品
    fun reload() {
        items.forEach { it.reload() }
    }

    // 注册新的铁砧物品
    override fun registerItem(item: AnvilApplicable): Boolean {
        return items.add(item)
    }

    // 删除铁砧物品
    override fun unregisterItem(item: AnvilApplicable): Boolean {
        return items.remove(item)
    }

    // 获取一个铁砧物品
    @Suppress("UNCHECKED_CAST")
    override fun <T : AnvilApplicable> getItem(applicableClass: Class<T>): T? {
        return items.find { applicableClass.isInstance(it) } as T?
    }

    // 获取所有铁砧物品
    override fun getItems(): List<AnvilApplicable> {
        return items.toList()
    }

    // 找到物品对应的处理器
    override fun findApplicableItem(itemStack: ItemStack): AnvilApplicable? {
        return items.find { it.matches(itemStack) }
    }

}