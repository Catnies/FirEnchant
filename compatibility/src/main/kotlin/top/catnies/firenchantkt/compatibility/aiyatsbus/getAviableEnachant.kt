package top.catnies.firenchantkt.compatibility.aiyatsbus

import cc.polarastrum.aiyatsbus.core.AiyatsbusEnchantment
import cc.polarastrum.aiyatsbus.module.ingame.mechanics.EnchantingTableSupport
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible
import cc.polarastrum.aiyatsbus.core.*
import cc.polarastrum.aiyatsbus.core.data.CheckType


// aiya api引用转换函数
fun getAvailableEnchantments(item: ItemStack, player: Player?): List<Enchantment> {
    return item.etsAvailable(CheckType.ATTAIN, player) // 传入玩家检测权限
        .filterNot { it.alternativeData.isTreasure } // 去除宝藏魔咒
        .map { it.enchantment }
}


val kFunction by lazy {
    EnchantingTableSupport::class.declaredMemberFunctions.find { it.name == "doPrepareEnchant" }
}


// 他的这个函数会卡死
fun getEnchantmentTableResult(player: Player, item: ItemStack, bonus: Int): List<Pair<Enchantment, Int>> {
    return kFunction?.let { kFunction ->
        kFunction.isAccessible = true
        val map =
            kFunction.call(EnchantingTableSupport, player, item, bonus) as Map<Int, Pair<AiyatsbusEnchantment, Int>>
        val list = map.map {
            val enchantment = it.value.first.enchantment
            val level = it.value.second
            enchantment to level
        }
        list
    } ?: emptyList()

}