package top.catnies.firenchantkt.engine.actions

import org.bukkit.entity.Player
import top.catnies.firenchantkt.engine.AbstractAction
import top.catnies.firenchantkt.engine.ArgumentKey
import top.catnies.firenchantkt.util.MessageUtils.renderToComponent

class SendMessageAction(
    args: Map<String, Any?>
) : AbstractAction(args) {

    @ArgumentKey(["player"], autoInject = true)
    private var player: Player? = null

    @ArgumentKey(["content", "message"])
    private lateinit var content: String

    override fun execute() {
        player?.let {
            content.renderToComponent(player).also { player!!.sendMessage(it) }
        }
    }

}