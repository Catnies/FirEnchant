package top.catnies.firenchantkt.item.enchantingtable

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.checkerframework.checker.index.qual.Positive
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.api.event.enchantingtable.OriginalBookInputEvent
import top.catnies.firenchantkt.config.EnchantingTableConfig
import top.catnies.firenchantkt.config.extern.CustomRollStrategyData
import top.catnies.firenchantkt.context.EnchantingTableContext
import top.catnies.firenchantkt.enchantment.FirEnchantmentSetting
import top.catnies.firenchantkt.enchantment.FirEnchantmentSettingFactory
import top.catnies.firenchantkt.gui.AbstractFirEnchantMenu
import top.catnies.firenchantkt.gui.wrapper.InventoryPostEventWrapper
import top.catnies.firenchantkt.integration.FirEnchantmentProviderRegistry
import top.catnies.firenchantkt.integration.FirItemProviderRegistry
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import top.catnies.firenchantkt.item.enchantingtable.origin_book.OriginalBookData
import top.catnies.firenchantkt.item.enchantingtable.origin_book.RollStrategy
import top.catnies.firenchantkt.item.enchantingtable.origin_book.VanillaRollStrategyData
import kotlin.random.Random

@Suppress("UnstableApiUsage")
class FirOriginalBook: OriginalBook {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = EnchantingTableConfig.instance
    }

    val nmsHandler = NMSHandlerHolder.getNMSHandler()

    // 检查物品是否是指定的可附魔物品
    override fun matches(itemStack: ItemStack): Boolean {
        val originalBookData = config.ORIGINAL_BOOK_MATCHES.find {
            val itemProvider = FirItemProviderRegistry.instance.getItemProvider(it.hookedPlugin) ?: return@find false
            return@find (itemProvider.getIdByItem(itemStack).equals(it.hookedID, true))
        }
        return originalBookData != null
    }

    // 当物品放入附魔台时
    override fun onPostInput(itemStack: ItemStack, eventWrapper: InventoryPostEventWrapper, context: EnchantingTableContext) {

        // 查找对应的配置类
        val originalBookData = config.ORIGINAL_BOOK_MATCHES.find {
            val itemProvider = FirItemProviderRegistry.instance.getItemProvider(it.hookedPlugin) ?: return@find false
            return@find (itemProvider.getIdByItem(itemStack).equals(it.hookedID, true))
        } ?: return

        // 获取附魔力
        val enchantable = itemStack.getData(DataComponentTypes.ENCHANTABLE)?.value() ?: return

        // 原版抽取策略
        if (originalBookData.rollStrategy == RollStrategy.VANILLA) {
            onVanillaStrategy(originalBookData, context, enchantable, itemStack)
            return
        } else if (originalBookData.rollStrategy == RollStrategy.CUSTOM) { // 如果此本起源书的抽取策略是自定义
            onCustomStrategy(originalBookData, context, enchantable)
            return
        }
    }

    private fun onCustomStrategy(
        originalBookData: OriginalBookData,
        context: EnchantingTableContext,
        enchantable: @Positive Int
    ) {
        val player = context.player
        val tableMenu = context.menu as AbstractFirEnchantMenu

        // 获取抽取策略数据
        val data = originalBookData.rollStrategyData as CustomRollStrategyData

        val enchantingTableResults = data.slotData.mapIndexed { index, slotData ->
            // 遍历3个附魔槽的配置

            requireNotNull(slotData) { "错误的配置, 槽 $index 无法读取" }

            // 覆写菜单中的按钮数据
            tableMenu.overrideSlot(
                index,
                slotData.afterEnchantAction,
                slotData.activeItem,
                slotData.inactiveItem,
                slotData.conditions
            )

            val randomSource = Random(player.enchantmentSeed + index)
            val eData = slotData.roll(randomSource)
            val enchantment = eData.enchantment
            val level = eData.rollLevel(randomSource) // 使用随机数提供器抽取等级
            val failure = eData.rollFailure(randomSource) // 使用随机数提供器抽取失败率
            val enchantmentData = FirEnchantAPI.getEnchantmentData(enchantment.key)!!

            // 返回最终附魔结果
            FirEnchantmentSetting(enchantmentData, level, failure, 0)
        }
        if (enchantingTableResults.isEmpty()) return // 没有结果魔咒, 无法附魔
        // TODO(广播事件)
        // 应用执行
        tableMenu.setRecordEnchantable(enchantable)
        tableMenu.setEnchantmentResult(enchantingTableResults)
        tableMenu.refreshCanLight()
        tableMenu.refreshLine()
        return
    }

    private fun onVanillaStrategy(
        originalBookData: OriginalBookData,
        context: EnchantingTableContext,
        enchantable: @Positive Int,
        itemStack: ItemStack
    ) {
        val player = context.player
        val tableMenu = context.menu as AbstractFirEnchantMenu

        // 获取这个配置的可附魔列表
        val data = originalBookData.rollStrategyData as VanillaRollStrategyData
        val enchantments = data.enchantmentList

        val enchantmentProviders = FirEnchantmentProviderRegistry.instance.getEnchantmentProviders()

        var index = 0
        val enchantingTableResults = if (enchantmentProviders.isNotEmpty()) {
            // 通过插件获取结果
            enchantmentProviders.last().prepareEnchant(enchantable, player, itemStack)
        }else {
            // 计算附魔结果
            nmsHandler.getPlayerNextEnchantmentTableResultByEnchantmentList(
                player, context.bookShelves, enchantable, enchantments
            )
        }.map { entry ->
            index++
            val failureRange = getEnchantBookFailureRange(index)
            val failure = Random(player.enchantmentSeed + index).nextInt(failureRange.first, failureRange.second)
            val enchantment = entry.keys.first()
            val level = entry.values.first()
            val enchantmentData = FirEnchantAPI.getEnchantmentData(enchantment.key)!!
            FirEnchantmentSettingFactory.fromData(enchantmentData, level, failure, 0)
        }
        if (enchantingTableResults.isEmpty()) return // 没有结果魔咒, 无法附魔

        // 广播事件
        val inputEvent = OriginalBookInputEvent(
            player, itemStack, enchantments, enchantingTableResults
        )
        Bukkit.getPluginManager().callEvent(inputEvent)

        // 应用执行
        tableMenu.setRecordEnchantable(enchantable)
        tableMenu.setEnchantmentResult(enchantingTableResults)
        tableMenu.refreshCanLight()
        tableMenu.refreshLine()
        return
    }

    // 获取附魔书失败率的上下界
    private fun getEnchantBookFailureRange(line: Int): Pair<Int, Int> {
        return when(line) {
            1 -> config.ENCHANT_COST_LINE_1_MIN_FAILURE to config.ENCHANT_COST_LINE_1_MAX_FAILURE
            2 -> config.ENCHANT_COST_LINE_2_MIN_FAILURE to config.ENCHANT_COST_LINE_2_MAX_FAILURE
            3 -> config.ENCHANT_COST_LINE_3_MIN_FAILURE to config.ENCHANT_COST_LINE_3_MAX_FAILURE
            else -> 0 to 100
        }
    }


}