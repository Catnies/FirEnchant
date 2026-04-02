package top.catnies.firenchantkt.config.extern

import kotlin.random.Random

const val MAX_BOOK_SHELF_COUNT: Int = 15

class EnchantSimulator {
    /**
     * 计算附魔等级需求
     * 根据书架数量和槽位
     * @param bookShelfCount 书架数量
     * @param enchantNum 槽位序号
     */
    fun getEnchantmentCost(bookShelfCount: Int, enchantNum: Int) :Int {
        val power = if (bookShelfCount > MAX_BOOK_SHELF_COUNT) MAX_BOOK_SHELF_COUNT
        else bookShelfCount
        // base -> [8, 30]
        val base = Random.Default.nextInt(8) + 1 + (power shr 1) + Random.Default.nextInt(power + 1)

        return when(enchantNum) {
            0 -> maxOf(base / 3, 1)
            1 -> base * 2 / 3 + 1
            else -> maxOf(base, power * 2)
        }
    }

    fun getEnchantPower(): Int {
        TODO()
    }
}