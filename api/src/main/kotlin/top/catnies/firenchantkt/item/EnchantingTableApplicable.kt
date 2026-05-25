package top.catnies.firenchantkt.item

import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.context.EnchantingTableContext
import top.catnies.firenchantkt.gui.wrapper.InventoryPostEventWrapper

interface EnchantingTableApplicable {

    // 创建处理器时执行的操作, 例如绑定检查物品是否存在.
    fun load() {
        return
    }

    // 重载插件时处理器执行的操作, 例如清除更新缓存等.
    fun reload() {
        return
    }

    // 检查给定的 ItemStack 是否匹配这个处理器
    fun matches(itemStack: ItemStack): Boolean {
        return false
    }

    // 当物品尝试放入附魔台时
    fun onPreInput(itemStack: ItemStack, cancelEvent: ()->Unit, context: EnchantingTableContext) {
        return
    }

    // 当物品放入附魔台后
    fun onPostInput(itemStack: ItemStack, eventWrapper: InventoryPostEventWrapper, context: EnchantingTableContext) {
        return
    }

}