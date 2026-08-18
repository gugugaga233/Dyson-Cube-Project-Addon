# Dyson Cube Project Addon Guide / 戴森球项目附属攻略

This guide is written for both Chinese and English players. Names may vary slightly with the language pack or the version of the original mod.

本攻略面向中文和英文玩家。不同语言包或原模组版本可能会让个别名称略有差异。

## 1. Installation / 安装

### 中文

1. 安装与当前 Minecraft、NeoForge 和原模组版本匹配的依赖。
2. 将附属模组 JAR 放入 `mods` 文件夹。
3. 首次进入世界前备份存档。更新模组时，先退出游戏，再替换 JAR。
4. 如果使用 AE2、Flux 或 BetterAE 联动功能，请同时安装对应模组；这些联动不是所有核心功能的必选前置。

### English

1. Install the dependency versions matching your Minecraft, NeoForge, and original Dyson Cube Project version.
2. Put the addon JAR in the `mods` folder.
3. Back up your world before the first launch. Always close the game before replacing a mod JAR.
4. Install AE2, Flux, or BetterAE when you want their integration features. They are not required for every core addon feature.

## 2. Dyson Sphere and Stellar Progression / 戴森球与恒星进度

### 中文

- 电磁轨道弹射器负责把太阳帆和结构材料送入戴森球流程。
- 戴森寰宇中枢会显示当前层级、已处理星体数量、每次结算的星体数量和总发电量。
- 恒星层级最高为 Lv8。随着星体数量达到更高阶段，单次处理数量会逐步提升；达到倍率阶段后，数量会按配置的乘算基数增长。
- 鸿蒙之气、寰宇之星等特殊星体可能按批次结算。若一次输入跨过多个触发点，界面会显示本次处理的批量结果，而不是把每个内部循环都弹成一条消息。
- 如果投入材料后没有反应，先确认弹射器连接到正确的戴森球、材料类型正确，并等待服务器完成一次 tick；不要在界面打开时连续快速点击。

### English

- EM-Rail Ejectors send solar sails and structure materials into the Dyson sphere process.
- The Dyson Universe Hub shows the current tier, processed star count, stars handled per settlement, and total power generation.
- Stellar progression has a maximum of Lv8. The amount processed per settlement increases at higher star-count milestones and follows the configured multiplication base.
- Special stars such as Primordial Qi and Cosmic Star may be settled in batches. When one input crosses multiple trigger points, the UI reports the batch result instead of creating one message for every internal loop.
- If inserted materials do nothing, verify the ejector is connected to the intended sphere, the item type is correct, and one server tick has passed. Avoid rapidly clicking while the screen is open.

## 3. Storage and AE Integration / 存储与 AE 联动

### 中文

- 戴森物品存储元件和戴森流体存储元件使用大数值存储，适合远超普通整数范围的数量。
- 网络向戴森元件写入时应当累加，而不是覆盖已有数量。若看到数量减少或被替换，先停止输入并保留日志、存档备份和复现步骤。
- 戴森 I/O 端口对普通 AE 存储元件执行类似 BetterAE 的高速转移；对戴森大数值元件则使用大数值转移路径。
- 戴森 AE 终端支持紧凑数量显示。界面中的 `K`、`M`、`G`、`T`、`P`、`E` 是数量单位缩写，悬浮提示通常提供更完整的数值。
- AE2 是可选联动，不安装 AE2 时仍可使用不依赖 AE2 的戴森功能。

### English

- Dyson item and fluid storage components use large-number storage for quantities far beyond ordinary integer limits.
- Writing from a network into a Dyson component should add to the existing amount rather than overwrite it. If values decrease or are replaced, stop the transfer and keep the log, a world backup, and reproduction steps.
- The Dyson I/O Port uses a BetterAE-like high-throughput path for ordinary AE storage components and a large-number path for Dyson components.
- Dyson AE Terminal uses compact number units. `K`, `M`, `G`, `T`, `P`, and `E` are unit abbreviations; tooltips normally provide a more complete value.
- AE2 integration is optional. Features that do not depend on AE2 remain available without it.

## 4. Cosmic Mining Hub / 戴森寰宇采矿中枢

### 中文

- 采矿中枢的输出端会把普通物品和流体分别送出；请使用对应的物品输出端或流体输出端。
- 对极大数量，优先使用批量操作，不要让端口逐个查询每一件物品的完整大整数值。
- 如果主线程延迟升高，暂时断开输出端，减少同时连接的容器和网络数量，再重新测试。
- 不要在大量物品持续输入时反复打开、关闭或刷新终端；先让一次批处理完成。

### English

- The Mining Hub sends items and fluids through separate output paths. Use the item output port for items and the fluid output port for fluids.
- For very large quantities, use batch operations. Avoid forcing a port to query a full huge integer for every individual stack.
- If main-thread latency rises, temporarily disconnect the output port, reduce connected inventories and network size, and test again.
- Do not repeatedly open, close, or refresh the terminal while a large transfer is still running. Let the current batch finish first.

## 5. Cosmic Heart Rewards / 寰宇之心奖励

### 中文

- 每个奖励都可以单独分配次数；输入框支持科学计数法，用于输入极大的重复次数。
- `MAX` 会把当前可分配次数全部填入选中的奖励。确认数值后再点击应用或结算。
- 结算进度会显示还需要多少次 `1e65535` 级别的安全分块，以及完成整次结算还需要多少进度。
- 寰宇之心稀有奖励统一按整次结算处理，不会把一个安全分块误当作完整结算。
- 反物质奖励表现为反物质效果倍率提升；恒星层级奖励表现为所有恒星层级效果提升；星体奖励直接增加星体数量。重复分配会累加。
- 如果 `MAX` 点击后没有变化，先确认当前奖励窗口仍连接到同一台戴森寰宇中枢，并查看服务器是否仍在处理上一批数据。

### English

- Each reward has its own repeat-count field. Scientific notation is supported for extremely large repeat counts.
- `MAX` fills the selected reward with all currently available allocations. Review the value before applying or settling it.
- Progress shows how many `1e65535`-scale safety chunks remain and how much progress is still required for the complete settlement.
- Rare Cosmic Heart rewards are resolved once per complete settlement; a safety chunk is not treated as a complete settlement.
- Antimatter rewards increase the antimatter effect multiplier, stellar rewards increase all stellar-tier effects, and star rewards directly add stars. Repeated allocations stack.
- If `MAX` appears to do nothing, verify that the reward screen is still linked to the same Dyson Universe Hub and that the previous batch has finished processing.

## 6. Large Numbers and Performance / 大数值与性能

### 中文

- 大数值显示优先使用科学计数法或紧凑单位，不要把完整指数全部绘制在窄面板里。
- 单次结算存在安全上限，用来避免一次 tick 执行过久；超过上限的数量应留在待处理进度中，后续批次继续结算，而不是强行转换成 `int`。
- 如果进入存档很慢或游戏卡死，先关闭大量持续输入的弹射器、输出端和自动化网络，再进入世界排查。
- 性能问题反馈时请提供 Minecraft 日志、崩溃报告、模组列表、模组版本、是否安装 AE2/Flux/BetterAE，以及能稳定复现的操作顺序。

### English

- Large values should use scientific notation or compact units instead of drawing the complete exponent inside a narrow panel.
- A per-settlement safety limit prevents one tick from running too long. Values beyond the limit should remain in pending progress for later batches instead of being forced into an `int`.
- If world loading is slow or the game freezes, first disable heavily loaded ejectors, output ports, and automation networks, then isolate the source.
- For a performance report, include the Minecraft log, crash report, mod list, mod versions, whether AE2/Flux/BetterAE is installed, and reliable reproduction steps.

## 7. Quick Checklist / 快速检查表

### 中文

- [ ] 原模组、附属模组、NeoForge 和 Minecraft 版本匹配。
- [ ] 已备份存档。
- [ ] 弹射器材料和目标戴森球正确。
- [ ] 物品端口与流体端口没有接反。
- [ ] 大数量转移使用批量方式。
- [ ] 看到覆盖或丢失时立即停止网络并保留日志。

### English

- [ ] Original mod, addon, NeoForge, and Minecraft versions match.
- [ ] The world has been backed up.
- [ ] Ejector materials and target Dyson sphere are correct.
- [ ] Item and fluid ports are not swapped.
- [ ] Large transfers use batch operations.
- [ ] If values are overwritten or lost, stop the network and preserve the log immediately.

