@file:Suppress("UnstableApiUsage")

package top.catnies.firenchantkt.command.debug

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import org.bukkit.World
import top.catnies.firenchantkt.command.AbstractCommand
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import top.catnies.firenchantkt.language.MessageConstants.COMMAND_DEBUG_GET_LOCATION_BOOK_SHELF_COUNT_EXECUTE
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent


object GetLocationBookShelfCountCommand : AbstractCommand() {
    // /firenchantkt debug getBookShelfCount <world> <X> <Y> <Z>
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("getLocationBookShelfCount")
            .then(
                Commands.argument("world", ArgumentTypes.world())
                    .then(
                        Commands.argument("location", ArgumentTypes.blockPosition())
                            .executes { execute(it) })
            )

    override fun requires(requirement: CommandSourceStack): Boolean = true

    override fun execute(context: CommandContext<CommandSourceStack>): Int {
        val world = context.getArgument("world", World::class.java)
        val location = context.getArgument("location", BlockPositionResolver::class.java).resolve(context.source)
        val count = NMSHandlerHolder.getNMSHandler().getEnchantmentTableBookShelf(location.toLocation(world))

        context.source.sender.sendTranslatableComponent(
            COMMAND_DEBUG_GET_LOCATION_BOOK_SHELF_COUNT_EXECUTE,
            world.name,
            location.x(),
            location.y(),
            location.z(),
            count
        )

        return Command.SINGLE_SUCCESS
    }
}