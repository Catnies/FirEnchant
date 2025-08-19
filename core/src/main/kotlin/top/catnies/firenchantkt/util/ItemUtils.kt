package top.catnies.firenchantkt.util

import com.saicone.rtag.item.ItemTagStream
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.util.MessageUtils.replacePlaceholders
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ItemUtils {

    // 判断物品是否是 null 或 空气
    @OptIn(ExperimentalContracts::class)
    fun ItemStack?.nullOrAir(): Boolean {
        contract { returns(false) implies (this@nullOrAir != null) }
        this ?: return true
        return this.type.isAir
    }

    // 检查物品是否能够应用某个魔咒兼容.
    fun ItemStack.isCompatibleWithEnchantment(other: Enchantment, level: Int): Boolean {
        // 如果物品类型不支持附魔
        if (!other.canEnchantItem(this)) return false
        // 检查物品上的魔咒是否有冲突
        this.enchantments.forEach { (enchantment, lv) ->
            if (enchantment.conflictsWith(other)) return false
            // 如果两个目标魔咒相同
            if (enchantment == other) {
                if (lv >= enchantment.maxLevel) return false // 不能超过最大等级
                if (lv > level) return false // 不能大于目标附魔的等级
            }
        }
        return true
    }

    // 序列化物品为Byte数组
    fun ItemStack.serializeToBytes(): ByteArray {
        return ItemTagStream.INSTANCE.toBytes(this)
    }
    fun ByteArray.deserializeFromBytes(): ItemStack {
        return ItemTagStream.INSTANCE.fromBytes(this)
    }

    // 增加物品的 RepairCost
    fun ItemStack.addRepairCost() {
        val cost = this.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0)!!
        this.setData(DataComponentTypes.REPAIR_COST, cost * 2 + 1)
    }
    fun ItemStack.addRepairCost(count: Int) {
        val cost = this.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0)!!
        this.setData(DataComponentTypes.REPAIR_COST, cost + count)
    }

    // 获取物品上所有的魔咒等级和
    fun ItemStack.sumAllEnchantmentLevel(): Int {
        return this.enchantments.values.sumOf { it }
    }

    // 获取物品上所有魔咒数量
    fun ItemStack.sumAllEnchantmentCount(): Int {
        return this.enchantments.size
    }

    // 将物品的 Name 和 Lore 中的占位符 ${placeholder} 替换成实际的值
    // @param args 占位符映射表，例如 mapOf("currentPage" to "1") 会将 ${currentPage} 替换为 1
    fun ItemStack.replacePlaceholder(args: Map<String, String>) {
        // 处理物品名称
        getData(DataComponentTypes.ITEM_NAME)?.let { nameComponent ->
            val replacedName = nameComponent.replacePlaceholders(args)
            setData(DataComponentTypes.ITEM_NAME, replacedName)
        }

        // 处理物品 Lore
        getData(DataComponentTypes.LORE)?.let { itemLore ->
            val loreBuilder = ItemLore.lore()

            itemLore.lines().forEach { line ->
                val replacedLine = line.replacePlaceholders(args)
                loreBuilder.addLine(replacedLine)
            }

            setData(DataComponentTypes.LORE, loreBuilder.build())
        }
    }

}