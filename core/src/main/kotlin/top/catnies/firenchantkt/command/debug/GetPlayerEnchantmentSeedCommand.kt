package top.catnies.firenchantkt.command.debug

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player
import top.catnies.firenchantkt.command.AbstractCommand
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import top.catnies.firenchantkt.language.MessageConstants.COMMAND_CONSOLE_CANT_EXECUTE
import top.catnies.firenchantkt.language.MessageConstants.COMMAND_DEBUG_GET_PLAYER_ENCHANTMENT_SEED_EXECUTE
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent

object GetPlayerEnchantmentSeedCommand : AbstractCommand() {

    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("getPlayerEnchantmentSeed")
            .then(
                Commands.argument("player", ArgumentTypes.player())
                    .executes { execute(it) }
            )
    }

    override fun requires(requirement: CommandSourceStack): Boolean = true

    override fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = if (context.nodes.last().node.name == "player")
            context.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(context.source)[0]
        else context.source.sender as? Player
        if (player == null) {
            context.source.sender.sendTranslatableComponent(COMMAND_CONSOLE_CANT_EXECUTE)
            return Command.SINGLE_SUCCESS
        }

        NMSHandlerHolder.getNMSHandler().getPlayerEnchantmentSeed(player)
            .let {
                context.source.sender.sendTranslatableComponent(
                    COMMAND_DEBUG_GET_PLAYER_ENCHANTMENT_SEED_EXECUTE,
                    player.name,
                    it
                )
            }
        return Command.SINGLE_SUCCESS
    }

}