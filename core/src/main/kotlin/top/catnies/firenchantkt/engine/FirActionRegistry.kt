package top.catnies.firenchantkt.engine

import com.google.common.collect.HashBiMap
import org.bukkit.Bukkit
import top.catnies.firenchantkt.api.ServiceContainer
import top.catnies.firenchantkt.api.event.ActionRegisterEvent
import top.catnies.firenchantkt.engine.actions.*

class FirActionRegistry : ActionRegistry {

    companion object {
        val instance by lazy { FirActionRegistry().also { it.load() } }
    }

    // 双向映射表, 方便根据 string / class 查找对应的值.
    private val registry = HashBiMap.create<String, Class<out Action>>()

    fun load() {
        registerAction("close_inventory", CloseInventoryAction::class.java)
        registerAction("cost_level", CostLevelAction::class.java)
        registerAction("execute_command", ExecuteCommandAction::class.java)
        registerAction("play_sound", PlaySoundAction::class.java)
        registerAction("send_message", SendMessageAction::class.java)
        registerAction("take_item", TakeItemAction::class.java)

        ServiceContainer.register(ActionRegistry::class.java, this)
        Bukkit.getPluginManager().callEvent(ActionRegisterEvent(this))
    }

    override fun registerAction(
        name: String,
        action: Class<out Action>
    ) {
        registry.putIfAbsent(name, action)
    }

    override fun unregisterAction(name: String): Boolean {
        return registry.remove(name) != null
    }

    override fun getAction(name: String): Class<out Action>? {
        return registry[name]
    }

    override fun getActionName(action: Class<out Action>): String? {
        return registry.inverse()[action]
    }

}