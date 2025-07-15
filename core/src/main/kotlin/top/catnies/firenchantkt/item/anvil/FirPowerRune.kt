package top.catnies.firenchantkt.item.anvil

import com.saicone.rtag.RtagItem
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.api.event.anvil.PowerRunePreUseEvent
import top.catnies.firenchantkt.api.event.anvil.PowerRuneUseEvent
import top.catnies.firenchantkt.config.AnvilConfig
import top.catnies.firenchantkt.context.AnvilContext
import kotlin.random.Random


class FirPowerRune: PowerRune {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = AnvilConfig.instance
    }

    override fun matches(itemStack: ItemStack): Boolean {
        if (!config.POWER_RUNE_ENABLE) return false
        return getChance(itemStack).takeIf { it > 0 && it <= 100 } != null
    }

    override fun onPrepare(event: PrepareAnvilEvent, context: AnvilContext) {
        val chance = getChance(context.secondItem).takeIf { it > 0 && it <= 100 } ?: return
        // 经验值
        val costExp = config.POWER_RUNE_EXP

        // 事件
        val preUseEvent = PowerRunePreUseEvent(context.viewer, event, costExp, chance, context.firstItem)
        Bukkit.getPluginManager().callEvent(preUseEvent)
        if (preUseEvent.isCancelled) return

        // 显示结果
        event.result = context.firstItem.also { injectContextData(it, preUseEvent.successChance) }
        event.view.repairCost = preUseEvent.costExp
    }

    override fun onCost(event: InventoryClickEvent, context: AnvilContext) {
        val chance = readAndClearContextData(context.result!!).takeIf { it > 0 && it <= 100 } ?: return
        val anvilView = event.view as AnvilView
        val resultItem = context.result?.clone() ?: return
        upgrade(resultItem)

        // 触发事件
        val useEvent = PowerRuneUseEvent(
            context.viewer,
            event,
            context.view,
            context.firstItem,
            isSuccess(chance),
            resultItem
        )
        Bukkit.getPluginManager().callEvent(useEvent)
        if (useEvent.isCancelled) {
            event.isCancelled = true
            return
        }

        event.isCancelled = true
        context.viewer.level -= anvilView.repairCost // 扣除经验值
        anvilView.setItem(0, ItemStack.empty())
        anvilView.setItem(2, ItemStack.empty())

        // 扣除一个强化符文
        if (context.secondItem.amount > 1) context.secondItem.amount -= 1
        else anvilView.setItem(1, ItemStack.empty())

        when {
            // 成功了
            useEvent.isSuccess -> {
                anvilView.setCursor(resultItem)
                context.viewer.playSound(context.viewer.location, "block.anvil.use", 1f, 1f)
            }

            // 失败了, 但是有保护符文
            FirEnchantAPI.hasProtectionRune(resultItem) -> {
                anvilView.setCursor(context.firstItem)
                context.viewer.playSound(context.viewer.location, "block.anvil.destroy", 1f, 1f)
            }

            // 失败了, 没有保护符文
            else -> {
                FirEnchantAPI.toBrokenGear(context.firstItem).let { anvilView.setCursor(it) }
                context.viewer.playSound(context.viewer.location, "block.anvil.destroy", 1f, 1f)
            }

        }


    }

    override fun getChance(item: ItemStack): Int {
        RtagItem.of(item).let {
            return it.get("FirEnchant", "PowerChance") as? Int ?: 0
        }
    }

    override fun canUpgrade(item: ItemStack) = item.enchantments.any { (enchantment, level) -> level < enchantment.maxLevel }

    override fun upgrade(item: ItemStack): Boolean {
        val canUpgradeMap = item.enchantments.filter { (enchantment, level) -> level < enchantment.maxLevel }
        if (canUpgradeMap.isEmpty()) return false
        val entry = canUpgradeMap.entries.elementAt(Random.nextInt(canUpgradeMap.size))
        val newLevel = entry.value + 1
        item.addUnsafeEnchantment(entry.key, newLevel)
        return true
    }

    // 夹带私货, 把部分数据缓存到物品.
    private fun injectContextData(item: ItemStack, successChance: Int) {
        RtagItem.edit(item) { tag ->
            tag.set(successChance, "FirEnchantTempData", "PowerSuccessChance")
        }
    }
    private fun readAndClearContextData(item: ItemStack): Int {
        return RtagItem.of(item).get<Int>("FirEnchantTempData", "PowerSuccessChance")?.also {
            RtagItem.edit(item) { tag ->
                tag.remove("FirEnchantTempData")
            }
        } ?: -1
    }

    // 是否成功
    private fun isSuccess(chance: Int) = Random.nextInt(100) < chance
}