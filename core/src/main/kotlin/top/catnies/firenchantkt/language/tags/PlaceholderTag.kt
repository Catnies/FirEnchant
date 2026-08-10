package top.catnies.firenchantkt.language.tags

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlaceholderTag : TagResolver {
    val hasPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")

    override fun has(name: String) = "papi" == name

    // 处理PlaceholderAPI标签
    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context
    ): Tag? {
        // 判断是否是Papi解析标签.
        if (!has(name) || !hasPlaceholderAPI) {
            return null
        }
        // 获取参数尝试解析
        val rawArgument = arguments.popOr("No argument relational placeholder provided").toString()
        val player = ctx.target() as? Player
        val parsed = PlaceholderAPI.setPlaceholders(player, "%$rawArgument%")
        // 如果返回值包含传统颜色代码，进行转换修复.
        if (parsed.contains('§')) {
            val component = LegacyComponentSerializer.legacySection().deserialize(parsed)
            return Tag.inserting(component)
        }
        return Tag.inserting(Component.text(parsed))
    }
}