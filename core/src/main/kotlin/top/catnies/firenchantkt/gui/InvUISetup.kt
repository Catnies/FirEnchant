package top.catnies.firenchantkt.gui

import org.bukkit.plugin.Plugin
import top.catnies.firenchantkt.util.VersionHelper

interface InvUISetup {

    companion object{
        @JvmStatic
        val instance by lazy { create() }

        fun create(): InvUISetup {
            val klass = if (VersionHelper.isOrAbove26_1())
                Class.forName("top.catnies.firenchantkt.invui.InvUIv2Setup") else
                Class.forName("top.catnies.firenchantkt.invui.InvUIv1Setup")
            return klass.getDeclaredConstructor().newInstance() as InvUISetup;
        }
    }

    fun setup(plugin: Plugin)

}