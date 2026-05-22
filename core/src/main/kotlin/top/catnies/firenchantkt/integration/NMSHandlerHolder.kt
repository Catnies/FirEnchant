package top.catnies.firenchantkt.integration

import org.bukkit.Bukkit
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.nms.NMSHandler

class NMSHandlerHolder {

    companion object{
        @JvmStatic
        val instance by lazy { NMSHandlerHolder().apply {
            load()
        } }

        @JvmStatic
        fun getNMSHandler(): NMSHandler = instance.nmsHandler
    }

    var plugin = FirEnchantPlugin.instance
    lateinit var nmsHandler: NMSHandler

    // 加载初始化NMSHandler
    private fun load() {
        val nmsPackPrefix = getNMSPackPrefix()
        val klass = Class.forName("top.catnies.firenchantkt.nms.$nmsPackPrefix.NMSHandlerImpl")
        nmsHandler = klass.getDeclaredConstructor().newInstance() as NMSHandler
    }

    private fun getNMSPackPrefix(): String {
        val minecraftVersion = Bukkit.getServer().minecraftVersion
        return when (minecraftVersion) {
//            "1.21.0", "1.21.1", "1.21.2"    -> "v1_21_R1"
//            "1.21.3"                        -> "v1_21_R2"
            "1.21.4"                        -> "v1_21_R3"
            "1.21.5"                        -> "v1_21_R4"
            "1.21.6", "1.21.7", "1.21.8"    -> "v1_21_R5"
            "1.21.9", "1.21.10"             -> "v1_21_R6"
            "1.21.11"                       -> "v1_21_R7"
            "26.1", "26.1.1", "26.1.2"      -> "v26_1"
            else -> {
                Bukkit.getPluginManager().disablePlugin(plugin)
                throw IllegalArgumentException("Unsupported minecraft version: $minecraftVersion")
            }
        }
    }

}