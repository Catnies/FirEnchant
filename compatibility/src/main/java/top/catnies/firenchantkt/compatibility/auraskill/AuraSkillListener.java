package top.catnies.firenchantkt.compatibility.auraskill;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.source.EnchantingLeveler;
import dev.aurelium.auraskills.common.api.implementation.ApiGlobalRegistry;
import net.nyana.reflection.clazz.NyanaClass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import top.catnies.firenchantkt.api.event.enchantingtable.EnchantItemEvent;

import java.lang.invoke.MethodHandle;

import static net.nyana.reflection.field.matcher.FieldMatchers.fNamed;
import static net.nyana.reflection.method.matcher.MethodMatchers.mNamed;

public class AuraSkillListener implements Listener {

    private static final AuraSkillsApi auraSkills;
    private static final AuraSkills auraSkillPlugin;
    private static final EnchantingLeveler enchantingLeveler;
    private static final MethodHandle enchantingLeveler$getSource;

    static {
        auraSkills = AuraSkillsApi.get();
        auraSkillPlugin = (AuraSkills) NyanaClass.of(ApiGlobalRegistry.class).getDeclaredNyanaField(fNamed("plugin")).mh().get(auraSkills.getGlobalRegistry());
        enchantingLeveler = auraSkillPlugin.getLevelManager().getLeveler(EnchantingLeveler.class);
        enchantingLeveler$getSource = NyanaClass.of(EnchantingLeveler.class).getDeclaredNyanaMethod(mNamed("getSource")).unreflect();
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onEnchantingTableEnchant(EnchantItemEvent event) {
//        try {
//            SkillSource<EnchantingXpSource> skillSource = (SkillSource<EnchantingXpSource>) enchantingLeveler$getSource.invoke(enchantingLeveler, event.getSetting().toItemStack());
//            if (skillSource == null) return;
//
//            EnchantingXpSource source = skillSource.source();
//            Skill skill = skillSource.skill();
//
//            if (failsChecks(event, player, event.getEnchantBlock().getLocation(), skill)) return;
//
//            User user = auraSkillPlugin.getUser(event.getPlayer());
//
//            double xp = source.getXp();
//            String unit = source.getUnit();
//            if (unit != null) {
//                if (unit.equals("{sources.units.enchant_level}")) {
//                    // Get the sum of levels of enchants added
//                    int totalLevel = event.getSetting().getLevel();
//                    xp *= totalLevel;
//                } else if (unit.equals("{sources.units.exp_requirement")) {
//                    xp *= event.getSetting().;
//                }
//            }
//
//            auraSkillPlugin.getLevelManager().addXp(user, skill, source, xp);
//        } catch (Throwable throwable) {
//            return;
//        }
    }


}
