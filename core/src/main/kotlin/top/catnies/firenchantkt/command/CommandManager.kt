package top.catnies.firenchantkt.command

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import top.catnies.firenchantkt.FirEnchantPlugin

class CommandManager private constructor() {
    val plugin get() = FirEnchantPlugin.instance
    val logger get() = plugin.logger

    companion object {
        val instance: CommandManager by lazy { CommandManager().apply { load() } }
    }

    // 初始化
    private fun load() {
        registerCommands()
    }

    fun reload () {}

    // 注册命令
    private fun registerCommands() {
        // 为根命令添加权限检查
        val root = Commands.literal("firenchant").requires {
            return@requires it.sender.hasPermission("firenchant.command")
        }

        // 添加子命令
        root.then(VersionCommand.create()) // 版本命令
        root.then(ReloadCommand.create()) // 重载插件命令
        root.then(GiveEnchantedBookCommand.create()) // 给予附魔书命令

        // 注册命令到服务器
        val lifecycleEventManager = plugin.lifecycleManager
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(root.build())
        }
    }

}