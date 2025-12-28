package top.catnies.firenchantkt;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import lombok.SneakyThrows;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


public class FirEnchantPluginLoader implements PluginLoader {

    private static final Properties versionProperties = new Properties();
    public static String PROJECT_VERSION;
    private static String CENTRAL_REPOSITORY;

    // Develop
    private static String KOTLIN;
    private static String KOTLINX_COROUTINES;

    // Database
    private static String ORMLITE;
    private static String HIKARICP;

    // Library
    private static String RTAG;
    private static String INVUI;


    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        loadVersionProperties(classpathBuilder);

        // Repository
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("alibaba-central", "default", CENTRAL_REPOSITORY).build());
        resolver.addRepository(new RemoteRepository.Builder("xenondevs", "default", "https://repo.xenondevs.xyz/releases/").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());

        // Kotlin STD, Reflect, Coroutines
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + KOTLIN), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:" + KOTLIN), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-core:" + KOTLINX_COROUTINES), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:" + KOTLINX_COROUTINES), null));

        // RTag
        resolver.addDependency(new Dependency(new DefaultArtifact("com.saicone.rtag:rtag:" + RTAG), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.saicone.rtag:rtag-item:" + RTAG), null));

        // InvUI
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui-core:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui-kotlin:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:inventory-access-r20:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:inventory-access-r21:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:inventory-access-r22:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:inventory-access-r23:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:inventory-access-r24:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:inventory-access-r25:" + INVUI), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:inventory-access-r26:" + INVUI), null));

        // MYSQL
        resolver.addDependency(new Dependency(new DefaultArtifact("com.j256.ormlite:ormlite-core:" + ORMLITE), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.j256.ormlite:ormlite-jdbc:" + ORMLITE), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:" + HIKARICP), null));

        classpathBuilder.addLibrary(resolver);
    }

    // 加载jar包内的版本信息文件
    @SneakyThrows
    private void loadVersionProperties(PluginClasspathBuilder classpathBuilder) {
        File pluginFile = classpathBuilder.getContext().getPluginSource().toFile();
        try(JarFile jarFile = new JarFile(pluginFile)) { // 读取插件文件
            ZipEntry entry = jarFile.getEntry("dependency-version.properties"); // 寻找指定文件
            // 创建文件输入流
            if (entry != null && !entry.isDirectory()) {
                try (InputStream inputStream = jarFile.getInputStream(entry);
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream))
                {
                    versionProperties.load(bufferedInputStream); // 读取文件内容
                    // Main
                    PROJECT_VERSION = versionProperties.getProperty("project.version");
                    CENTRAL_REPOSITORY = versionProperties.getProperty("repository.central");
                    // Develop
                    KOTLIN = versionProperties.getProperty("kotlin.version");
                    KOTLINX_COROUTINES = versionProperties.getProperty("kotlinx-coroutines.version");
                    // Database
                    ORMLITE = versionProperties.getProperty("ormlite.version");
                    HIKARICP = versionProperties.getProperty("hikaricp.version");
                    // Library
                    RTAG = versionProperties.getProperty("rtag.version");
                    INVUI = versionProperties.getProperty("invui.version");
                }
            }
        }
    }
}
