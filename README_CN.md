# MapVNC - 你的 VNC 客户端，就在 Minecraft 里

![许可证](https://img.shields.io/badge/License-MIT-blue.svg)
![Minecraft 版本](https://img.shields.io/badge/Minecraft-1.19.3+-green.svg)
![状态](https://img.shields.io/badge/Status-beta-orange.svg)

[English](README.md) | 简体中文

MapVNC 是一款功能强大的 Spigot/Paper 插件，它将一个完全可交互的 VNC 客户端直接带入游戏世界。通过使用强大的 MapEngine API，它将远程桌面渲染到游戏内地图构成的动态网格上，让玩家可以在 Minecraft 中查看和控制远程计算机。

无论是用于服务器管理、远程监控，还是仅仅为了好玩，MapVNC 都为 Minecraft 和任何启用 VNC 的机器之间提供了无缝且独特的集成体验。

![MapVNC 演示截图](images/demo1.webp)
## 功能特性

- **动态显示创建**：在世界任何地方创建任意大小的 VNC 屏幕。
- **实时渲染**：查看远程桌面，并可配置颜色深度以调整性能。
- **完全交互**：
    - **鼠标控制**：在地图上指向并点击，以控制远程光标。
    - **鼠标拖拽**：基于潜行的特殊机制，模拟点击和拖拽操作。
    - **文本输入**：通过命令直接向远程会话输入文本。
    - **按键操作**：发送特殊按键事件，如回车、Shift 和 ESC。
- **游戏内管理**：无需重启服务器，即可轻松移动、调整大小和移除 VNC 显示屏。
- **基于邻近度的渲染**：显示屏仅为附近的玩家渲染，以确保最佳的服务器性能。
- **快速目标选择器**：使用 `-1` 作为 ID，可自动定位最近的屏幕。
- **详细的权限系统**：精细控制谁可以创建、管理和与显示屏交互。
- **用户友好的命令**：直观的命令和丰富的 Tab 补全功能，为用户提供引导。

## 先决条件

- **服务器软件**：Paper、Spigot 或其兼容的分支（推荐 1.21+）。
- **Java**：Java 21 或更高版本。
- **依赖项**：**[MapEngine 1.8.10+](https://modrinth.com/plugin/mapengine)** 是一个**必需**的依赖项。没有它，MapVNC 将无法启动。

## 安装

1.  确保您的服务器上已安装 **MapEngine 1.8.10+**。
2.  从 [发布页面](https://github.com/Steve3184/MapVNC/releases) 下载最新的 `MapVNC-*-all.jar` 文件。
3.  将下载的 JAR 文件放入您服务器的 `plugins` 目录中。
4.  重启或重载您的服务器。

## 命令与用法

主命令是 `/mapvnc`，可以缩写为 `/mvnc`。

---

### 定位最近的屏幕

为了方便起见，您不必总是知道显示屏的具体 ID。对于任何需要 `<id>` 的命令，您都可以使用 **`-1`** 作为一个特殊的占位符。

当您使用 `-1` 时，插件会自动查找并定位到距离您位置 **16 格半径**内最近的 VNC 显示屏。距离是从显示屏的锚点（其左上角）开始测量的。

**示例：** 站在一个屏幕附近，输入 `/mvnc connect -1` 即可连接它，完全无需先运行 `/mvnc list`。

此功能适用于：`remove`, `set`, `connect`, `disconnect`, `input`, 和 `key`。

---

### 命令列表

| 命令 | 描述 | 权限 |
| --- | --- | --- |
| `/mvnc create <x> <y> <z> <width> <height>` | 在指定坐标创建一个新的 VNC 显示屏。 | `mapvnc.command.create` |
| `/mvnc remove <id>` | 移除指定 ID 的 VNC 显示屏。 | `mapvnc.command.remove` |
| `/mvnc list` | 列出所有活动的 VNC 显示屏。 | `mapvnc.command.list` |
| `/mvnc set <id> address <ip> <port>` | 为显示屏设置 VNC 服务器地址。 | `mapvnc.command.set` |
| `/mvnc set <id> password [password]` | 为显示屏设置或清除密码。 | `mapvnc.command.set` |
| `/mvnc set <id> colordepth <8\|16\|24>` | 设置连接质量（每像素位数）。 | `mapvnc.command.set` |
| `/mvnc set <id> pos <x> <y> <z>` | 将显示屏移动到新位置。 | `mapvnc.command.set` |
| `/mvnc set <id> size <width> <height>` | 调整显示屏的大小。 | `mapvnc.command.set` |
| `/mvnc connect <id>` | 将显示屏连接到其配置的 VNC 服务器。 | `mapvnc.command.connect` |
| `/mvnc disconnect <id>` | 断开显示屏与 VNC 服务器的连接。 | `mapvnc.command.disconnect`|
| `/mvnc input <id> <text...>` | 将给定的文本输入到连接的显示屏中。 | `mapvnc.command.input` |
| `/mvnc key <id> <keyName>` | 发送单个按键操作（例如 `ENTER`, `SHIFT`）。 | `mapvnc.command.key` |

---

### 鼠标拖拽

您可以通过一种特殊的基于潜行的机制来模拟点击和拖拽（例如，选择文本或移动窗口）：

1.  **瞄准** 您想在地图上开始拖拽的位置。
2.  **按住您的潜行键**（默认为 L-SHIFT）。
3.  在保持潜行的同时，**左键点击** 地图。一个“鼠标按下”事件将被发送，并且屏幕上会出现一条ActionBar消息，确认您已进入拖拽模式。
4.  要在拖拽时移动鼠标，只需**点击地图的其他部分**。您必须保持在潜行模式。
5.  完成后，**松开潜行键**。这将发送“鼠标松开”事件，完成拖拽操作。

---

## 权限

| 权限节点 | 描述 | 默认 |
| --- | --- | --- |
| `mapvnc.*` | 授予访问所有 MapVNC 功能和命令的权限。 | OP |
| `mapvnc.command.*` | 授予访问所有 MapVNC 子命令的权限。 | OP |
| `mapvnc.command.base` | 允许使用 MapVNC 的主命令。 | OP |
| `mapvnc.command.create` | 允许创建新的 VNC 显示屏。 | OP |
| `mapvnc.command.remove` | 允许移除 VNC 显示屏。 | OP |
| `mapvnc.command.list` | 允许列出活动的 VNC 显示屏。 | OP |
| `mapvnc.command.set` | 允许修改显示屏属性（地址、大小等）。 | OP |
| `mapvnc.command.connect` | 允许将显示屏连接到 VNC 服务器。 | OP |
| `mapvnc.command.disconnect`| 允许断开显示屏的连接。 | OP |
| `mapvnc.command.input` | 允许向显示屏输入文本。 | OP |
| `mapvnc.command.key` | 允许向显示屏发送按键操作。 | OP |

## 从源代码构建

要自行编译此插件，您需要 Git 和 JDK 21+。

1.  克隆仓库：
    ```bash
    git clone https://github.com/Steve3184/MapVNC.git
    ```
2.  进入项目目录：
    ```bash
    cd MapVNC
    ```
3.  运行 Gradle 的 `shadowJar` 任务来构建插件并包含其依赖项：
    ```bash
    ./gradlew shadowJar
    ```
4.  编译后的 JAR 文件将位于 `build/libs/` 目录中。

## 许可证

本项目采用 MIT 许可证授权。详情请参阅 [LICENSE](LICENSE) 文件。

## 致谢

-   **[MapEngine](https://github.com/FroglightNET/MapEngine)**：感谢其提供了令人难以置信的 API，使游戏内渲染成为可能。
-   **[Vernacular](https://github.com/shinyhut/vernacular)**：感谢其提供了轻量级且易于使用的 Java VNC 客户端库。