# 项目结构概述

## 1. 命令模块 (Commands)
- **LocateCommand**: 用于定位游戏中的结构，如村庄等。代码位置：`src/main/java/anticope/rejects/commands/LocateCommand.java`
- **TerrainExport**: 导出地形数据到文件。代码位置：`src/main/java/anticope/rejects/commands/TerrainExport.java`
- **ServerCommand**: 用于扫描服务器端口。代码位置：`src/main/java/anticope/rejects/commands/ServerCommand.java`
- **SaveSkinCommand**: 保存玩家的皮肤到本地文件。代码位置：`src/main/java/anticope/rejects/commands/SaveSkinCommand.java`

## 2. GUI 模块 (Graphical User Interface)
- **WMeteorModule**: 自定义的模块 GUI 组件。代码位置：`src/main/java/anticope/rejects/gui/themes/rounded/widgets/WMeteorModule.java`
- **WMeteorTextBox**: 自定义的文本框组件。代码位置：`src/main/java/anticope/rejects/gui/themes/rounded/widgets/input/WMeteorTextBox.java`
- **WMeteorDropdown**: 自定义的下拉菜单组件。代码位置：`src/main/java/anticope/rejects/gui/themes/rounded/widgets/input/WMeteorDropdown.java`
- **WMeteorButton**: 自定义的按钮组件。代码位置：`src/main/java/anticope/rejects/gui/themes/rounded/widgets/pressable/WMeteorButton.java`
- **WMeteorSection**: 自定义的折叠面板组件。代码位置：`src/main/java/anticope/rejects/gui/themes/rounded/widgets/WMeteorSection.java`
- **WMeteorTooltip**: 自定义的工具提示组件。代码位置：`src/main/java/anticope/rejects/gui/themes/rounded/widgets/WMeteorTooltip.java`

## 3. 服务器管理模块 (Server Management)
- **ServerManagerScreen**: 管理服务器的 GUI 界面。代码位置：`src/main/java/anticope/rejects/gui/servers/ServerManagerScreen.java`

## 4. 工具类 (Utilities)
- **WorldGenUtils**: 用于生成世界相关的工具类。代码位置：`src/main/java/anticope/rejects/utils/WorldGenUtils.java`
- **Seeds**: 用于管理种子的工具类。代码位置：`src/main/java/anticope/rejects/utils/seeds/Seeds.java`

## 5. 其他
- **LICENSE**: 项目的许可证文件。代码位置：`LICENSE`
- **gradlew.bat**: Gradle 的启动脚本。代码位置：`gradlew.bat`