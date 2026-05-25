package top.catnies.firenchantkt.gui

import org.bukkit.entity.Player
import top.catnies.firenchantkt.util.VersionHelper
import java.lang.reflect.Constructor
import kotlin.lazy
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

object InvUIAdaptor {
    val useV2 = VersionHelper.isOrAbove26_1()

    val setupConstructor: Constructor<out Any> = if (!useV2)
        Class.forName("top.catnies.firenchantkt.invui_v1.InvUIv1Setup").getDeclaredConstructor()
    else
        Class.forName("top.catnies.firenchantkt.invui_v2.InvUIv2Setup").getDeclaredConstructor()

    fun getSetup(): InvUISetup {
        return setupConstructor.newInstance() as InvUISetup;
    }

    val enchantingTableMenuConstructor: KFunction<Any> by lazy {

        requireNotNull(
            (
                    if (!useV2) {
                        Class.forName("top.catnies.firenchantkt.invui_v1.FirEnchantingTableMenu")
                    } else
                        Class.forName("top.catnies.firenchantkt.invui_v2.FirEnchantingTableMenu")
                    ).kotlin.primaryConstructor
        )
    }

    fun getEnchantingTableMenu(
        player: Player,
        bookShelves: Int = 0
    ): AbstractFirEnchantMenu {
        return enchantingTableMenuConstructor.call(player, bookShelves) as AbstractFirEnchantMenu;
    }

    val extractSoulMenuConstructor: KFunction<Any> by lazy {

        requireNotNull(
            (
                    if (!useV2) {
                        Class.forName("top.catnies.firenchantkt.invui_v1.FirExtractSoulMenu")
                    } else
                        Class.forName("top.catnies.firenchantkt.invui_v2.FirExtractSoulMenu")
                    ).kotlin.primaryConstructor
        )
    }
    fun getExtractSoulMenu(player: Player): ExtractSoulMenu {
        return extractSoulMenuConstructor.call(player) as ExtractSoulMenu
    }

    val repairTableMenuConstructor: KFunction<Any> by lazy {

        requireNotNull(
            (
                    if (!useV2) {
                        Class.forName("top.catnies.firenchantkt.invui_v1.FirRepairTableMenu")
                    } else
                        Class.forName("top.catnies.firenchantkt.invui_v2.FirRepairTableMenu")
                    ).kotlin.primaryConstructor
        )
    }

    fun getRepairTableMenu(player: Player): RepairTableMenu {
        return repairTableMenuConstructor.call(player) as RepairTableMenu
    }


    val showEnchantedBooksMenuConstructor: KFunction<Any> by lazy {
        requireNotNull(
            (
                    if (!useV2) {
                        Class.forName("top.catnies.firenchantkt.invui_v1.FirShowEnchantedBooksMenu")
                    } else
                        Class.forName("top.catnies.firenchantkt.invui_v2.FirShowEnchantedBooksMenu")
                    ).kotlin.primaryConstructor
        )
    }

    fun getShowEnchantedBooksMenu(player: Player): ShowEnchantedBooksMenu {
        return showEnchantedBooksMenuConstructor.call(player) as ShowEnchantedBooksMenu
    }
}