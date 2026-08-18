# Dyson Cube Project Addon 1.0.9

## 中文

### 性能修复

- 修复超大戴森结构数量在发电量重算和鸿蒙之气倍率刷新时造成的严重服务器延迟。
- `AbsoluteInteger` 指数现在按 Flux 的 `2^63` 分层数字直接映射到 `BigNumber`，不再逐位调用十进制指数输入接口。
- 兼容 Torcherino 等方块加速模组：每台电磁轨道弹射器每个世界 tick 最多执行一次附属结构结算，避免同 tick 重复消耗结算时间预算。
- 超大指数仍保留 Flux 原生完整分层精度，不会转换为巨型 Java `BigInteger`，也不会截断存档数据。

### 界面优化

- 重做电磁轨道弹射器的戴森信息面板，改为紧凑的 `108 x 58` 信息区，不再覆盖输入槽、按钮和玩家背包。
- 常驻面板显示进度、发电量、结构梁、太阳帆、恒星等级和每批结算数量。
- 完整功率消耗、材料需求、等级、下一批次阈值和批处理说明移至悬浮提示，超长文本会自动裁切。

### 开发与验证

- 增加最大 Flux 指数层直接写入 `BigNumber` 的性能与兼容性回归测试。

## English

### Performance Fixes

- Fixed severe server stalls while recalculating power and refreshing Primordial Qi multipliers with extremely large Dyson structure counts.
- `AbsoluteInteger` exponents are now mapped directly from Flux's layered base-`2^63` digits into `BigNumber`, bypassing per-digit decimal exponent input.
- Added compatibility with tick accelerators such as Torcherino: each EM Rail Ejector may run addon structure settlement at most once per world tick, preventing repeated settlement budgets within the same tick.
- Huge exponents retain Flux's native layered precision without conversion to a massive Java `BigInteger` or truncation of saved data.

### UI Improvements

- Redesigned the EM Rail Ejector Dyson information panel as a compact `108 x 58` area that no longer covers machine slots, controls, or the player inventory.
- The persistent panel now shows progress, power generation, structure beams, solar sails, stellar level, and settlement batch size.
- Full power consumption, material requirements, level details, the next batch threshold, and aggregation notes are available in the tooltip. Long persistent labels are clipped cleanly.

### Development and Verification

- Added performance and compatibility regression coverage for directly mapping the maximum Flux exponent layer into `BigNumber`.
