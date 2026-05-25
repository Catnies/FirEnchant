package top.catnies.firenchantkt.config

import com.google.common.base.Enums
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.FirEnchantPlugin
import top.catnies.firenchantkt.config.extern.CustomRollStrategyData
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.engine.ConfigConditionTemplate
import top.catnies.firenchantkt.item.enchantingtable.origin_book.OriginalBookData
import top.catnies.firenchantkt.item.enchantingtable.origin_book.RollStrategy
import top.catnies.firenchantkt.item.enchantingtable.origin_book.RollStrategy.*
import top.catnies.firenchantkt.item.enchantingtable.origin_book.RollStrategyData
import top.catnies.firenchantkt.item.enchantingtable.origin_book.VanillaRollStrategyData
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_MENU_STRUCTURE_ERROR
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_ORIGINAL_BOOK_INVALID_ENCHANTMENT
import top.catnies.firenchantkt.language.MessageConstants.RESOURCE_ORIGINAL_BOOK_MISSING_KEY
import top.catnies.firenchantkt.util.ConfigParser
import top.catnies.firenchantkt.util.EnchantmentUtils
import top.catnies.firenchantkt.util.ItemUtils.nullOrAir
import top.catnies.firenchantkt.util.MessageUtils.sendTranslatableComponent
import top.catnies.firenchantkt.util.ResourceCopyUtils
import top.catnies.firenchantkt.util.YamlUtils
import top.catnies.firenchantkt.util.YamlUtils.getConfigurationSectionList
import top.catnies.firenchantkt.util.resource_wrapper.ETMenuItemData
import top.catnies.firenchantkt.util.resource_wrapper.MenuItemData
import java.util.Locale

class EnchantingTableConfig private constructor():
    AbstractConfigFile("modules/enchanting_table.yml")
{
    companion object {
        @JvmStatic
        val instance by lazy { EnchantingTableConfig().apply { loadConfig() } }
    }

    val fallbackMenuStructure = arrayOf(
        "........?",
        ".I.aaaaaA",
        "...bbbbbB",
        ".X.cccccC",
    )

    /*菜单设置*/
    var REPLACE_VANILLA_ENCHANTMENT_TABLE: Boolean by ConfigProperty(true)      // 点击原版附魔台时打开新版附魔gui

    var MENU_TITLE_000: String by ConfigProperty("000")
    var MENU_TITLE_100: String by ConfigProperty("100")
    var MENU_TITLE_110: String by ConfigProperty("110")
    var MENU_TITLE_111: String by ConfigProperty("111")
    var MENU_TITLE_222: String by ConfigProperty("222")
    var MENU_TITLE_122: String by ConfigProperty("122")
    var MENU_TITLE_112: String by ConfigProperty("112")
    var MENU_TITLE_022: String by ConfigProperty("022")
    var MENU_TITLE_002: String by ConfigProperty("002")
    var MENU_TITLE_102: String by ConfigProperty("102")
    var MENU_STRUCTURE_ARRAY: Array<String> by ConfigProperty(fallbackMenuStructure)    // 菜单结构
    var MENU_INPUT_SLOT: Char by ConfigProperty('I')                                    // 放入物品的槽位
    var MENU_CUSTOM_ITEMS: Set<MenuItemData> by ConfigProperty(mutableSetOf())          // 菜单中的自定义物品

    var MENU_SHOW_ENCHANTMENT_LINE_1: ETMenuItemData? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_2: ETMenuItemData? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_3: ETMenuItemData? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_BOOK_1: ETMenuItemData? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_BOOK_2: ETMenuItemData? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_BOOK_3: ETMenuItemData? by ConfigProperty(null)

    var ENCHANT_COST_LINE_1_MIN_FAILURE: Int by ConfigProperty(0)
    var ENCHANT_COST_LINE_2_MIN_FAILURE: Int by ConfigProperty(0)
    var ENCHANT_COST_LINE_3_MIN_FAILURE: Int by ConfigProperty(0)
    var ENCHANT_COST_LINE_1_MAX_FAILURE: Int by ConfigProperty(100)
    var ENCHANT_COST_LINE_2_MAX_FAILURE: Int by ConfigProperty(100)
    var ENCHANT_COST_LINE_3_MAX_FAILURE: Int by ConfigProperty(100)
    var ENCHANT_COST_LINE_1_ACTIONS: List<ConfigActionTemplate> by ConfigProperty(listOf())
    var ENCHANT_COST_LINE_2_ACTIONS: List<ConfigActionTemplate> by ConfigProperty(listOf())
    var ENCHANT_COST_LINE_3_ACTIONS: List<ConfigActionTemplate> by ConfigProperty(listOf())
    var ENCHANT_COST_LINE_1_CONDITIONS: List<ConfigConditionTemplate> by ConfigProperty(listOf())
    var ENCHANT_COST_LINE_2_CONDITIONS: List<ConfigConditionTemplate> by ConfigProperty(listOf())
    var ENCHANT_COST_LINE_3_CONDITIONS: List<ConfigConditionTemplate> by ConfigProperty(listOf())

    /*附魔书*/
    var ORIGINAL_BOOK_MATCHES: MutableList<OriginalBookData> by ConfigProperty(mutableListOf())

    /*重生之书设置*/
    var RENEWAL_BOOK_ENABLE: Boolean by ConfigProperty(false)               // 开启重生之书道具
    var RENEWAL_BOOK_ITEM_PROVIDER: String? by ConfigProperty(null)         // 重生之书的道具提供者
    var RENEWAL_BOOK_ITEM_ID: String? by ConfigProperty(null)               // 重生之书的道具ID
    var RENEWAL_BOOK_ACTIONS: List<ConfigActionTemplate> by ConfigProperty(listOf())    // 使用后执行的动作

    /*反转之书设置*/
    var REVERSAL_BOOK_ENABLE: Boolean by ConfigProperty(false)              // 开启反转之书道具
    var REVERSAL_BOOK_ITEM_PROVIDER: String? by ConfigProperty(null)        // 反转之书的道具提供者
    var REVERSAL_BOOK_ITEM_ID: String? by ConfigProperty(null)              // 反转之书的道具ID
    var REVERSAL_BOOK_ACTIONS: List<ConfigActionTemplate> by ConfigProperty(listOf())    // 使用后执行的动作


    // 加载数据
    override fun loadConfig() {
        /*菜单设置*/
        REPLACE_VANILLA_ENCHANTMENT_TABLE = config().getBoolean("replace-vanilla-enchanting-table", true)

        MENU_TITLE_000 = config().getString("menu-setting.title-000", "000")!!
        MENU_TITLE_100 = config().getString("menu-setting.title-100", "100")!!
        MENU_TITLE_110 = config().getString("menu-setting.title-110", "110")!!
        MENU_TITLE_111 = config().getString("menu-setting.title-111", "111")!!
        MENU_TITLE_222 = config().getString("menu-setting.title-222", "222")!!
        MENU_TITLE_122 = config().getString("menu-setting.title-122", "122")!!
        MENU_TITLE_112 = config().getString("menu-setting.title-112", "112")!!
        MENU_TITLE_022 = config().getString("menu-setting.title-022", "022")!!
        MENU_TITLE_002 = config().getString("menu-setting.title-002", "002")!!
        MENU_TITLE_102 = config().getString("menu-setting.title-102", "102")!!

        try {
            config().getStringList("menu-setting.structure").toTypedArray()
            .also {
//                Structure(*it);
                // 不测了
                MENU_STRUCTURE_ARRAY = it
            } // 测试合法性然后再赋值
        } catch (exception: Exception) {
            Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_MENU_STRUCTURE_ERROR, fileName)
        }
        MENU_INPUT_SLOT = config().getString("menu-setting.input-slot", "I")?.first() ?: 'I'

        MENU_SHOW_ENCHANTMENT_LINE_1 = ETMenuItemData.getETMenuItemDataBySection(config().getConfigurationSection("menu-setting.show-enchantment-slot.line-1")!!, 'a', false, fileName)
        MENU_SHOW_ENCHANTMENT_LINE_2 = ETMenuItemData.getETMenuItemDataBySection(config().getConfigurationSection("menu-setting.show-enchantment-slot.line-2")!!, 'b', false, fileName)
        MENU_SHOW_ENCHANTMENT_LINE_3 = ETMenuItemData.getETMenuItemDataBySection(config().getConfigurationSection("menu-setting.show-enchantment-slot.line-3")!!, 'c', false, fileName)
        MENU_SHOW_ENCHANTMENT_BOOK_1 = ETMenuItemData.getETMenuItemDataBySection(config().getConfigurationSection("menu-setting.show-enchantment-slot.line-1-book")!!, 'A', true, fileName)
        MENU_SHOW_ENCHANTMENT_BOOK_2 = ETMenuItemData.getETMenuItemDataBySection(config().getConfigurationSection("menu-setting.show-enchantment-slot.line-2-book")!!, 'B', true, fileName)
        MENU_SHOW_ENCHANTMENT_BOOK_3 = ETMenuItemData.getETMenuItemDataBySection(config().getConfigurationSection("menu-setting.show-enchantment-slot.line-3-book")!!, 'C', true, fileName)

        ENCHANT_COST_LINE_1_MIN_FAILURE = config().getInt("enchant-cost.line-1.min-failure", 0)
        ENCHANT_COST_LINE_2_MIN_FAILURE = config().getInt("enchant-cost.line-2.min-failure", 0)
        ENCHANT_COST_LINE_3_MIN_FAILURE = config().getInt("enchant-cost.line-3.min-failure", 0)
        ENCHANT_COST_LINE_1_MAX_FAILURE = config().getInt("enchant-cost.line-1.max-failure", 100)
        ENCHANT_COST_LINE_2_MAX_FAILURE = config().getInt("enchant-cost.line-2.max-failure", 100)
        ENCHANT_COST_LINE_3_MAX_FAILURE = config().getInt("enchant-cost.line-3.max-failure", 100)

        /*重生之书设置*/
        REVERSAL_BOOK_ENABLE = config().getBoolean("reversal-book.enable", false)
        if (REVERSAL_BOOK_ENABLE) {
            REVERSAL_BOOK_ITEM_PROVIDER = config().getString("reversal-book.hooked-plugin", null)
            REVERSAL_BOOK_ITEM_ID = config().getString("reversal-book.hooked-id", null)
        }

        /*反转之书设置*/
        RENEWAL_BOOK_ENABLE = config().getBoolean("renewal-book.enable", false)
        if (RENEWAL_BOOK_ENABLE) {
            RENEWAL_BOOK_ITEM_PROVIDER = config().getString("renewal-book.hooked-plugin", null)
            RENEWAL_BOOK_ITEM_ID = config().getString("renewal-book.hooked-id", null)
        }

        // 自定义物品
        config().getConfigurationSection("menu-setting.custom-items")?.let { customItemsSection ->
            val customItems = mutableSetOf<MenuItemData>() // 创建结果列表
            customItemsSection.getKeys(false).forEach { itemSectionKey ->
                // 解析物品节点如 'X', '?' 等节点
                val itemSections = customItemsSection.getConfigurationSection(itemSectionKey) // 这些 key 就是 如 'X', '?' 等
                itemSections?.let { section ->
                    // 保存到结果列表里
                    customItems.add(
                        MenuItemData.getMenuItemDataBySection(section, itemSectionKey.first(), fileName)
                    )
                }
            }
            MENU_CUSTOM_ITEMS = customItems
        }

        // 条件
        ENCHANT_COST_LINE_1_CONDITIONS = config().getConfigurationSectionList("enchant-cost.line-1.conditions")
            .mapNotNull { ConfigParser.parseConditionTemplate(it, fileName, "enchant-cost.line-1.conditions") }
        ENCHANT_COST_LINE_2_CONDITIONS = config().getConfigurationSectionList("enchant-cost.line-2.conditions")
            .mapNotNull { ConfigParser.parseConditionTemplate(it, fileName, "enchant-cost.line-2.conditions") }
        ENCHANT_COST_LINE_3_CONDITIONS = config().getConfigurationSectionList("enchant-cost.line-3.conditions")
            .mapNotNull { ConfigParser.parseConditionTemplate(it, fileName, "enchant-cost.line-3.conditions") }

        ENCHANT_COST_LINE_1_ACTIONS = config().getConfigurationSectionList("enchant-cost.line-1.actions")
            .mapNotNull { ConfigParser.parseActionTemplate(it, fileName, "enchant-cost.line-1.actions") }
        ENCHANT_COST_LINE_2_ACTIONS = config().getConfigurationSectionList("enchant-cost.line-2.actions")
            .mapNotNull { ConfigParser.parseActionTemplate(it, fileName, "enchant-cost.line-2.actions") }
        ENCHANT_COST_LINE_3_ACTIONS = config().getConfigurationSectionList("enchant-cost.line-3.actions")
            .mapNotNull { ConfigParser.parseActionTemplate(it, fileName, "enchant-cost.line-3.actions") }

        // 附魔书
        loadOriginFilesFromFileSystem()

        // 重生之书
        if (REVERSAL_BOOK_ENABLE) {
            RENEWAL_BOOK_ACTIONS = config().getConfigurationSectionList("renewal-book.actions")
                .mapNotNull { ConfigParser.parseActionTemplate(it, fileName, "renewal-book.actions") }

            val testItem = YamlUtils.tryBuildItem(REVERSAL_BOOK_ITEM_PROVIDER, REVERSAL_BOOK_ITEM_ID, fileName, "enchant-soul")
            if (testItem.nullOrAir()) REVERSAL_BOOK_ENABLE = false
        }

        // 反转之书
        if (RENEWAL_BOOK_ENABLE) {
            REVERSAL_BOOK_ACTIONS = config().getConfigurationSectionList("reversal-book.actions")
                .mapNotNull { ConfigParser.parseActionTemplate(it, fileName, "reversal-book.actions") }

            val testItem = YamlUtils.tryBuildItem(RENEWAL_BOOK_ITEM_PROVIDER, RENEWAL_BOOK_ITEM_ID, fileName, "enchant-renewal")
            if (testItem.nullOrAir()) RENEWAL_BOOK_ENABLE = false
        }
    }

    // 加载读取可附魔物品的文件
    private fun loadOriginFilesFromFileSystem() {
        // 确保文件夹存在，如果不存在，就从资源文件里复制
        val originalBookDirectory = plugin.dataFolder.resolve("original_books")
        if (!originalBookDirectory.exists() || originalBookDirectory.listFiles()?.isEmpty() == true) {
            ResourceCopyUtils.copyFolder(plugin, "original_books", plugin.dataFolder)
        }
        // 将文件夹下的内容加载到 ORIGINAL_BOOK_MATCHES 中.
        this.ORIGINAL_BOOK_MATCHES = originalBookDirectory.walkTopDown()
            .maxDepth(1)
            .filter { it.isFile && it.extension == "yml" && !it.name.startsWith('_')}
            .mapNotNull { file ->
                val enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                val yaml = YamlConfiguration.loadConfiguration(file)

                val plugin = yaml.getString("hooked-plugin") ?: run {
                    sendMissingKeyWarn(file.name, "hooked-plugin")
                    return@mapNotNull null
                }
                val id = yaml.getString("hooked-id") ?: run {
                    sendMissingKeyWarn(file.name, "hooked-id")
                    return@mapNotNull null
                }

                // 策略合法性
                val rollStrategyStr = yaml.getString("roll-strategy", "")!!
                val rollStrategy = Enums.getIfPresent(RollStrategy::class.java, rollStrategyStr).orNull() ?: run {
                    FirEnchantPlugin.instance.logger.warning("插件不支持的抽取策略: $rollStrategyStr")
                    return@mapNotNull null
                }

                // 读取策略
                val rollStrategyData: RollStrategyData = when(rollStrategy) {
                    // 读取原版策略的配置
                    VANILLA -> {
                        // 读取魔咒
                        val enchantments: MutableSet<Enchantment> = mutableSetOf()
                        val importEnchantments = yaml.getStringList("vanilla-enchantments.import").fold(mutableSetOf<Enchantment>()) { acc, vanillaID ->
                            // 导入列表
                            Material.getMaterial(vanillaID.uppercase(Locale.ROOT))?.let {
                                // 获取物品对应的魔咒列表添加
                                val applicableEnchants = EnchantmentUtils.getApplicableEnchants(ItemStack(it))
                                acc.addAll(applicableEnchants)
                                return@fold acc
                            }
                            return@fold acc
                        }
                        // 导入普通魔咒
                        val removeEnchantments = mutableSetOf<Enchantment>()
                        val commonEnchantments = yaml.getStringList("vanilla-enchantments.enchantments").fold(mutableSetOf<Enchantment>()) { acc, enchantment ->
                            val remove = enchantment[0] == '!'
                            val readString = if (remove) enchantment.substring(1) else enchantment
                            val readEnchantment = enchantmentRegistry.get(Key.key(readString))
                            if (readEnchantment == null) { sendInvalidEnchantment(file.name, enchantment); acc }
                            else {
                                if (remove) removeEnchantments.add(readEnchantment)
                                else acc.add(readEnchantment)
                            }
                            return@fold acc
                        }
                        // 合并检查
                        enchantments.addAll(importEnchantments)
                        enchantments.addAll(commonEnchantments)
                        enchantments.removeAll(removeEnchantments)
                        if (enchantments.isEmpty()) {
                            sendMissingKeyWarn(file.name, "vanilla-enchantments")
                            return@mapNotNull null
                        }
                        // 返回结果
                        VanillaRollStrategyData(enchantments)
                    }
                    // 读取自定义策略的配置
                    CUSTOM -> {
                        val section = yaml.getConfigurationSection("custom-strategy")
                            ?: run {
                                sendMissingKeyWarn(file.name, "custom-strategy")
                                return@mapNotNull null
                            }
                        val data = CustomRollStrategyData.fromYamlSection(section)
                        requireNotNull(data)
                    }
                }
                // 最终策略
                OriginalBookData(plugin, id, rollStrategy, rollStrategyData)
            }.toMutableList()
    }

    // 发送缺少键的信息
    private fun sendMissingKeyWarn(fileName: String, key: String) {
        Bukkit.getConsoleSender().sendTranslatableComponent(
            RESOURCE_ORIGINAL_BOOK_MISSING_KEY,
            "original_book/$fileName",
            key
        )
    }

    // 发送无法解析的魔咒的信息
    private fun sendInvalidEnchantment(fileName: String, enchantment: String) {
        Bukkit.getConsoleSender().sendTranslatableComponent(
            RESOURCE_ORIGINAL_BOOK_INVALID_ENCHANTMENT,
            fileName,
            enchantment
        )
    }
}
