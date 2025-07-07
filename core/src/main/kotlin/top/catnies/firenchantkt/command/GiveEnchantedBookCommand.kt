package top.catnies.firenchantkt.command

import com.google.common.collect.Range
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.Enchantment
import top.catnies.firenchantkt.api.FirEnchantAPI
import java.util.concurrent.ThreadLocalRandom

/**
 * 给予附魔书.
 */
object GiveEnchantedBookCommand: AbstractCommand() {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("giveenchantedbook").requires { VersionCommand.requires(it) }
            .then(Commands.argument("player", ArgumentTypes.players())
                    .then(Commands.argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT))
                        .then(Commands.argument("level", ArgumentTypes.integerRange())
                            .then(Commands.argument("failure", ArgumentTypes.integerRange())
                                .executes { execute(it) }
                                .then(Commands.argument("consumedSouls", ArgumentTypes.integerRange())
                                    .executes { execute(it) }
                                )
                            )
                        )
                    )
            )
    }

    override fun requires(requirement: CommandSourceStack): Boolean {
        return requirement.sender.hasPermission("firenchant.command.giveenchantedbook")
    }

    override fun execute(context: CommandContext<CommandSourceStack>): Int {
        val enchantmentKey = context.getArgument("enchantment", Enchantment::class.java).key()
        val levelRange = context.getArgument("level", IntegerRangeProvider::class.java)
        val failureRange = context.getArgument("failure", IntegerRangeProvider::class.java)

        var consumedSoulsRange: IntegerRangeProvider? = null;
        try {
            consumedSoulsRange = context.getArgument("consumedSouls", IntegerRangeProvider::class.java)
        } catch (_: IllegalArgumentException) {}

        val level = getRandomFromRange(levelRange.range())
        val failure = getRandomFromRange(failureRange.range())
        val consumedSouls = consumedSoulsRange?.range()?.let { getRandomFromRange(it) } ?: 0
        val enchantmentSetting = FirEnchantAPI.getSettingsByData(enchantmentKey, level, failure, consumedSouls)

        if (enchantmentSetting == null) {
            println("enchantmentSetting is null")
            return Command.SINGLE_SUCCESS
        }

        val targetResolver = context.getArgument("player", PlayerSelectorArgumentResolver::class.java)
        val players = targetResolver.resolve(context.source)
        players.forEach { player ->
            player.inventory.addItem(enchantmentSetting.toItemStack())
            player.sendMessage { Component.text("give enchanted book to 6666") }
        }

        println("give enchanted book to $players")
        return Command.SINGLE_SUCCESS
    }


    // 根据范围生成一个随机数.
    private fun getRandomFromRange(range: Range<Int>): Int {
        if (range.isEmpty) {
            throw IllegalArgumentException("Range 不能为空.")
        }
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw IllegalArgumentException("Range 必须有上下界.");
        }

        val lower = range.lowerEndpoint()
        val upper = range.upperEndpoint()

        if (lower > upper) {
            throw IllegalArgumentException("调整后的范围无效");
        }

        return ThreadLocalRandom.current().nextInt(lower, upper + 1)
    }

}