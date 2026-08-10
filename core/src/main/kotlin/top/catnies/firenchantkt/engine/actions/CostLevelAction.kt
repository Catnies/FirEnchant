package top.catnies.firenchantkt.engine.actions

import org.bukkit.entity.Player
import top.catnies.firenchantkt.engine.AbstractAction
import top.catnies.firenchantkt.engine.ArgumentKey
import kotlin.math.max

class CostLevelAction(
    args: Map<String, Any?>
) : AbstractAction(args) {

    @ArgumentKey(["player"], autoInject = true)
    private var player: Player? = null

    @ArgumentKey(["cost"])
    private lateinit var sound: String

    override fun execute() {
        val currentLv = player?.level
        val costLv = sound.toIntOrNull() ?: 0
        player?.level = max(0, currentLv!! - costLv)
    }

}