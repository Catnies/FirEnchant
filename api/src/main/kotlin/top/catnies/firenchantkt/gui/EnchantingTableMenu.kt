package top.catnies.firenchantkt.gui

import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.enchantment.EnchantmentSetting

interface EnchantingTableMenu {

    // 打开菜单
    fun openMenu(data: Map<String, Any>, async: Boolean = false)

    // 设置附魔台结果, 失败率来源于玩家的 seed + 0,1,2 生成随机数
    fun setEnchantmentResult(list: List<EnchantmentSetting>)

    // 检查并记录玩家现在能点亮多少个栏位
    fun refreshCanLight(): Int

    // 刷新附魔栏
    fun refreshLine()

    // 获取指定栏位的附魔书数据
    fun getEnchantmentSettingByLine(line: Int): EnchantmentSetting?

    // 设置记录的物品附魔力
    fun setRecordEnchantable(enchantable: Int)

    // 获取容器内的物品
    fun getInputInventoryItem(): ItemStack?

    // 清空容器
    fun clearInputInventory()
}