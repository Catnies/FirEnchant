package top.catnies.firenchantkt.engine.actions

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import top.catnies.firenchantkt.engine.AbstractAction
import top.catnies.firenchantkt.engine.ArgumentKey

class ExecuteCommandAction(
    args: Map<String, Any?>
) : AbstractAction(args) {

    @ArgumentKey(["player"], autoInject = true)
    private var player: Player? = null

    @ArgumentKey(["command"])
    private lateinit var command: String

    @ArgumentKey(["sudo"], required = false)
    private var sudo: Boolean = false

    override fun execute() {
        val executedCommand = PlaceholderAPI.setPlaceholders(player, command)
        if (sudo) {
            player!!.performCommand(executedCommand)
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executedCommand)
        }
    }
}