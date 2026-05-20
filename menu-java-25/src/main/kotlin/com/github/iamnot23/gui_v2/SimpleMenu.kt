package com.github.iamnot23.gui_v2

import org.bukkit.entity.Player
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class SimpleMenu(
    val player: Player,
) {

    init {


    }

    fun open() {
        val gui = Gui.empty(9, 4)
        val window = Window.builder()
            .setViewer(player)
            .setUpperGui(gui)
            .build()
        window.open()
    }

}