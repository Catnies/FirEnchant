package top.catnies.firenchantkt.util

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarFile

// 从插件JAR包里复制文件夹或文件到插件目录.
object ResourceCopyUtils {

    // 从资源文件路径复制单个文件到目标文件.
    fun copyFile(plugin: JavaPlugin, resourcePath: String, targetFile: File, overwrite: Boolean = false): Boolean {
        if (targetFile.exists() && !overwrite) return true
        targetFile.parentFile?.mkdirs()

        // 从jar包内读取资源文件并粘贴.
        val jarFile = getJarFile(plugin) ?: return false
        jarFile.use { jar ->
            val entry = jar.getEntry(resourcePath.replace("\\", "/"))
            if (entry != null && !entry.isDirectory) {
                jar.getInputStream(entry).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                return true
            }
            return false
        }
    }


    // 从资源文件路径复制单个文件夹到目标文件夹.
    fun copyFolder(plugin: JavaPlugin, resourcePath: String, targetDir: File, overwrite: Boolean = false): Boolean {
        if (!targetDir.exists()) targetDir.mkdirs()
        val jarFile = getJarFile(plugin) ?: return false
        jarFile.use { jar ->
            val normalizedPath = resourcePath.replace("\\", "/")
            jar.entries().asSequence()
                .filter { it.name.startsWith(normalizedPath) && !it.isDirectory }
                .forEach {
                    // 保持完整的文件夹结构
                    // 比如：languages/subfolder/file.yml -> targetDir/languages/subfolder/file.yml
                    // println(File(targetDir, it.name).absolutePath)
                    val targetFile = File(targetDir, it.name)
                    if (overwrite || !targetFile.exists()) {
                        targetFile.parentFile?.mkdirs()
                        jarFile.getInputStream(it).use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            return true
        }
    }


    // 获取插件的JAR包文件
    private fun getJarFile(plugin: JavaPlugin): JarFile? {
        val pluginFile = File(plugin.javaClass.protectionDomain.codeSource.location.toURI())
        return if (pluginFile.isFile && pluginFile.extension == "jar") JarFile(pluginFile) else null
    }
}