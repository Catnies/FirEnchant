package top.catnies.firenchantkt.listener

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.view.AnvilView
import top.catnies.firenchantkt.context.AnvilContext
import top.catnies.firenchantkt.item.FirAnvilItemRegistry
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir

class RecipeListener: Listener {

    @EventHandler(ignoreCancelled = true)
    fun onPrepareAnvilEvent(event: PrepareAnvilEvent){
        if (event.inventory.viewers.isEmpty()) return
        val player = event.inventory.viewers.first() as? Player ?: return
        val firstItem = event.inventory.getItem(0) ?: return
        val secondItem = event.inventory.getItem(1) ?: return
        if (firstItem.nullOrAir() || secondItem.nullOrAir()) return

        // 找到物品对应的处理器
        val anvilApplicable = FirAnvilItemRegistry.instance.findApplicableItem(secondItem) ?: return
        // 构建上下文
        val context = AnvilContext(firstItem, secondItem, event.result, event.view, player)
        // 处理物品
        anvilApplicable.onPrepare(event, context)
    }


    @EventHandler(ignoreCancelled = true)
    fun onPrepareResultEvent(event: PrepareResultEvent) {
        if (event.inventory.viewers.isEmpty()) return
        val player = event.inventory.viewers.first() as? Player ?: return
        val firstItem = event.inventory.getItem(0) ?: return
        val secondItem = event.inventory.getItem(1) ?: return
        if (firstItem.nullOrAir() || secondItem.nullOrAir()) return

        // 找到物品对应的处理器
        val anvilApplicable = FirAnvilItemRegistry.instance.findApplicableItem(secondItem) ?: return
        // 构建上下文
        val anvilView = event.view as AnvilView
        val context = AnvilContext(firstItem, secondItem, event.result, anvilView, player)
        // 处理物品
        anvilApplicable.onPrepareResult(event, context)
    }


    @EventHandler(ignoreCancelled = true)
    fun onCostEvent(event: InventoryClickEvent) {
        if (event.inventory.viewers.isEmpty()) return
        val player = event.inventory.viewers.first() as? Player ?: return
        if (!player.itemOnCursor.nullOrAir()) return // 光标有物品就拒绝处理

        // 铁砧事件
        val anvilView = event.view as? AnvilView
        if (anvilView != null && event.slot == 2) {
            if (player.level < anvilView.repairCost) return // 等级需要超过铁砧的消耗等级, 否则不处理.
            val firstItem = anvilView.getItem(0) ?: return
            val secondItem = anvilView.getItem(1) ?: return
            val resultItem = anvilView.getItem(2) ?: return
            if (firstItem.nullOrAir() || secondItem.nullOrAir() || resultItem.nullOrAir()) return

            // 找到处理器
            val anvilApplicable = FirAnvilItemRegistry.instance.findApplicableItem(secondItem) ?: return
            // 构建上下文
            val context = AnvilContext(firstItem, secondItem, resultItem, anvilView, player)
            // 处理物品
            anvilApplicable.onCost(event, context)
            return
        }

        // 其他事件
    }
}