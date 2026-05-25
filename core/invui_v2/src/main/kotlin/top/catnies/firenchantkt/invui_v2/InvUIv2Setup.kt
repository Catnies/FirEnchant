package top.catnies.firenchantkt.invui_v2

import org.bukkit.plugin.Plugin
import top.catnies.firenchantkt.gui.InvUISetup
import xyz.xenondevs.invui.InvUI

class InvUIv2Setup : InvUISetup {

    override fun setup(plugin: Plugin) {
        InvUI.getInstance().setPlugin(plugin)
    }

}