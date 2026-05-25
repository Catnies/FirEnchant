package top.catnies.firenchantkt.item.anvil

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.api.event.anvil.EnchantedBookMergeEvent
import top.catnies.firenchantkt.api.event.anvil.EnchantedBookPreMergeEvent
import top.catnies.firenchantkt.api.event.anvil.EnchantedBookPreUseEvent
import top.catnies.firenchantkt.api.event.anvil.EnchantedBookUseEvent
import top.catnies.firenchantkt.config.AnvilConfig
import top.catnies.firenchantkt.context.AnvilContext
import top.catnies.firenchantkt.database.FirCacheManager
import top.catnies.firenchantkt.database.FirConnectionManager
import top.catnies.firenchantkt.database.dao.EnchantLogData
import top.catnies.firenchantkt.database.entity.AnvilEnchantLogTable
import top.catnies.firenchantkt.enchantment.EnchantmentSetting
import top.catnies.firenchantkt.enchantment.FirEnchantmentSettingFactory
import top.catnies.firenchantkt.language.MessageConstants.ANVIL_ENCHANTED_BOOK_USE_FAIL
import top.catnies.firenchantkt.language.MessageConstants.ANVIL_ENCHANTED_BOOK_USE_FAIL_BREAK
import top.catnies.firenchantkt.language.MessageConstants.ANVIL_ENCHANTED_BOOK_USE_PROTECT_FAIL
import top.catnies.firenchantkt.util.ItemUtils.addRepairCost
import top.catnies.firenchantkt.util.ItemUtils.isCompatibleWithEnchantment
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.PlayerUtils.giveOrDrop
import top.catnies.firenchantkt.util.TaskUtils
import kotlin.math.max
import kotlin.math.min

class FirEnchantedBook : EnchantedBook {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = AnvilConfig.instance
    }

    val enchantLogData: EnchantLogData = FirConnectionManager.getInstance().enchantLogData
    val cacheManager: FirCacheManager = FirCacheManager.getInstance()
    var enabled: Boolean = false
    var failBackItem: ItemStack? = null

    init {
        load()
    }

    // 检查部分配置
    override fun load() {
        enabled = config.EB_FAIL_BACK_ENABLE
        failBackItem = null
        if (enabled) {
            failBackItem = config.EB_FAIL_BACK_ITEM!!.renderItem()
        }
    }

    override fun reload() = load()

    override fun matches(itemStack: ItemStack): Boolean {
        FirEnchantmentSettingFactory.fromItemStack(itemStack) ?: return false
        return true
    }

    override fun onPrepare(event: PrepareAnvilEvent, context: AnvilContext) {
        val firstSetting = FirEnchantmentSettingFactory.fromItemStack(context.firstItem)
        val setting = FirEnchantmentSettingFactory.fromItemStack(context.secondItem)!!
        val repairCost = context.firstItem.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0)!!
        val repairCost2 = context.secondItem.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0)!!
        val originEnchantment = setting.data.originEnchantment

        when {
            // 如果第一件物品也是附魔书, 触发合并逻辑.
            isEnchantedBookMerge(firstSetting, setting) -> {
                // 计算结果
                val resultSetting = FirEnchantmentSettingFactory.fromAnother(firstSetting!!).apply {
                    failure = getMergeEnchantmentFailureRate(firstSetting, setting)
                    level++
                }

                // 计算经验消耗
                val costExp = if (config.EB_MERGE_EXP_COST_MODE.equals("FIXED", true)) config.EB_MERGE_EXP_FIXED_VALUE
                else getCost(resultSetting, resultSetting.level, repairCost, repairCost2)

                // 触发事件
                val mergeEvent = EnchantedBookPreMergeEvent(context.viewer, event, costExp, firstSetting, setting, resultSetting)
                Bukkit.getPluginManager().callEvent(mergeEvent)
                if (mergeEvent.isCancelled) return

                // 显示结果, 添加 RepairCost.
                TaskUtils.runTaskLater(delay = 0, task =  {
                    context.view.setItem(2, resultSetting.toItemStack().apply { this.addRepairCost() })
                    context.view.repairCost = mergeEvent.costExp
                })

            }

            // 如果第一件物品是普通物品, 触发使用逻辑.
            context.firstItem.isCompatibleWithEnchantment(originEnchantment, setting.level) -> {
                // 计算目标结果等级
                val oldLevel = context.firstItem.getEnchantmentLevel(originEnchantment)
                val targetLevel = if (oldLevel != 0 && oldLevel == setting.level) setting.level + 1 else setting.level

                // 计算经验消耗
                val costExp = if (config.EB_USE_EXP_COST_MODE.equals("FIXED", true)) config.EB_USE_EXP_FIXED_VALUE
                else getCost(setting, targetLevel, repairCost, repairCost2)
                val resultItem = context.firstItem.clone().apply {
                    setData(DataComponentTypes.REPAIR_COST, repairCost * 2 + 1)
                    addUnsafeEnchantment(originEnchantment, targetLevel)
                }

                // 触发事件
                val useEvent = EnchantedBookPreUseEvent(context.viewer, event, costExp, context.firstItem, setting, resultItem)
                Bukkit.getPluginManager().callEvent(useEvent)
                if (useEvent.isCancelled) return

                // 显示结果
                TaskUtils.runTaskLater(delay = 0, task =  {
                    context.view.setItem(2, useEvent.resultItem.apply { this.addRepairCost() })
                    context.view.repairCost = useEvent.costExp
                })
            }
        }
    }

    override fun onCost(event: InventoryClickEvent, context: AnvilContext) {
        val firstSetting = FirEnchantmentSettingFactory.fromItemStack(context.firstItem)
        val setting = FirEnchantmentSettingFactory.fromItemStack(context.secondItem)!!
        val originEnchantment = setting.data.originEnchantment
        val resultItem = context.result!!
        val anvilView = event.view as AnvilView

        when {
            // 如果第一件物品也是附魔书, 触发合并逻辑.
            isEnchantedBookMerge(firstSetting, setting) -> {
                // 触发事件
                val mergeEvent = EnchantedBookMergeEvent(
                    context.viewer,
                    event, anvilView,
                    firstSetting!!,
                    setting,
                    resultItem
                )
                Bukkit.getPluginManager().callEvent(mergeEvent)
                if (mergeEvent.isCancelled) {
                    event.isCancelled = true
                    return
                }
            }

            // 如果第一件物品是普通物品, 检查物品是否可以附魔.
            context.firstItem.isCompatibleWithEnchantment(originEnchantment, setting.level) -> {
                val player = context.viewer

                // 触发事件
                val useEvent = EnchantedBookUseEvent(
                    player, event, anvilView, context.firstItem, setting, resultItem,
                    isSuccess(player, setting, anvilView.repairCost)
                )
                Bukkit.getPluginManager().callEvent(useEvent)
                if (useEvent.isCancelled) {
                    event.isCancelled = true
                    return
                }

                // 记录数据
                AnvilEnchantLogTable().apply {
                    this.player = player.uniqueId
                    usedEnchantment = originEnchantment.key.asString()
                    usedEnchantmentLevel = setting.level
                    takeLevel = anvilView.repairCost
                    failure = setting.failure.toShort()
                    isSuccess = useEvent.isSuccess
                    timestamp = System.currentTimeMillis()
                }.let { saveToDatabaseAndCache(it) }

                // 成功直接设置物品, 然后取消物品
                if (useEvent.isSuccess) {
                    player.setItemOnCursor(context.result)
                    event.isCancelled = true
                }
                // 失败逻辑
                else {
                    event.isCancelled = true
                    if (player.gameMode != GameMode.CREATIVE) player.level -= anvilView.repairCost // 扣除经验值
                    anvilView.setItem(1, ItemStack.empty())
                    anvilView.setItem(2, ItemStack.empty())

                    // 返回失败补偿物品
                    if (enabled) anvilView.setCursor(failBackItem)

                    // 没开启破坏装备
                    if (!config.EB_BREAK_FAILED_ITEM) {
                        player.playSound(player.location, "block.anvil.destroy", 1f, 1f)
                        player.sendTranslatableComponent(ANVIL_ENCHANTED_BOOK_USE_FAIL)
                        return
                    }

                    // 开启了破坏装备 & 有保护符文
                    if (FirEnchantAPI.hasProtectionRune(context.firstItem)) {
                        FirEnchantAPI.removeProtectionRune(context.firstItem)
                        player.playSound(player.location, "block.anvil.destroy", 1f, 1f)
                        player.sendTranslatableComponent(ANVIL_ENCHANTED_BOOK_USE_PROTECT_FAIL)
                        return
                    }

                    // 开启了破坏装备 & 没有保护符文
                    val brokenEquipment = FirEnchantAPI.toBrokenGear(context.firstItem)
                    TaskUtils.runTaskLater(delay = 1L, task = {
                        anvilView.setItem(0, brokenEquipment)
                    })
                    player.playSound(player.location, "block.anvil.destroy", 1f, 1f)
                    player.sendTranslatableComponent(ANVIL_ENCHANTED_BOOK_USE_FAIL_BREAK)
                }
            }
        }
    }

    // 检查是否是两本附魔书合并的情况
    private fun isEnchantedBookMerge(firstSetting: EnchantmentSetting?, secondSetting: EnchantmentSetting?): Boolean {
        if (firstSetting == null || secondSetting == null) return false // 第一个物品可转换成附魔配置.
        if (secondSetting.data.originEnchantment != firstSetting.data.originEnchantment) return false // 两个魔咒应当相同
        if (firstSetting.level >= firstSetting.data.maxLevel) return false // 第一本附魔书应当小于最大等级, 否则无法升级.
        if (secondSetting.level != firstSetting.level) return false // 第一本和第二本附魔书应当等级相同.
        return true
    }

    // 根据模式计算合并附魔书时最终附魔书的失败率
    private fun getMergeEnchantmentFailureRate(
        firstSetting: EnchantmentSetting,
        secondSetting: EnchantmentSetting
    ): Int {
        return when (config.EB_MERGE_FAILURE_INHERITANCE) {
            "HIGHER" -> max(firstSetting.failure, secondSetting.failure)
            "LOWER" -> min(firstSetting.failure, secondSetting.failure)
            else -> firstSetting.failure
        }
    }

    // 获取经验花费
    private fun getCost(setting: EnchantmentSetting, level: Int, repairCost: Int, repairCost2: Int): Int {
        return setting.data.originEnchantment.anvilCost * level + repairCost + repairCost2
    }

    // 根据失败率判断是否成功
    private fun isSuccess(player: Player, setting: EnchantmentSetting, anvilCostLevel: Int): Boolean {
        val baseFailure = setting.failure

        // 先纯随机结果
        val random = (0..100).random()
        var adjustedFailure = baseFailure
        val success = (random > baseFailure)

        // 如果启用 上下限必成/必败
        if (config.EB_FAILURE_CORRECTION_MINMAX_ENABLED) {
            if (baseFailure <= config.EB_FAILURE_CORRECTION_MINMAX_MIN) return true
            if (baseFailure >= config.EB_FAILURE_CORRECTION_MINMAX_MAX) return false
        }

        // TODO 重写: 策略性选择判断方案
        // 如果启用 根据历史记录修正概率
        if (config.EB_FAILURE_CORRECTION_HISTORY_ENABLE) {
            // 获取玩家历史记录（最近20次）
            val dataLogs = FirCacheManager.getInstance().getRecentAnvilEnchantLogs(player.uniqueId)

            // 如果有强制必成, 则忽略这一部分的数据
            if (config.EB_FAILURE_CORRECTION_MINMAX_ENABLED) {
                dataLogs.filter {
                    it.failure <= config.EB_FAILURE_CORRECTION_MINMAX_MIN || it.failure >= config.EB_FAILURE_CORRECTION_MINMAX_MAX
                }
            }

            // 如果没记录, 直接纯随机返回
            if (dataLogs.isEmpty()) return success

            // TODO 可配置的更好的计算方法?
            // 计算实际成功率和期望成功率的差异
            val actualSuccessRate = dataLogs.count { it.isSuccess }.toDouble() / dataLogs.size * 100
            val expectedSuccessRate = 100 - baseFailure.toDouble()

            // 如果实际成功率低于期望，降低失败率进行补偿
            val difference = expectedSuccessRate - actualSuccessRate
            if (difference > 0) {
                val compensation = (difference * 0.5).toInt() // 补偿一半的差异
                adjustedFailure = maxOf(0, adjustedFailure - compensation)
            }
        }

        return random > adjustedFailure
    }

    /**
     * 更新数据到数据库并插入缓存
     */
    private fun saveToDatabaseAndCache(log: AnvilEnchantLogTable) {
        enchantLogData.insert(log)
        cacheManager.addEnchantLog(log)
    }
}