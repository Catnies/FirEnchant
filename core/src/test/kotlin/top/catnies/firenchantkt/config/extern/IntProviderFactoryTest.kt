package top.catnies.firenchantkt.config.extern

import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.random.Random

class IntProviderFactoryTest {
    @Test
    fun test() {
        val testFile = File(
            this::class.java.classLoader.getResource("IntProviderTestResource.yml")!!.toURI()
        )
        val yaml = YamlConfiguration.loadConfiguration(testFile)

//
//        testNode(requireNotNull(yaml.get("weighted-int-provider-for-test")))
//        testNode(requireNotNull(yaml.get("const-int-provider-for-test")))
//        testNode(requireNotNull(yaml.get("uniform-int-provider-for-test")))
//        testNode(requireNotNull(yaml.get("normal-int-provider-for-test")))

        val list = requireNotNull(yaml.getList("list"))
        testNode(requireNotNull(list[0]))
        testNode(requireNotNull(list[1]))
        testNode(requireNotNull(list[2]))
        testNode(requireNotNull(list[3]))

    }

    fun testNode(node: Any) {
        val p = IntProviderFactory.fromNode(node)
        requireNotNull(p)
        testOutput("IntProvider=$p")

        val random = Random.Default
        val value = p.value(random)
        testOutput("结果=$value")
    }

    fun testOutput(s: Any?) {
        println("[Test]:$s")
    }
}