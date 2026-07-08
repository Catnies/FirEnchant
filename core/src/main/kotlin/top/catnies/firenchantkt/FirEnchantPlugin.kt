package top.catnies.firenchantkt

import cn.chengzhimeow.ccscheduler.scheduler.CCScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.catnies.firenchantkt.command.CommandManager
import top.catnies.firenchantkt.config.ConfigManager
import top.catnies.firenchantkt.database.FirCacheManager
import top.catnies.firenchantkt.database.FirConnectionManager
import top.catnies.firenchantkt.enchantment.FirEnchantmentManager
import top.catnies.firenchantkt.engine.FirActionRegistry
import top.catnies.firenchantkt.engine.FirConditionRegistry
import top.catnies.firenchantkt.gui.InvUIAdaptor
import top.catnies.firenchantkt.integration.FirItemProviderRegistry
import top.catnies.firenchantkt.integration.IntegrationManager
import top.catnies.firenchantkt.integration.NMSHandlerHolder
import top.catnies.firenchantkt.item.FirAnvilItemRegistry
import top.catnies.firenchantkt.item.FirEnchantingTableRegistry
import top.catnies.firenchantkt.item.FirRepairTableItemRegistry
import top.catnies.firenchantkt.item.brokengear.FirBrokenGear
import top.catnies.firenchantkt.language.MessageConstants.PLUGIN_COMPATIBILITY_HOOK_SUCCESS
import top.catnies.firenchantkt.language.TranslationManager
import top.catnies.firenchantkt.lazyinit.CraftEngineLoadListener
import top.catnies.firenchantkt.lazyinit.ItemsAdderLoadListener
import top.catnies.firenchantkt.lazyinit.NexoLoadListener
import top.catnies.firenchantkt.lazyinit.OraxenLoadListener
import top.catnies.firenchantkt.listener.ListenerManger
import top.catnies.firenchantkt.util.EnchantmentUtils
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.TaskUtils
import top.catnies.firenchantkt.util.TaskUtils.plugin
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

class FirEnchantPlugin: JavaPlugin(), FirEnchant, CoroutineScope {

    companion object {
        @JvmStatic
        var instance by Delegates.notNull<FirEnchantPlugin>()
            private set
    }

    // 协程同步 Dispatcher
    val mainDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (Bukkit.isPrimaryThread()) block.run()
            else CCScheduler.getInstance().globalRegionScheduler.runTask(plugin, block)
        }
    }

    private val job = SupervisorJob()
        override val coroutineContext: CoroutineContext = job + Dispatchers.IO
    override var isInitializedRegistry = false

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        InvUIAdaptor.getSetup().setup(this) // GUI依赖库
        this.registerLateInitListener(this)     // 延时初始化器
        CommandManager.instance             // 命令管理器
        NMSHandlerHolder.instance           // NMS管理器
        // 异步加载反射
        TaskUtils.runAsyncTasksLater(
            tasks = arrayOf({ InvUIAdaptor.loadConstructors() }),
            delay = 10L
        )
        logger.info("FirEnchant Plugin Enabled!")
    }

    override fun onDisable() {
        job.cancel()
        logger.info("FirEnchant Plugin Disabled!")
    }

    // 插件重载
    fun reload() {
        FirEnchantmentManager.instance.reload() // 系统魔咒管理器
        TranslationManager.instance.reload() // 语言管理器
        ConfigManager.instance.reload() // 配置文件管理器 (依赖: FirEnchantmentManager)

        IntegrationManager.instance.reload() // 关联插件集成管理器

        FirAnvilItemRegistry.instance.reload() // 铁砧物品注册表
        FirEnchantingTableRegistry.instance.reload() // 附魔台物品注册表

        EnchantmentUtils.ENCHANT_CACHE.clear() // 清除物品的可应用魔咒的缓存
    }

    // 初始化注册表
    override fun initRegistry() {
        if (isInitializedRegistry) {
            logger.warning("警告，插件试图重复初始化注册表，不应发生这样的问题，请保存错误日志，联系开发者反馈修复。")
            return
        }
        isInitializedRegistry = true

        FirItemProviderRegistry.instance        // 物品集成注册表
        FirConditionRegistry.instance           // 条件注册表
        FirActionRegistry.instance              // 动作注册表
        FirEnchantmentManager.instance          // 系统魔咒管理器
        TranslationManager.instance             // 语言管理器
        ConfigManager.instance                  // 配置文件管理器

        ListenerManger.instance                 // 事件监听器 (依赖: ConfigManager)
        FirConnectionManager.getInstance()      // 数据库链接控制器 (依赖: ConfigManager)
        FirCacheManager.getInstance()           // 玩家缓存管理器

        IntegrationManager.instance             // 插件集成管理器
        FirAnvilItemRegistry.instance           // 铁砧物品注册表
        FirRepairTableItemRegistry.instance     // 修复桌物品注册表
        FirEnchantingTableRegistry.instance     // 附魔台物品注册表
        FirBrokenGear.instance                  // 破损物品注册表
    }

    // 延迟初始化注册表实现
    fun registerLateInitListener(plugin: FirEnchantPlugin) {
        when {
            // CraftEngine
            Bukkit.getPluginManager().getPlugin("CraftEngine") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(CraftEngineLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "CraftEngine")
                }
            }
            // Nexo
            Bukkit.getPluginManager().getPlugin("Nexo") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(NexoLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "Nexo")
                }
            }
            // Oraxen
            Bukkit.getPluginManager().getPlugin("Oraxen") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(OraxenLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "Oraxen")
                }
            }
            // ItemsAdder
            Bukkit.getPluginManager().getPlugin("ItemsAdder") != null -> {
                runCatching {
                    Bukkit.getPluginManager().registerEvents(ItemsAdderLoadListener(plugin), plugin)
                    Bukkit.getConsoleSender().sendTranslatableComponent(PLUGIN_COMPATIBILITY_HOOK_SUCCESS, "ItemsAdder")
                }
            }
            else -> {
                plugin.initRegistry()
            }
        }
    }
}