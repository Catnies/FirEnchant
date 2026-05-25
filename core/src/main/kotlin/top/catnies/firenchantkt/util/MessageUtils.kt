package top.catnies.firenchantkt.util

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.entity.Player
import top.catnies.firenchantkt.language.MessageTranslator
import top.catnies.firenchantkt.language.tags.PlaceholderTag


object MessageUtils {

    val miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(TagResolver.standard())
            .resolver(PlaceholderTag) // Papi标签解析器
            .build()
    ).build()

    /**
     * 发送一个可翻译组件给目标.
     *
     * @param key 翻译键
     * @param ptr 上下文解析目标
     * @param args 消息参数
     */
    fun Audience.sendTranslatableComponent(key: String, ptr: Pointered, vararg args: Any) {
        val component = Component.translatable(
            key,
            Argument.target(ptr),
            *args.map {
                when (it) {
                    is String -> Component.text(it)
                    is Component -> it
                    else -> Component.text(it.toString())
                }
            }.toTypedArray()
        )
        this.sendMessage(component)
    }

    fun Audience.sendTranslatableComponent(key: String, vararg args: Any) {
        this.sendTranslatableComponent(key, this, *args)
    }


    /**
     * 将字符串解析成组件, 自动处理 PlaceholderAPI 和 LegacyColor.
     * 同时还可以传入运行时参数进行解析.
     * @param ptr PAPI 解析者
     * @return 文本组件.
     */
    fun String.renderToComponent(ptr: Pointered? = null, args: Map<String, String> = mutableMapOf()): Component{
        val parsedRuntimePlaceholders = if (args.isEmpty()) this else this.replacePlaceholders(args)
        val parsedPlaceholderAPI = parsedRuntimePlaceholders.parsePlaceholderAPI(ptr as? Player)
        val miniMessageText = parsedPlaceholderAPI.convertLegacyColorToMiniMessage()
        return if (ptr != null) miniMessage.deserialize(miniMessageText, ptr) else miniMessage.deserialize(miniMessageText)
    }

    // 解析 PlaceholderAPI
    fun String.parsePlaceholderAPI(player: Player? = null): String {
        return PlaceholderAPI.setPlaceholders(player, this)
    }

    fun List<String>.parsePlaceholderAPI(player: Player? = null): List<String> {
        return this.map { it.parsePlaceholderAPI(player) }
    }

    // 将传统颜色序列化成Minimessage格式的颜色
    fun String.convertLegacyColorToMiniMessage(): String {
        return MessageTranslator.legacyToMiniMessage(MessageTranslator.legacyColorToMiniMessage(this))
    }

    fun List<String>.convertLegacyColorToMiniMessage(): List<String> {
        return this.map { it.convertLegacyColorToMiniMessage() }
    }

    // 递归替换 Component 中的占位符
    fun Component.replacePlaceholders(args: Map<String, String>): Component {
        return when (this) {
            is TextComponent -> {
                // 获取文本内容并替换占位符
                val content = this.content().replacePlaceholders(args)
                // 创建新的 TextComponent
                Component.text(content)
                    .style(this.style()) // 保留原有样式
                    .children(this.children().map { it.replacePlaceholders(args) }) // 递归处理子组件
            }
            else -> {
                // 对于其他类型的组件，只处理子组件
                this.children(this.children().map { it.replacePlaceholders(args) })
            }
        }
    }

    // 替换String中的占位符
    fun String.replacePlaceholders(args: Map<String, String>): String {
        // 依次对args的每个entry进行处理.
        return args.entries.fold(this) {acc, (key, value) -> acc.replace("{$key}", value) }
    }

    fun List<String>.replacePlaceholders(list: List<String>, args: Map<String, String>): List<String> {
        return list.map { it.replacePlaceholders(args) }
    }


    // 将字符串包装成菜单标题
//    fun String.wrapTitle(player: Player?): AdventureComponentWrapper {
//        val component =
//        return AdventureComponentWrapper(component)
//    }
    fun String.parseTitleStringAsComponent(player: Player?)= this.renderToComponent(player)

}
