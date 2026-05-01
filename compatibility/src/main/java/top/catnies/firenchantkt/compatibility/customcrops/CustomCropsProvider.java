package top.catnies.firenchantkt.compatibility.customcrops;

import net.kyori.adventure.key.Key;
import net.momirealms.customcrops.api.integration.ItemProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.catnies.firenchantkt.api.FirEnchantAPI;
import top.catnies.firenchantkt.enchantment.EnchantmentSetting;

import java.util.concurrent.ThreadLocalRandom;

public class CustomCropsProvider implements ItemProvider {

    // 魔咒|等级|成功率
    // 示例:  minecraft:mending|1|10  -- 1级的经验修补附魔书，失败率为10%
    //       minecraft:unbreaking|1..3|30..70  -- 1~3级的经验修补附魔书，失败率为30~70%
    @Override
    public @NotNull ItemStack buildItem(@NotNull Player player, @NotNull String builderString) {
        String[] split = builderString.split("\\|");
        String enchantmentName = split[0].contains("minecraft:") ? "minecraft:" + split[0] : split[0];

        // 等级和失败率
        int level;
        if (split[1].contains("~")){
            String[] levelString = split[1].split("~");
            int min = Integer.parseInt(levelString[0]);
            int max = Integer.parseInt(levelString[1]);
            level = ThreadLocalRandom.current().nextInt(max - min + 1) + min;
        } else { level = Integer.parseInt(split[1]); }

        int failure;
        if (split[2].contains("~")){
            String[] failureString = split[2].split("~");
            int min = Integer.parseInt(failureString[0]);
            int max = Integer.parseInt(failureString[1]);
            failure = ThreadLocalRandom.current().nextInt(max - min + 1) + min;
        } else { failure = Integer.parseInt(split[2]); }

        if (level <= 0 || failure <= 0)  return new ItemStack(Material.AIR);

        EnchantmentSetting settings = FirEnchantAPI.INSTANCE.getSettingsByData(Key.key(enchantmentName), level, failure, 0);
        if (settings != null) {
            return settings.toItemStack();
        }

        return new ItemStack(Material.AIR);
    }

    @Nullable
    @Override
    public String itemID(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public String identifier() {
        return "FirEnchant";
    }
}
