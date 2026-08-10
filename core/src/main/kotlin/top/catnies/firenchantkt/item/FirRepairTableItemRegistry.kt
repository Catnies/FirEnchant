package top.catnies.firenchantkt.item

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.api.event.repairtable.RepairTableApplicableItemRegisterEvent
import top.catnies.firenchantkt.item.repairtable.FirReductionCard


class FirRepairTableItemRegistry : RepairTableItemRegistry {

    private val items = mutableListOf<RepairTableApplicable>()

    companion object {
        @JvmStatic
        val instance: FirRepairTableItemRegistry by lazy {
            FirRepairTableItemRegistry().apply {
                load()
            }
        }
    }

    // 初始化注册物品
    private fun load() {
        registerItem(FirReductionCard())
        ServiceContainer.register(RepairTableItemRegistry::class.java, this)
        Bukkit.getPluginManager().callEvent(RepairTableApplicableItemRegisterEvent(this))
    }

    // 重载物品
    fun reload() {
        items.forEach { it.reload() }
    }

    // 注册新的铁砧物品
    override fun registerItem(item: RepairTableApplicable): Boolean {
        return items.add(item)
    }

    // 删除铁砧物品
    override fun unregisterItem(item: RepairTableApplicable): Boolean {
        return items.remove(item)
    }

    // 获取一个铁砧物品
    @Suppress("UNCHECKED_CAST")
    override fun <T : RepairTableApplicable> getItem(applicableClass: Class<T>): T? {
        return items.find { applicableClass.isInstance(it) } as T?
    }

    // 获取所有铁砧物品
    override fun getItems(): List<RepairTableApplicable> {
        return items.toList()
    }

    // 找到物品对应的处理器
    override fun findApplicableItem(itemStack: ItemStack): RepairTableApplicable? {
        return items.find { it.matches(itemStack) }
    }

}