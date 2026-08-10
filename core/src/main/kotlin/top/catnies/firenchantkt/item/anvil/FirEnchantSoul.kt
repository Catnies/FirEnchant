package top.catnies.firenchantkt.item.anvil

import com.saicone.rtag.RtagItem
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.event.anvil.EnchantSoulPreUseEvent
import top.catnies.firenchantkt.api.event.anvil.EnchantSoulUseEvent
import top.catnies.firenchantkt.config.AnvilConfig
import top.catnies.firenchantkt.context.AnvilContext
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.enchantment.FirEnchantmentSettingFactory
import top.catnies.firenchantkt.util.TaskUtils
import kotlin.math.max
import kotlin.math.min

class FirEnchantSoul : EnchantSoul {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = AnvilConfig.instance
    }

    var enabled: Boolean = false
    var soulItem: ItemStack? = null

    init {
        load()
    }

    // 检查配置合法性
    override fun load() {
        enabled = config.ENCHANT_SOUL_ENABLE
        soulItem = null
        if (enabled) {
            soulItem = config.ENCHANT_SOUL_ITEM!!.renderItem()
        }
    }

    override fun reload() = load()

    override fun matches(itemStack: ItemStack): Boolean {
        if (!enabled) return false
        val data = config.ENCHANT_SOUL_ITEM!!
        return data.itemProvider.getIdByItem(itemStack) == data.id
    }

    override fun onPrepare(
        event: PrepareAnvilEvent,
        context: AnvilContext
    ) {
        val setting = FirEnchantmentSettingFactory.fromItemStack(context.firstItem) ?: return
        if (isLowestFailure(setting.failure)) return

        // 计算花费
        val canUseAmount = getCanUseAmount(setting, context.secondItem.amount).also { if (it <= 0) return }
        val costExp = config.ENCHANT_SOUL_EXP * canUseAmount
        val resultSetting = FirEnchantmentSettingFactory.fromAnother(setting).apply {
            failure -= canUseAmount * getPreSoulSubChance()
            consumedSouls += canUseAmount
        }

        // 触发事件
        val useEvent =
            EnchantSoulPreUseEvent(context.viewer, event, canUseAmount, costExp, resultSetting, context.firstItem)
        if (useEvent.isCancelled) return

        // 显示结果
        val resultItem = useEvent.resultSetting.toItemStack().also { injectContextData(it, useEvent.useAmount) }
        TaskUtils.runTaskLater(delay = 0, task = {
            context.view.setItem(2, resultItem)
            context.view.repairCost = useEvent.costExp
        })
    }

    override fun onCost(
        event: InventoryClickEvent,
        context: AnvilContext
    ) {
        val useAmount = context.result?.let { readAndClearContextData(it) }?.takeIf { it > 0 } ?: return
        val anvilView = event.view as AnvilView
        val player = context.viewer

        // 触发事件
        val useEvent = EnchantSoulUseEvent(player, event, anvilView, context.firstItem, useAmount, context.result!!)
        Bukkit.getPluginManager().callEvent(useEvent)
        if (useEvent.isCancelled) {
            event.isCancelled = true
            return
        }

        // 计算使用的物品数量
        event.isCancelled = true
        if (player.gameMode != GameMode.CREATIVE) player.level -= anvilView.repairCost // 扣除经验值, 控制经验值的是 onPrepare 的事件设置.
        anvilView.setItem(0, ItemStack.empty())
        anvilView.setItem(2, ItemStack.empty())
        val resultAmount = context.secondItem.amount - useEvent.useAmount
        if (resultAmount <= 0) event.view.setItem(1, ItemStack.empty())
        else context.secondItem.amount.apply { context.secondItem.amount = resultAmount }

        // 光标给物品
        @Suppress("UsePropertyAccessSyntax")
        anvilView.setCursor(useEvent.resultItem)
        player.playSound(player.location, "block.anvil.use", 1f, 1f)
    }

    // 夹带私货, 把部分数据缓存到物品.
    private fun injectContextData(item: ItemStack, useAmount: Int) {
        RtagItem.edit(item) { tag ->
            tag.set(useAmount, "FirEnchantTempData", "CostSoulsAmount")
        }
    }

    private fun readAndClearContextData(item: ItemStack): Int {
        return RtagItem.of(item).get<Int>("FirEnchantTempData", "CostSoulsAmount")?.also {
            RtagItem.edit(item) { tag ->
                tag.remove("FirEnchantTempData")
            }
        } ?: -1
    }

    // 每个魔咒之魂可以降低附魔书多少失败率
    override fun getPreSoulSubChance(): Int {
        return config.ENCHANT_SOUL_REDUCE_FAILURE
    }

    // 检查概率是否到达了最低的附魔书概率
    override fun isLowestFailure(failure: Int): Boolean {
        return failure <= config.ENCHANT_SOUL_MIN_FAILURE
    }

    // 检查附魔书还能用多少个魔咒之魂
    override fun getReamingCanUse(setting: EnchantmentSetting) =
        max((config.ENCHANT_SOUL_MAX_USE_SOULS - setting.consumedSouls), 0)

    // 计算当前情况最多可以使用多少个魔咒之魂
    private fun getCanUseAmount(setting: EnchantmentSetting, inputAmount: Int): Int {
        val failure = setting.failure
        val reaming = getReamingCanUse(setting).also { if (it <= 0) return 0 } // 如果没有剩余的使用次数则返回.

        val maxCanUse = min(reaming, inputAmount) // 不考虑最低概率限制, 最多可以使用多少个魔咒之魂.
        if ((failure - (maxCanUse * config.ENCHANT_SOUL_REDUCE_FAILURE)) >= config.ENCHANT_SOUL_MIN_FAILURE) return maxCanUse // 如果减去所有概率仍然合法, 那就返回可用的数量

        val canCostFailure = failure - config.ENCHANT_SOUL_MIN_FAILURE // 当前可以继续扣除的概率
        val i = canCostFailure / config.ENCHANT_SOUL_REDUCE_FAILURE // 检查还能使用几个灵魂
        val j = canCostFailure % config.ENCHANT_SOUL_REDUCE_FAILURE // 检查还有没有余数, 有的话再加一个
        return if (j <= 0) return i else i + 1
    }

}