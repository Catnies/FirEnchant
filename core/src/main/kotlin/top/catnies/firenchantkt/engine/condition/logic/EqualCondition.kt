package top.catnies.firenchantkt.engine.condition.logic

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import top.catnies.firenchantkt.engine.AbstractCondition
import top.catnies.firenchantkt.engine.ArgumentKey

class EqualCondition(
    args: Map<String, Any>,
) : AbstractCondition(args) {

    @ArgumentKey(["player"], autoInject = true)
    private var player: Player? = null

    @ArgumentKey(["v1", "value1"])
    private lateinit var value1: String

    @ArgumentKey(["v2", "value2"])
    private lateinit var value2: String

    override fun check(): Boolean {
        val firstNumber = PlaceholderAPI.setPlaceholders(player, value1).toDoubleOrNull() ?: return false
        val secondNumber = PlaceholderAPI.setPlaceholders(player, value2).toDoubleOrNull() ?: return false
        return firstNumber == secondNumber
    }

}