package top.catnies.firenchantkt.item.anvil

import com.saicone.rtag.RtagItem
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.event.anvil.ProtectionRunePreUseEvent
import top.catnies.firenchantkt.api.event.anvil.ProtectionRuneUseEvent
import top.catnies.firenchantkt.config.AnvilConfig
import top.catnies.firenchantkt.context.AnvilContext
import top.catnies.firenchantkt.util.TaskUtils

class FirProtectionRune : ProtectionRune {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = AnvilConfig.instance
    }

    var enabled: Boolean = false
    var runeItem: ItemStack? = null

    init {
        load()
    }

    // 检查配置合法性
    override fun load() {
        enabled = config.PROTECTION_RUNE_ENABLE
        runeItem = null
        if (enabled) {
            runeItem = config.PROTECTION_RUNE_ITEM!!.renderItem()
        }
    }

    override fun reload() = load()

    override fun hasProtectionRune(item: ItemStack): Boolean {
        if (!enabled) return false // 如果功能未开启就均视为没有保护.
        RtagItem.of(item).let { tag ->
            return tag.get<String>("FirEnchant", "HasProtection") == "yes"
        }
    }

    override fun removeProtectionRune(item: ItemStack): Boolean {
        var success = false
        RtagItem.edit(item) { tag ->
            if (tag.hasTag("FirEnchant", "HasProtection")) {
                tag.remove("FirEnchant", "HasProtection")
                success = true
            }
        }
        return success
    }

    override fun addProtectionRune(item: ItemStack): Boolean {
        var success = false
        RtagItem.edit(item) { tag ->
            if (tag.get<String>("FirEnchant", "HasProtection") != "yes") {
                tag.set("yes", "FirEnchant", "HasProtection")
                success = true
            }
        }
        return success
    }

    override fun matches(itemStack: ItemStack): Boolean {
        if (!enabled) return false
        val data = config.PROTECTION_RUNE_ITEM!!
        return data.itemProvider.getIdByItem(itemStack) == data.id
    }

    override fun onPrepare(
        event: PrepareAnvilEvent,
        context: AnvilContext
    ) {
        if (hasProtectionRune(context.firstItem)) return

        // 经验值
        val costExp = config.PROTECTION_RUNE_EXP

        // 触发事件
        val protectionRunePreUseEvent = ProtectionRunePreUseEvent(context.viewer, event, costExp, context.firstItem)
        Bukkit.getPluginManager().callEvent(protectionRunePreUseEvent)
        if (protectionRunePreUseEvent.isCancelled) return

        // 显示结果
        TaskUtils.runTaskLater(delay = 0, task = {
            context.view.setItem(2, context.firstItem.clone().also { addProtectionRune(it) })
            context.view.repairCost = costExp
        })
    }

    override fun onCost(
        event: InventoryClickEvent,
        context: AnvilContext
    ) {
        val anvilView = event.view as AnvilView
        val player = context.viewer

        // 触发事件
        val protectionRuneUseEvent =
            ProtectionRuneUseEvent(player, event, event.view as AnvilView, context.firstItem, context.result!!)
        Bukkit.getPluginManager().callEvent(protectionRuneUseEvent)
        if (protectionRuneUseEvent.isCancelled) {
            event.isCancelled = true
            return
        }

        event.isCancelled = true
        if (player.gameMode != GameMode.CREATIVE) player.level -= anvilView.repairCost // 扣除经验值
        anvilView.setItem(0, ItemStack.empty())
        anvilView.setItem(2, ItemStack.empty())

        // 扣除一个保护符文
        if (context.secondItem.amount > 1) context.secondItem.amount -= 1
        else anvilView.setItem(1, ItemStack.empty())

        // 设置结果
        anvilView.setCursor(context.result)
        player.playSound(player.location, "block.anvil.use", 1f, 1f)
    }

}