package top.catnies.firenchantkt.engine.actions

import org.bukkit.entity.Player
import top.catnies.firenchantkt.engine.AbstractAction
import top.catnies.firenchantkt.engine.ArgumentKey
import top.catnies.firenchantkt.integration.FirItemProviderRegistry

class TakeItemAction(
    args: Map<String, Any?>
) : AbstractAction(args) {

    @ArgumentKey(["player"], autoInject = true)
    private var player: Player? = null

    @ArgumentKey(["hooked-plugin"])
    private lateinit var hookPlugin: String

    @ArgumentKey(["hooked-id", "hooked-item"])
    private lateinit var hookedID: String

    @ArgumentKey(["count"])
    private var count: Int = 0

    override fun execute() {
        val itemProvider = FirItemProviderRegistry.instance.getItemProvider(hookPlugin) ?: return
        var targetCount = count
        player?.inventory?.forEach { itemStack ->
            if (itemStack == null) return@forEach
            if (itemProvider.getIdByItem(itemStack).equals(hookedID, true)) {
                // 如果当前物品数量大于目标数量
                if (itemStack.amount >= targetCount) {
                    itemStack.apply { amount -= targetCount }
                    return
                }
                // 如果当前物品数量不足目标数量
                targetCount -= itemStack.amount
                itemStack.apply { amount = 0 }
            }
            if (targetCount <= 0) return
        }
    }
}