package top.catnies.firenchantkt.command.debug

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.command.AbstractCommand
import top.catnies.firenchantkt.config.SettingsConfig
import top.catnies.firenchantkt.integration.NMSHandlerHolder

object GetNextEnchantingTableResultCommand : AbstractCommand() {
    // /firenchantkt debug getNextEnchantingTableResult <player> <level> <count> <item>
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("getNextEnchantingTableResult")
            .then(
                Commands.argument("player", ArgumentTypes.player())
                    .then(
                        Commands.argument("bookShelfCount", IntegerArgumentType.integer(0, 15))
                            .then(
                                Commands.argument("item", ArgumentTypes.itemStack())
                                    .executes { execute(it) })
                    )
            )


    override fun requires(requirement: CommandSourceStack): Boolean = true

    override fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player =
            context.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(context.source)[0]
        val count = context.getArgument("bookShelfCount", Integer::class.java)
        val item = context.getArgument("item", ItemStack::class.java)

        val result = NMSHandlerHolder.getNMSHandler()
            .getPlayerNextEnchantmentTableResultByItemStack(
                player,
                count.toInt(),
                item,
                SettingsConfig.instance.REGISTRY
            )
        println(result)

        return Command.SINGLE_SUCCESS
    }
}