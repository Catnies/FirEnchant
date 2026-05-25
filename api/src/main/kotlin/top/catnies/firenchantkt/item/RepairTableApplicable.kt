package top.catnies.firenchantkt.item

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.context.RepairTableContext


interface RepairTableApplicable {

    // 创建处理器时执行的操作, 例如绑定检查物品是否存在.
    fun load() {
        return
    }

    // 重载插件时处理器执行的操作, 例如清除更新缓存等.
    fun reload() {
        return
    }

    // 检查给定的 ItemStack 是否匹配这个处理器.
    fun matches(itemStack: ItemStack): Boolean {
        return false
    }

    // 当光标持该物品点击正在修复中的物品时触发.
    fun onUse(event: InventoryClickEvent?, context: RepairTableContext) {
        return
    }

}