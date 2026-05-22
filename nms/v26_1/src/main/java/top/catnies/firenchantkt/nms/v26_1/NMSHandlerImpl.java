package top.catnies.firenchantkt.nms.v26_1;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.catnies.firenchantkt.nms.EnchantmentRegistryType;
import top.catnies.firenchantkt.nms.NMSHandler;

import java.util.*;

import static net.minecraft.world.item.enchantment.EnchantmentHelper.filterCompatibleEnchantments;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public int getPlayerEnchantmentSeed(Player player) {
        net.minecraft.world.entity.player.Player nmsPlayer = ((CraftPlayer) player).getHandle();
        return nmsPlayer.enchantmentSeed;
    }

    @Override
    public int getEnchantmentTableBookShelf(Location location) {
        Level nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        BlockPos tablePos = new BlockPos(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        int bookshelfCount = 0;
        for (BlockPos blockPos1 : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(nmsWorld, tablePos, blockPos1)) {
                bookshelfCount++;
            }
        }

        return bookshelfCount;
    }

    @Override
    public Set<Enchantment> getEnchantmentTableEnchantmentList(World world, EnchantmentRegistryType registryType) {
        if (registryType == EnchantmentRegistryType.NMS) {
            Level nmsWorld = ((CraftWorld) world).getHandle();

            Optional<HolderSet.Named<net.minecraft.world.item.enchantment.Enchantment>> optional = nmsWorld.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
            if (optional.isEmpty()) {
                return new LinkedHashSet<>();
            }

            Set<Enchantment> enchantmentList = new LinkedHashSet<>();
            for (Holder<net.minecraft.world.item.enchantment.Enchantment> holder : optional.get()) {
                enchantmentList.add(CraftEnchantment.minecraftHolderToBukkit(holder));
            }

            return enchantmentList;
        }

        Set<Enchantment> enchantmentList = new LinkedHashSet<>();
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isTreasure()) continue;
            enchantmentList.add(enchantment);
        }
        return enchantmentList;
    }

    @Override
    public List<Map<Enchantment, Integer>> getPlayerNextEnchantmentTableResultByItemStack(Player player, int bookShelfCount, ItemStack itemStack, EnchantmentRegistryType registryType) {
        net.minecraft.world.entity.player.Player nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        RandomSource random = RandomSource.create(nmsPlayer.enchantmentSeed); // 根据玩家的附魔种子生成随机数.
        Set<Enchantment> canSelectEnchantments = getEnchantmentTableEnchantmentList(player.getWorld(), registryType); // 获取附魔台上所有的魔咒, 作为可使用的魔咒列表.

        // 根据书架获取附魔台上3个槽位的经验等级花费.
        int[] costs = new int[3];
        for (int slot = 0; slot < 3; slot++) {
            costs[slot] = this.getEnchantmentCost(random, slot, bookShelfCount);
            if (costs[slot] < slot + 1) {
                costs[slot] = 0;
            }
        }

        // 根据魔咒种子, 物品, 附魔台槽位, 槽位花费的经验等级, 筛选的魔咒列表计算这个物品的可出现的附魔.
        List<Map<Enchantment, Integer>> results = new ArrayList<>();
        for (int slot = 0; slot < costs.length; slot++) {
            int cost = costs[slot];

            List<EnchantmentInstance> enchantmentInstanceList = this.getEnchantmentList(nmsPlayer.enchantmentSeed, nmsItem, slot, cost, canSelectEnchantments);
            if (enchantmentInstanceList.isEmpty()) continue;

            // 根据结果随机取一个魔咒.
            EnchantmentInstance enchantmentInstance = enchantmentInstanceList.get(random.nextInt(enchantmentInstanceList.size()));

            Map<Enchantment, Integer> optionMap = new HashMap<>();
            Enchantment enchant = CraftEnchantment.minecraftHolderToBukkit(enchantmentInstance.enchantment());
            optionMap.put(enchant, enchantmentInstance.level());

            results.add(optionMap);
        }

        return results;
    }

    @Override
    public List<Map<Enchantment, Integer>> getPlayerNextEnchantmentTableResultByEnchantmentList(Player player, int bookShelfCount, int enchantable, Set<Enchantment> enchantmentList) {
        net.minecraft.world.entity.player.Player nmsPlayer = ((CraftPlayer) player).getHandle();
        RandomSource random = RandomSource.create(nmsPlayer.enchantmentSeed);

        int[] costs = new int[3];
        for (int slot = 0; slot < 3; slot++) {
            costs[slot] = this.getEnchantmentCost(random, slot, bookShelfCount);
            if (costs[slot] < slot + 1) {
                costs[slot] = 0;
            }
        }

        List<Map<Enchantment, Integer>> results = new ArrayList<>();
        for (int cost : costs) {
            List<EnchantmentInstance> resultList = new ArrayList<>(); // 结果集合

            // 迷之计算, 见 `net.minecraft.world.item.enchantment.EnchantmentHelper` 行 `536` .
            int level = cost;
            level += 1 + random.nextInt(enchantable / 4 + 1) + random.nextInt(enchantable / 4 + 1);
            float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
            level = Mth.clamp(Math.round(level + level * f), 1, Integer.MAX_VALUE);

            List<EnchantmentInstance> tempList = new ArrayList<>(); // 临时集合
            for (Holder<net.minecraft.world.item.enchantment.Enchantment> holder : enchantmentList.stream().map(CraftEnchantment::bukkitToMinecraftHolder).toList()) {
                net.minecraft.world.item.enchantment.Enchantment enchantment = holder.value();

                // 计算出具体的附魔, 见 `net.minecraft.world.item.enchantment.EnchantmentHelper` 行 `581` .
                for (int level1 = enchantment.getMaxLevel(); level1 >= enchantment.getMinLevel(); level1--) {
//                    if (level >= enchantment.getMinCost(level1) && level <= enchantment.getMaxCost(level1)) {
                    if (level >= enchantment.getMinCost(level1)) {
                        tempList.add(new EnchantmentInstance(holder, level1));
                        break;
                    }
                }
            }

            // 迷之计算, 见 `net.minecraft.world.item.enchantment.EnchantmentHelper` 行 `540` .
            if (!tempList.isEmpty()) {
                WeightedRandom.getRandomItem(random, tempList, EnchantmentInstance::weight).ifPresent(resultList::add);

                while (random.nextInt(50) <= level) {
                    if (!resultList.isEmpty()) filterCompatibleEnchantments(tempList, resultList.getLast());
                    if (tempList.isEmpty()) break;
                    WeightedRandom.getRandomItem(random, tempList, EnchantmentInstance::weight).ifPresent(resultList::add);
                    level /= 2;
                }
            }

            if (resultList.isEmpty()) continue;
            EnchantmentInstance enchantmentInstance = resultList.get(random.nextInt(resultList.size()));

            Map<Enchantment, Integer> optionMap = new HashMap<>();
            Enchantment enchant = CraftEnchantment.minecraftHolderToBukkit(enchantmentInstance.enchantment());
            optionMap.put(enchant, enchantmentInstance.level());

            results.add(optionMap);
        }

        return results;
    }

    @Override
    public List<Map<Enchantment, Integer>> getPlayerNextEnchantmentTableResult(Player player, int bookShelfCount, ItemStack itemStack, Set<Enchantment> enchantmentList) {
        net.minecraft.world.entity.player.Player nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        RandomSource random = RandomSource.create(nmsPlayer.enchantmentSeed);

        int[] costs = new int[3];
        for (int slot = 0; slot < 3; slot++) {
            costs[slot] = this.getEnchantmentCost(random, slot, bookShelfCount);
            if (costs[slot] < slot + 1) {
                costs[slot] = 0;
            }
        }

        List<Map<Enchantment, Integer>> results = new ArrayList<>();
        for (int slot = 0; slot < costs.length; slot++) {
            int cost = costs[slot];

            List<EnchantmentInstance> enchantmentInstanceList = this.getEnchantmentList(nmsPlayer.enchantmentSeed, nmsItem, slot, cost, enchantmentList);
            if (enchantmentInstanceList.isEmpty()) {
                continue;
            }

            EnchantmentInstance enchantmentInstance = enchantmentInstanceList.get(random.nextInt(enchantmentInstanceList.size()));

            Map<Enchantment, Integer> optionMap = new HashMap<>();
            Enchantment enchant = CraftEnchantment.minecraftHolderToBukkit(enchantmentInstance.enchantment());
            optionMap.put(enchant, enchantmentInstance.level());

            results.add(optionMap);
        }

        return results;
    }

    /**
     * 获取附魔花费
     *
     * @param random         随机数源
     * @param slot           附魔等级ID
     * @param bookShelfCount 书架数量
     * @return 附魔等级花费
     */
    private int getEnchantmentCost(RandomSource random, int slot, int bookShelfCount) {
        bookShelfCount = Math.min(bookShelfCount, 15);

        int i = random.nextInt(8) + 1 + (bookShelfCount >> 1) + random.nextInt(bookShelfCount + 1);
        if (slot == 0) {
            return Math.max(i / 3, 1);
        }

        return slot == 1 ? i * 2 / 3 + 1 : Math.max(i, bookShelfCount * 2);
    }

    /**
     * 获取附魔列表
     *
     * @param randomSeed 随机种子
     * @param itemStack  物品
     * @param slot       附魔等级ID
     * @param cost       附魔等级花费
     * @return 附魔列表
     */
    private List<EnchantmentInstance> getEnchantmentList(int randomSeed, net.minecraft.world.item.ItemStack itemStack, int slot, int cost, Set<Enchantment> enchantmentList) {
        if (enchantmentList.isEmpty()) {
            return new ArrayList<>();
        }

        RandomSource random = RandomSource.create(randomSeed + slot);
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(random, itemStack, cost, enchantmentList.stream().map(CraftEnchantment::bukkitToMinecraftHolder));
        if (itemStack.is(Items.BOOK) && list.size() > 1) {
            list.remove(random.nextInt(list.size()));
        }

        return list;
    }
}
