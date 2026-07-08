package top.catnies.firenchantkt.compatibility.auraskill;

import org.bukkit.plugin.java.JavaPlugin;
import top.catnies.firenchantkt.integration.HookPluginLoader;

public class AuraSkillLoader implements HookPluginLoader {

    private final JavaPlugin plugin;

    public AuraSkillLoader(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void load() {
//        Bukkit.getPluginManager().registerEvents(new AuraSkillListener(), plugin);
    }

}
