package top.catnies.firenchantkt.config

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.catnies.firenchantkt.engine.ConfigActionTemplate
import top.catnies.firenchantkt.engine.ConfigConditionTemplate
import top.catnies.firenchantkt.item.enchantingtable.OriginalBookData
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
import xyz.xenondevs.invui.gui.structure.Structure

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
    var MENU_CUSTOM_ITEMS: Map<Char, Pair<ItemStack?, List<ConfigActionTemplate>>> by ConfigProperty(emptyMap())    // 菜单中的自定义物品

    var MENU_SHOW_ENCHANTMENT_LINE_1_SLOT: Char by ConfigProperty('a')
    var MENU_SHOW_ENCHANTMENT_LINE_2_SLOT: Char by ConfigProperty('b')
    var MENU_SHOW_ENCHANTMENT_LINE_3_SLOT: Char by ConfigProperty('c')
    var MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_SLOT: Char by ConfigProperty('A')
    var MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_SLOT: Char by ConfigProperty('B')
    var MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_SLOT: Char by ConfigProperty('C')

    var MENU_SHOW_ENCHANTMENT_LINE_1_ONLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_2_ONLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_3_ONLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_1_OFFLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_2_OFFLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_3_OFFLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_ONLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_ONLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_ONLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_OFFLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_OFFLINE: ConfigurationSection? by ConfigProperty(null)
    var MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_OFFLINE: ConfigurationSection? by ConfigProperty(null)

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
        REPLACE_VANILLA_ENCHANTMENT_TABLE = config().getBoolean("replace-vanilla-enchantment-table", true)

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
        try { config().getStringList("menu-setting.structure").toTypedArray()
            .also { Structure(*it); MENU_STRUCTURE_ARRAY = it } // 测试合法性然后再赋值
        } catch (exception: Exception) {
            Bukkit.getConsoleSender().sendTranslatableComponent(RESOURCE_MENU_STRUCTURE_ERROR, fileName) }
        MENU_INPUT_SLOT = config().getString("menu-setting.input-slot", "I")?.first() ?: 'I'

        MENU_SHOW_ENCHANTMENT_LINE_1_SLOT = config().getString("menu-setting.show-enchantment-slot.line-1.slot", "a")?.first() ?: 'a'
        MENU_SHOW_ENCHANTMENT_LINE_2_SLOT = config().getString("menu-setting.show-enchantment-slot.line-2.slot", "b")?.first() ?: 'b'
        MENU_SHOW_ENCHANTMENT_LINE_3_SLOT = config().getString("menu-setting.show-enchantment-slot.line-3.slot", "c")?.first() ?: 'c'
        MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_SLOT = config().getString("menu-setting.show-enchantment-slot.line-1-book.slot", "A")?.first() ?: 'A'
        MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_SLOT = config().getString("menu-setting.show-enchantment-slot.line-2-book.slot", "B")?.first() ?: 'B'
        MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_SLOT = config().getString("menu-setting.show-enchantment-slot.line-3-book.slot", "C")?.first() ?: 'C'

        MENU_SHOW_ENCHANTMENT_LINE_1_ONLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-1.online")
        MENU_SHOW_ENCHANTMENT_LINE_2_ONLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-2.online")
        MENU_SHOW_ENCHANTMENT_LINE_3_ONLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-3.online")
        MENU_SHOW_ENCHANTMENT_LINE_1_OFFLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-1.offline")
        MENU_SHOW_ENCHANTMENT_LINE_2_OFFLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-2.offline")
        MENU_SHOW_ENCHANTMENT_LINE_3_OFFLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-3.offline")
        MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_ONLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-1-book.online")
        MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_ONLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-2-book.online")
        MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_ONLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-3-book.online")
        MENU_SHOW_ENCHANTMENT_LINE_1_BOOK_OFFLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-1-book.offline")
        MENU_SHOW_ENCHANTMENT_LINE_2_BOOK_OFFLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-2-book.offline")
        MENU_SHOW_ENCHANTMENT_LINE_3_BOOK_OFFLINE = config().getConfigurationSection("menu-setting.show-enchantment-slot.line-3-book.offline")

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
    }

    // 等待注册表完成后延迟加载的部分
    override fun loadLatePartConfig() {
        // 自定义物品
        config().getConfigurationSection("menu-setting.custom-items")?.let { customItemsSection ->
            val customItems = mutableMapOf<Char, Pair<ItemStack?, List<ConfigActionTemplate>>>() // 创建结果列表
            customItemsSection.getKeys(false).forEach { itemSectionKey ->
                // 解析物品节点如 'X', '?' 等节点
                val itemSection = customItemsSection.getConfigurationSection(itemSectionKey) // 这些 key 就是 如 'X', '?' 等
                itemSection?.let { section ->
                    // 使用节点构建物品
                    val itemStack = ConfigParser.parseItemFromConfig(section, fileName, itemSectionKey)
                    // 获取动作节点, 解析动作
                    val actionList = section.getConfigurationSectionList("click-actions")
                    val actionTemplates = actionList.mapNotNull { actionNode ->
                        ConfigParser.parseActionTemplate(actionNode, fileName, itemSectionKey)
                    }
                    if (itemStack.nullOrAir()) return@forEach // 如果物品是空则跳过保存, 延迟处理是想要继续解析物品动作一类的并给予警告, 虽然物品无效整个节点都无效就是了.
                    // 保存到结果列表里
                    customItems[itemSectionKey.first()] = itemStack to actionTemplates
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
        originalBookDirectory.walkTopDown()
            .maxDepth(1)
            .filter { it.isFile && it.extension == "yml" }
            .forEach { file ->
                val enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                val yaml = YamlConfiguration.loadConfiguration(file)

                val plugin = yaml.getString("hooked-plugin")
                if (plugin == null) sendMissingKeyWarn(file.name, "hooked-plugin")
                val id = yaml.getString("hooked-id")
                if (id == null) sendMissingKeyWarn(file.name, "hooked-id")
                val enchantable = yaml.getInt("enchantable", -1)

                val enchantmentStringList = yaml.getStringList("enchantment-list")
                val enchantments = enchantmentStringList.fold(mutableSetOf<Enchantment>()) { acc, enchantment ->
                    // 导入列表
                    if (enchantment.startsWith("import:")) {
                        val vanillaID = enchantment.substring(7).uppercase()
                        Material.getMaterial(vanillaID)?.let {
                            // 获取物品对应的魔咒列表添加
                            val applicableEnchants = EnchantmentUtils.getApplicableEnchants(ItemStack(it))
                            acc.addAll(applicableEnchants)
                            return@fold acc
                        }
                    }
                    // 导入普通魔咒
                    val readEnchantment = enchantmentRegistry.get(Key.key(enchantment))
                    if (readEnchantment == null) { sendInvalidEnchantment(file.name, enchantment); acc }
                    else { acc.add(readEnchantment) }
                    return@fold acc
                }

                if (enchantments.isEmpty()) {
                    sendMissingKeyWarn(file.name, "enchantment-list")
                    return@forEach
                }

                if (plugin != null && id != null) {
                    ORIGINAL_BOOK_MATCHES.add(
                        OriginalBookData(plugin, id, enchantable, enchantments)
                    )
                }
            }
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