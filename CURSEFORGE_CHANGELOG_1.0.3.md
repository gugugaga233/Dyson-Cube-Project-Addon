# Dyson Cube Project Addon 1.0.3

## 中文

### 紧急性能修复

- 修复 LV100000 等极高压缩等级使戴森系统持续严重卡顿的问题。
- 戴森球现在只在结构、星体或倍率变化时重新计算超大功率，不再让全部戴森球每 tick 重建 Flux 分层指数。
- 能量以 5 tick 为一组等量合并入账，累计发电量不会降低或改变。
- 戴森寰宇中枢的大数汇总改为每秒刷新，避免对数百个戴森球进行每 tick 重复聚合。
- 完整戴森球客户端同步由每 4 tick 调整为每 20 tick，显著减少大型存档的序列化与网络开销。

### 大数界面修复

- 修复 Flux Networks 8.0.0 分层指数显示为 `E[layer=...,index=...,digit=...]` 原始内部文本的问题。
- 总发电量、能量缓存和 BigNumber 能量输出端口现在都支持 Flux 分层指数。
- 总发电量和能量缓存悬浮框统一显示紧凑值、计数法含义和 Flux 存储式。
- 分层指数示例会显示为 `1E(8.469714182E9990)`，不会再横跨整个屏幕。

## English

### Critical Performance Fixes

- Fixed persistent severe lag after using extremely high compression levels such as LV100000.
- Dyson sphere power is now recalculated only when its structure, star, or multipliers change instead of rebuilding Flux layered exponents for every sphere every tick.
- Energy is accumulated in equivalent five-tick batches, preserving the exact total generation.
- Dyson Universe Hub aggregation now refreshes once per second instead of repeatedly aggregating hundreds of spheres every tick.
- Full Dyson sphere client synchronization now runs every 20 ticks instead of every 4 ticks, greatly reducing serialization and network overhead for large saves.

### Huge-Number Interface Fixes

- Fixed Flux Networks 8.0.0 layered exponents appearing as raw `E[layer=...,index=...,digit=...]` internal text.
- Total generation, the energy buffer, and the BigNumber Energy Output Port now support layered Flux exponents.
- Total generation and energy-buffer tooltips now consistently show the compact value, notation meaning, and Flux storage form.
- Layered values are displayed in a bounded form such as `1E(8.469714182E9990)` instead of stretching across the screen.
