# Firenchants杉云附魔 ✨

![](https://img.shields.io/badge/%E6%94%AF%E6%8C%81%E7%89%88%E6%9C%AC-1.18--1.21-6772616) [![](https://img.shields.io/badge/%E6%8F%92%E4%BB%B6%E5%8F%91%E5%B8%83-MineBBS-6772616)](https://www.minebbs.com/resources/firenchant-eco.8479/) [![](https://img.shields.io/badge/%E6%8F%92%E4%BB%B6%E6%96%87%E6%A1%A3-gitbook-6772616)]() ![](https://img.shields.io/github/languages/code-size/Catnies/FirEnchant?label=代码大小) ![](https://img.shields.io/github/license/Catnies/FirEnchant?label=代码许可) 

## 📌 关于 Firenchants

Firenchants 是一款**附魔系统改革**插件，灵感来源于某国外服务器的创新设计。最初为私人服务器开发，现已对外开放。本插件彻底改造了Minecraft的附魔机制，提供丰富的附魔道具和深度玩法，特别适合想要**延长附魔毕业周期**的生存服。

### 🔥 核心特色

- **完全重写附魔台机制** - 全新的附魔书分类系统
- **附魔失败机制** - 引入破损物品和修复系统
- **多种趣味道具** - 升级符文、拓展符文等
- **兼容主流物品插件** - 支持CE等
- **真附魔兼容** - 支持ECO、ExcellentEnchant、aiyatsbus等原版注册的真实附魔
- **附魔自动导入** - 无需手动导入附魔，可自动检测所有注册原版的附魔
- **多物品同时修复** - 支持多页物品同时修复
- **多插件兼容** - 支持更多插件兼容

------

## 🔧 如何构建

### 💻 命令行

1. 安装 **JDK 21**。

2. 打开命令行并定位到项目目录。

3. 运行

   ```
   ./gradlew build
   ```

4. 在 **/target** 文件夹中找到项目。

### 🛠️ 使用 IDE

1. 将项目导入到您的 IDE 中。
2. 执行 **Gradle 构建**。
3. 在 **/target** 文件夹中找到项目。

-----------------------

## 🤝 如何贡献

### 🌍 翻译

1. 克隆此存储库。

2. 在以下位置创建新的语言文件：

   ```
   /core/src/main/resources/languages
   ```

3. 完成后，提交**拉取请求**以供审核。我们感谢您的贡献！

--------------

## ❌ 已知不兼容

- AE等基于NBT的伪附魔插件

------

## 💖 支持开发者

如果喜欢本插件，请考虑支持开发！

- **购买链接**: [MineBBS](https://www.minebbs.com/resources/firenchant-eco.8479/)
- **联系方式**: QQ 1286071831

------

## 📚 Firenchants API

### 📌 仓库配置

```
repositories {
    maven("https://repo.catnies.top/releases")
}
```

### 📌 依赖项

```
dependencies {
    compileOnly("top.catnies:firenchantkt:3.0.0")
}
```