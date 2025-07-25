package top.catnies.firenchantkt.item

import org.bukkit.inventory.ItemStack

class FirEnchantingTableRegistry: EnchantingTableRegistry {

    private val items = mutableListOf<EnchantingTableApplicable>()

    companion object {
        val instance: FirEnchantingTableRegistry by lazy { FirEnchantingTableRegistry().also {
            it.load()
        } }
    }

    fun load() {

    }

    override fun registerItem(item: EnchantingTableApplicable): Boolean {
        return items.add(item)
    }

    override fun unregisterItem(item: EnchantingTableApplicable): Boolean {
        return items.remove(item)
    }

    override fun getItems(): List<EnchantingTableApplicable> {
        return items.toList()
    }

    override fun findApplicableItem(itemStack: ItemStack): EnchantingTableApplicable? {
        return items.find { it.matches(itemStack) }
    }
}