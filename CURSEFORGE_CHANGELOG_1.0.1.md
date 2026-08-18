# Dyson Cube Project Addon 1.0.1

## 中文更新日志

### 性能与稳定性

- 修复宇宙采矿枢纽处理十万位目标质量时，服务器线程长时间卡死的问题。
- 修复矿物处理枢纽将超大 `AbsoluteInteger` 转换为 Flux `BigNumber` 时持续占满服务器线程的问题。
- 为矿物处理能耗增加大数精度限制与结果缓存，避免每 tick 重复执行昂贵转换。
- 优化宇宙采矿目标的网络同步和存档格式，避免发送超长数字字符串。

### 戴森 AE 存储

- 修复戴森大数存储元件内容超过 2 MiB 后，触发 `container_set_slot` 解码失败并断开客户端的问题。
- 完整精确数量仍保存在世界存档中；客户端现在只接收紧凑的种类数和总量摘要。
- 戴森 IO 端口的大数传输改为整批只持久化一次，显著减少大量物品类型传输时的卡顿。
- 保持对 1.0.0 世界和已有戴森存储元件的兼容，无需清空或重新制作存储元件。

### 显示与工具

- 改进极大数字的科学计数法显示，支持嵌套指数的紧凑格式。
- 优化戴森终端、采矿界面和悬浮提示中的超大数量显示。
- 新增管理员指令，可给予玩家任意压缩等级的模组物品：

```mcfunction
/dysoncubeproject_addon give_compressed <玩家> <类型> <等级> [数量]
```

### 兼容性

- Minecraft 1.21.1
- NeoForge 21.1.64 或更高版本
- Dyson Cube Project 1.0.5 或更高版本
- Flux Networks 8.0.0
- AE2 19.2.17 或更高版本为可选前置，仅 AE 功能需要

## English Changelog

### Performance and Stability

- Fixed a server-thread freeze in the Cosmic Mining Hub when processing target masses with tens of thousands of digits.
- Fixed sustained server-thread stalls while the Ore Processing Hub converted huge `AbsoluteInteger` values to Flux `BigNumber` values.
- Added precision limiting and result caching for huge ore-processing energy costs, avoiding expensive conversions every tick.
- Optimized cosmic mining target synchronization and persistence to prevent oversized decimal strings from being sent over the network.

### Dyson AE Storage

- Fixed client disconnects caused by Dyson BigNumber Storage Cell contents exceeding Minecraft's 2 MiB `ItemStack` NBT packet limit.
- Full exact amounts remain stored in the world save; clients now receive only a compact type-count and total-amount summary.
- Batched Dyson IO Port exact transfers so a complete transfer is persisted once instead of once per item type.
- Existing 1.0.0 worlds and Dyson storage cells remain compatible and do not need to be emptied or recreated.

### Display and Tools

- Improved scientific notation for extremely large values, including compact nested-exponent formatting.
- Improved huge-number presentation in the Dyson terminal, mining screens, and tooltips.
- Added an administrator command for giving mod items at arbitrary compression levels:

```mcfunction
/dysoncubeproject_addon give_compressed <player> <type> <level> [count]
```

### Compatibility

- Minecraft 1.21.1
- NeoForge 21.1.64 or newer
- Dyson Cube Project 1.0.5 or newer
- Flux Networks 8.0.0
- AE2 19.2.17 or newer is optional and only required for AE features
