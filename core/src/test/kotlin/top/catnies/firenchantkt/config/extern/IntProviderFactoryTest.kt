package top.catnies.firenchantkt.config.extern

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Test
import java.io.File

class IntProviderFactoryTest {
    @Test
    fun test() {
        val testFile = File(
            this::class.java.classLoader.getResource("IntProviderTestResource.yml")!!.toURI()
        )
        val yaml = YamlConfiguration.loadConfiguration(testFile)
        (yaml.get("map") as? Map<*, *>)?.let {
            val p = IntProviderFactory.fromMap(it)
            println(p)
        }
        (yaml.get("map") as? ConfigurationSection)?.let {
            val p = IntProviderFactory.fromYaml(it)
            println(p)
        }
    }

}