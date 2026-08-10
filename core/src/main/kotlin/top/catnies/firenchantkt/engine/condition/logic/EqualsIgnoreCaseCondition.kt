package top.catnies.firenchantkt.engine.condition.logic

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import top.catnies.firenchantkt.engine.AbstractCondition
import top.catnies.firenchantkt.engine.ArgumentKey

class EqualsIgnoreCaseCondition(
    args: Map<String, Any>,
) : AbstractCondition(args) {

    @ArgumentKey(["player"], autoInject = true)
    private var player: Player? = null

    @ArgumentKey(["s", "string"])
    private lateinit var string: String

    @ArgumentKey(["v", "value"])
    private lateinit var value: String

    override fun check(): Boolean {
        val rawString = PlaceholderAPI.setPlaceholders(player, string)
        val valueString = PlaceholderAPI.setPlaceholders(player, value)
        return rawString.equals(valueString, true)
    }

}