package top.catnies.firenchantkt.compatibility.customfishing;

import com.saicone.rtag.RtagItem;
import net.kyori.adventure.key.Key;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.catnies.firenchantkt.api.FirEnchantAPI;
import top.catnies.firenchantkt.enchantment.EnchantmentSetting;

import java.util.concurrent.ThreadLocalRandom;

public class CustomFishingProvider implements ItemProvider {

    // 魔咒|等级|成功率
    // 示例:  minecraft:mending|1|10  -- 1级的经验修补附魔书，失败率为10%
    //       minecraft:unbreaking|1..3|30..70  -- 1~3级的经验修补附魔书，失败率为30~70%
    @NotNull
    @Override
    public ItemStack buildItem(@NotNull Context<Player> context, @NotNull String builderString) {
        String[] split = builderString.split("\\|");
        String enchantmentName = split[0].contains("minecraft:") ? split[0] : "minecraft:" + split[0];

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
        RtagItem rtagItem = RtagItem.of(itemStack);
        String enchantment = rtagItem.get("FirEnchant", "Enchantment");
        String level = rtagItem.get("FirEnchant", "Level");
        String failure = rtagItem.get("FirEnchant", "Failure");
        return enchantment + "|" + level + "|" + failure;
    }

    @Override
    public String identifier() {
        return "FirEnchant";
    }
}
