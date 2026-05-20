package com.github.iamnot23.gui_v2

import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI

object InvUIv2Setup {
    fun setPlugin(plugin: JavaPlugin) {
        InvUI.getInstance().setPlugin(plugin)
    }
}