package top.catnies.firenchantkt.language.tags

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import top.catnies.firenchantkt.enchantment.FirEnchantmentSetting
import top.catnies.firenchantkt.util.MessageUtils.renderToComponent
import top.catnies.firenchantkt.util.NumberUtils.toRoman

class FirEnchantTag(
    val setting: FirEnchantmentSetting
) : TagResolver {

    override fun has(name: String) = "firenchant" == name

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context
    ): Tag? {
        // 判断是否是解析标签
        if (!has(name)) {
            return null
        }
        // 获取参数尝试解析
        val rawArgument = arguments.popOr("No argument provided").toString()

        val parsed = when (rawArgument) {
            "level" -> setting.level.toString().renderToComponent()
            "level_roman" -> setting.level.toRoman().renderToComponent()
            "max_level" -> setting.data.maxLevel.toString().renderToComponent()
            "max_level_roman" -> setting.data.maxLevel.toRoman().renderToComponent()
            "enchantment" -> setting.data.originEnchantment.description()
            "enchantment_key" -> setting.data.originEnchantment.key.asString().renderToComponent()
            "failure" -> setting.failure.toString().renderToComponent()
            "consumed_souls" -> setting.consumedSouls.toString().renderToComponent()
            else -> Component.empty()
        }

        return Tag.inserting(parsed)
    }

}