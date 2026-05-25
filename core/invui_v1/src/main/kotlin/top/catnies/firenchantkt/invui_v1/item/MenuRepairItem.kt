package top.catnies.firenchantkt.invui_v1.item

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import top.catnies.firenchantkt.database.entity.ItemRepairTable
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.item.Click
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.SuppliedItem
import xyz.xenondevs.invui.window.AbstractWindow

class MenuRepairItem(
    val data: ItemRepairTable,
    val period: Int,
    var originItem: ItemStack,
    var showItem: ItemProvider,
    var clickHandler: (Click) -> Boolean,
    var task: BukkitTask? = null
): SuppliedItem({showItem}, clickHandler) {

    fun start() {
        if (task != null) task!!.cancel()
        task = Bukkit.getScheduler()
            .runTaskTimer(InvUI.getInstance().getPlugin(), Runnable { this.notifyWindows() }, 0, period.toLong())
    }

    fun cancel() {
        task!!.cancel()
        task = null
    }

    override fun addWindow(window: AbstractWindow?) {
        super.addWindow(window)
        if (task == null) start()
    }

    override fun removeWindow(window: AbstractWindow?) {
        super.removeWindow(window)
        if (windows.isEmpty() && task != null) cancel()
    }

}