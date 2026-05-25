package top.catnies.firenchantkt.invui_v2.item

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import top.catnies.firenchantkt.database.entity.ItemRepairTable
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.AbstractBoundItem
import xyz.xenondevs.invui.item.ItemProvider

class MenuRepairItem(
    val data: ItemRepairTable,
    val period: Int,
    var originItem: ItemStack,
    var showItem: ItemProvider,
    var clickHandler: (Click) -> Boolean,
    var task: BukkitTask? = null
): AbstractBoundItem() {

    fun start() {
        if (task != null) task!!.cancel()
        task = Bukkit.getScheduler()
            .runTaskTimer(InvUI.getInstance().getPlugin(), Runnable { this.notifyWindows() }, 0, period.toLong())
    }

    fun cancel() {
        task!!.cancel()
        task = null
    }

    override fun bind(gui: Gui) {
        super.bind(gui)
        if (task == null) start()
    }

    override fun unbind() {
        super.unbind()
        if (gui.windows.isEmpty() && task != null) cancel()

    }

    override fun getItemProvider(viewer: Player): ItemProvider = showItem

    override fun handleClick(
        clickType: ClickType,
        player: Player,
        click: Click
    ) {
        clickHandler(click)
    }

}