# Dyson Cube Project Addon 1.0.22

## 中文更新日志

### 戴森 AE 存储可靠性

- 修复戴森物品存储元件从 AE2 网络拉取同类物品时，旧存储视图可能覆盖新数量而不是继续累加的问题。
- 同步修复戴森流体存储元件的相同问题；物品和流体现在共用可靠的精确累加逻辑。
- 存储提交改为类似 Flux 电网的增量合并：保存前读取元件最新状态，再叠加本次插入或提取变化，不再用旧完整快照回写。
- 为元件内容增加轻量修订号。只有检测到其他 AE2 视图已更新元件时才重新载入，避免无意义地重复解析超大数量。
- 修复普通 AE2 接口可能根据旧视图报告过多提取量的问题；普通传输和戴森大数传输现在都以刷新后的实际结果为准。
- 达到 256 种类型上限时不再静默丢弃已经接受的传输；已有内容会完整保留，同时阻止继续加入新的类型。
- 兼容现有存档和 1.0.21 戴森元件，无需手动迁移或重新制作。

### 戴森 IO 端口贴图

- 修复戴森 IO 端口只有顶部和正面显示蓝色、其余四面贴图不一致的问题。
- 补齐侧面、背面和底面的关闭/工作状态贴图，使六个方向的外观保持一致。

## English Changelog

### Dyson AE Storage Reliability

- Fixed Dyson item storage cells replacing newer amounts with a stale snapshot when pulling matching items from an AE2 network instead of accumulating them.
- Fixed the same issue for Dyson fluid storage cells; item and fluid cells now share the same reliable exact-count accumulation path.
- Changed cell persistence to Flux-style delta merging: the latest saved cell state is loaded before applying the current insert or extract changes, rather than writing an outdated full snapshot.
- Added a lightweight content revision counter. Huge cell contents are reloaded only when another AE2 view has actually updated the cell.
- Fixed normal AE2 operations potentially reporting an extraction amount calculated from stale contents. Normal and arbitrary-precision transfers now return the refreshed amount that was actually moved.
- Transfers already accepted at the 256-type boundary are no longer silently discarded. Existing contents are preserved while additional new types remain blocked.
- Existing saves and Dyson cells created with 1.0.21 remain compatible and require no manual migration.

### Dyson IO Port Textures

- Fixed the Dyson IO Port showing blue textures only on its top and front faces while the other four faces used inconsistent textures.
- Added matching inactive and active textures for the sides, back, and bottom so all six directions render consistently.
