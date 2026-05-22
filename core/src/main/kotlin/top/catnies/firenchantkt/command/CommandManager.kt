package top.catnies.firenchantkt.command

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.command.brokenitem.BreakMainHandItemCommand
import top.catnies.firenchantkt.command.brokenitem.FixMainHandItemCommand
import top.catnies.firenchantkt.command.debug.GetLocationBookShelfCountCommand
import top.catnies.firenchantkt.command.debug.GetNextEnchantingTableResultCommand
import top.catnies.firenchantkt.command.debug.GetPlayerEnchantmentSeedCommand
import top.catnies.firenchantkt.command.openmenu.EnchantingTableMenuCommand
import top.catnies.firenchantkt.command.openmenu.ExtractSoulMenuCommand
import top.catnies.firenchantkt.command.openmenu.RepairTableMenuCommand
import top.catnies.firenchantkt.command.openmenu.ShowEnchantedBookMenuCommand

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

    // 注册命令
    private fun registerCommands() {
        // 根命令
        val root = Commands.literal("firenchant").requires {
            return@requires it.sender.hasPermission("firenchant.command")
        }.apply {
            then(VersionCommand.create()) // 版本命令
            then(ReloadCommand.create()) // 重载插件命令
            then(GiveEnchantedBookCommand.create()) // 给予附魔书命令
        }

        // 菜单命令
        val menuRoot = Commands.literal("openmenu").requires {
            return@requires it.sender.hasPermission("firenchant.command.openmenu")
        }.apply {
            then(ExtractSoulMenuCommand.create()) // 灵魂提取菜单
            then(RepairTableMenuCommand.create()) // 修复装备菜单
            then(EnchantingTableMenuCommand.create()) // 附魔台菜单
            then(ShowEnchantedBookMenuCommand.create()) // 附魔书列表菜单
        }.also { root.then(it) }

        // 破损物品命令
        val brokenRoot = Commands.literal("brokengear").requires {
            return@requires it.sender.hasPermission("firenchant.command.brokengear")
        }.apply {
            then(BreakMainHandItemCommand.create()) // 破坏主手物品
            then(FixMainHandItemCommand.create()) // 修复主手物品
        }.also { root.then(it) }

        // Debug命令
        val debugRoot = Commands.literal("debug").requires {
            return@requires it.sender.hasPermission("firenchant.command.debug")
        }.apply {
            then(GetLocationBookShelfCountCommand.create()) // 获取附近书架数量
            then(GetNextEnchantingTableResultCommand.create()) // 预测附魔台结果
            then(GetPlayerEnchantmentSeedCommand.create()) // 获取玩家附魔种子
        }.also { root.then(it) }

        // 注册命令到服务器
        val lifecycleEventManager = plugin.lifecycleManager
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(root.build())
        }
    }

}