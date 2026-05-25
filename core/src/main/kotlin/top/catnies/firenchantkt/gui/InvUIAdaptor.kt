package top.catnies.firenchantkt.gui

import org.bukkit.entity.Player
import top.catnies.firenchantkt.util.VersionHelper
import java.lang.reflect.Constructor
import kotlin.lazy

object InvUIAdaptor {
    val useV2 = VersionHelper.isOrAbove26_1()

    val setupConstructor: Constructor<out Any> = if (!useV2)
        Class.forName("top.catnies.firenchantkt.invui_v1.InvUIv1Setup").getDeclaredConstructor()
    else
        Class.forName("top.catnies.firenchantkt.invui_v2.InvUIv2Setup").getDeclaredConstructor()

    fun getSetup(): InvUISetup {
        return setupConstructor.newInstance() as InvUISetup;
    }

    val enchantingTableMenuConstructor: Constructor<out Any> by lazy {
        if (!useV2)
            Class.forName("top.catnies.firenchantkt.invui_v1.FirEnchantingTableMenu").getDeclaredConstructor()
        else
            Class.forName("top.catnies.firenchantkt.invui_v2.FirEnchantingTableMenu").getDeclaredConstructor()
    }

    fun getEnchantingTableMenu(
        player: Player,
        bookShelves: Int = 0
    ): AbstractFirEnchantMenu {
        return enchantingTableMenuConstructor.newInstance(player, bookShelves) as AbstractFirEnchantMenu;
    }

    val extractSoulMenuConstructor: Constructor<out Any> by lazy {
        if (!useV2)
            Class.forName("top.catnies.firenchantkt.invui_v1.ExtractSoulMenu").getDeclaredConstructor()
        else
            Class.forName("top.catnies.firenchantkt.invui_v2.ExtractSoulMenu").getDeclaredConstructor()
    }


    fun getExtractSoulMenu(player: Player): ExtractSoulMenu {
        return extractSoulMenuConstructor.newInstance(player) as ExtractSoulMenu
    }

    val repairTableMenuConstructor: Constructor<out Any> by lazy {
        if (!useV2)
            Class.forName("top.catnies.firenchantkt.invui_v1.FirRepairTableMenu").getDeclaredConstructor()
        else
            Class.forName("top.catnies.firenchantkt.invui_v2.FirRepairTableMenu").getDeclaredConstructor()
    }


    fun getRepairTableMenu(player: Player): RepairTableMenu {
        return repairTableMenuConstructor.newInstance(player) as RepairTableMenu
    }

    val showEnchantedBooksMenuConstructor: Constructor<out Any> by lazy {
        if (!useV2)
            Class.forName("top.catnies.firenchantkt.invui_v1.FirShowEnchantedBooksMenu").getDeclaredConstructor()
        else
            Class.forName("top.catnies.firenchantkt.invui_v2.FirShowEnchantedBooksMenu").getDeclaredConstructor()
    }

    fun getShowEnchantedBooksMenu(player: Player): RepairTableMenu {
        return showEnchantedBooksMenuConstructor.newInstance(player) as RepairTableMenu
    }
}