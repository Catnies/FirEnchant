package top.catnies.firenchantkt;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.jspecify.annotations.NonNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class FirEnchantBootstrapper implements PluginBootstrap {

    /**
     * 设置系统属性, 禁止旧版格式警告
     * @param context the server provided context
     */
    @Override
    public void bootstrap(@NonNull BootstrapContext context) {
        System.setProperty("net.kyori.adventure.text.warnWhenLegacyFormattingDetected", "false");
    }

}