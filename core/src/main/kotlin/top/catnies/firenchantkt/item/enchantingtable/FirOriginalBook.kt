package top.catnies.firenchantkt.item.enchantingtable

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.api.FirEnchantAPI
import top.catnies.firenchantkt.api.event.enchantingtable.OriginalBookInputEvent
import top.catnies.firenchantkt.config.EnchantingTableConfig
import top.catnies.firenchantkt.context.EnchantingTableContext
import top.catnies.firenchantkt.enchantment.FirEnchantmentSettingFactory
import top.catnies.firenchantkt.integration.FirItemProviderRegistry
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent
import kotlin.random.Random

class FirOriginalBook: OriginalBook {

    companion object {
        val plugin = FirEnchantPlugin.instance
        val logger = plugin.logger
        val config = EnchantingTableConfig.instance
    }

    val nmsHandler = NMSHandlerHolder.getNMSHandler()
    val matches = config.ORIGINAL_BOOK_MATCHES

    // 检查物品是否是指定的可附魔物品
    override fun matches(itemStack: ItemStack): Boolean {
        val originalBookData = matches.find {
            val itemProvider = FirItemProviderRegistry.instance.getItemProvider(it.hookedPlugin) ?: return@find false
            return@find (itemProvider.getIdByItem(itemStack).equals(it.hookedID, true))
        }
        return originalBookData != null
    }

    // 当物品放入附魔台时
    override fun onPostInput(itemStack: ItemStack, event: ItemPostUpdateEvent, context: EnchantingTableContext) {
        val player = context.player
        val tableMenu = context.menu

        // 查找对应的配置类
        val originalBookData = matches.find {
            val itemProvider = FirItemProviderRegistry.instance.getItemProvider(it.hookedPlugin) ?: return@find false
            return@find (itemProvider.getIdByItem(itemStack).equals(it.hookedID, true))
        } ?: return

        // 获取附魔力
        val enchantable = (if (originalBookData.enchantable >= 0) originalBookData.enchantable
        else itemStack.getData(DataComponentTypes.ENCHANTABLE)?.value()) ?: return

        // 获取这个配置的可附魔列表
        val enchantments = originalBookData.enchantmentList

        // 计算附魔结果
        var index = 0
        val enchantingTableResults = nmsHandler.getPlayerNextEnchantmentTableResultByEnchantmentList(
                player, context.bookShelves, enchantable, enchantments
        ).map { entry ->
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