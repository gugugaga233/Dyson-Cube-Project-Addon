# Dyson Cube Project Addon 1.0.21

## 中文

- 修复单人游戏在寰宇之心奖励界面点击 `MAX` 并应用后显示“连接丢失/网络协议错误”的问题。
- 修复戴森 AE 存储元件把超大物品数量按 Flux 原始分层 NBT 保存，导致区块数据超过 Minecraft 2 MiB NBT 读取上限的问题。
- 新增紧凑绝对整数存储格式，只保存实际二进制数据；旧版 `layer0...` 数据仍可读取并会在读取时自动迁移。
- 同步压缩戴森物品/流体元件、矿机库存、加工产物、寰宇之心和戴森球进度中的绝对整数保存数据。
- 保留网络摘要只发送数量概览，避免把完整库存内容发送到客户端。

## English

- Fixed singleplayer disconnects showing “Connection Lost / Network Protocol Error” after pressing `MAX` and applying a Cosmic Heart reward plan.
- Fixed Dyson AE storage cells writing huge item amounts using Flux's verbose layered NBT format, causing chunks to exceed Minecraft's 2 MiB NBT read limit.
- Added a compact absolute-integer format that stores only the actual binary payload; legacy `layer0...` tags remain readable and are migrated on load.
- Applied compact persistence to Dyson item/fluid cells, mining inventories, processing products, Cosmic Heart data, and Dyson sphere progress.
- Kept network summaries limited to inventory counts instead of sending full cell contents to the client.
