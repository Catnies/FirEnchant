package top.catnies.firenchantkt.command.openmenu

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player
import top.catnies.firenchantkt.command.AbstractCommand
import top.catnies.firenchantkt.gui.FirShowEnchantedBooksMenu
import top.catnies.firenchantkt.language.MessageConstants.COMMAND_CONSOLE_CANT_EXECUTE
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent

object ShowEnchantedBookMenuCommand: AbstractCommand() {

    private const val PERMISSION = "firenchant.command.openmenu.enchanted-books-list"
    private const val PERMISSION_OTHER = "firenchant.command.openmenu.enchanted-books-list.other"

    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("enchanted-books-list")
            .requires { requires(it) }
            .executes { execute(it) }
            .then(Commands.argument("player", ArgumentTypes.player())
                .requires { requiresOther(it) }
                .executes { execute(it) })
    }

    override fun requires(requirement: CommandSourceStack): Boolean {
        return requirement.sender.hasPermission(PERMISSION)
    }

    private fun requiresOther(requirement: CommandSourceStack): Boolean {
        return requirement.sender.hasPermission(PERMISSION_OTHER)
    }

    override fun execute(context: CommandContext<CommandSourceStack>): Int {
        val targetResolver = if (context.nodes.last().node.name == "player")
            context.getArgument("player", PlayerSelectorArgumentResolver::class.java) else null
        val player = targetResolver?.resolve(context.source)?.get(0) ?: (context.source.sender as? Player)
        if (player == null) {
            context.source.sender.sendTranslatableComponent(COMMAND_CONSOLE_CANT_EXECUTE)
            return Command.SINGLE_SUCCESS
        }

        FirShowEnchantedBooksMenu(player).openMenu(mutableMapOf(), true)
        return Command.SINGLE_SUCCESS
    }
}