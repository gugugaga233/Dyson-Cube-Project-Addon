# Dyson Cube Project Addon 1.0.23

## 中文

### 性能优化

- 修复戴森状态每秒向所有在线玩家无条件发送完整 NBT 的问题。
- 只有当前打开界面并处于实时订阅状态的玩家才会接收同步包；没有实时查看者时完全跳过完整快照生成。
- 实时订阅与玩家保存的默认戴森球选择分离，关闭界面后自动取消实时订阅；打开界面后立即发送首个快照。
- 客户端快照只序列化当前查看的戴森球，避免把其他戴森球的大数状态一并发送。
- 为客户端同步快照加入修订号缓存，同一份状态不再重复执行 BigNumber 和 NBT 序列化。
- 移除主循环每 tick 无条件标记存档脏数据的行为，改为在真实状态批次变化时标记。
- 优化寰宇采矿中心的质量限制判断：只有产量可能超过剩余质量时才进行超大 BigDecimal 除法，减少 BigInteger.divide 带来的主线程卡顿。
- 保持产出数量、质量消耗和存档字段兼容不变。

### 兼容性

- 兼容已有 1.0.22 存档和戴森物品/流体存储元件。
- 继续使用 Flux Networks 8.0.0 API。
- AE2 仍为可选前置。

## English

### Performance

- Fixed the Dyson state being serialized into a full NBT snapshot and sent to every online player once per second.
- Dyson sync packets are now sent only to players who currently have a Dyson screen open and an active realtime subscription; snapshot generation is skipped when nobody is viewing the data.
- Realtime viewing subscriptions are separated from the player's persistent default-sphere selection. Closing the screen cancels realtime sync, while opening it sends the first snapshot immediately.
- Client snapshots serialize only the Dyson sphere being viewed, instead of copying every sphere's huge-number state.
- Added revision-based client snapshot caching so unchanged state does not repeatedly rebuild BigNumber and NBT data.
- Removed the unconditional per-tick SavedData dirty mark and now mark the save data when a real state batch changes.
- Optimized Cosmic Mining Hub mass-limit checks: the expensive huge BigDecimal division is performed only when the produced amount can reach the remaining mass, reducing main-thread BigInteger.divide stalls.
- Production amounts, mass consumption, and save data fields remain compatible.

### Compatibility

- Compatible with existing 1.0.22 worlds and Dyson item/fluid storage cells.
- Continues to use the Flux Networks 8.0.0 API.
- AE2 remains an optional dependency.
