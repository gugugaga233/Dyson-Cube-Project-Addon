# Dyson Cube Project Addon 1.0.15

## 中文

### 核心结算

- 重做结构材料结算：投入的结构梁和太阳帆现在按整次库存直接聚合计算，不再把一次投入强制拆成 `1E65536` 安全块逐块等待。
- 保留大数运算边界保护，但不再把安全边界当作玩家需要等待的结算次数。
- 进度显示现在对应整次直接结算的完成数量，而不是单个安全块。
- 暗物质、鸿蒙之气和其他批量事件继续使用聚合数量，避免为每颗星体创建对象。

### 寰宇之心

- 一次整次直接结算中出现的全部寰宇之心命中会合并成一个带精确次数的寰宇之心目标。
- 寰宇之心的结构梁和太阳帆需求按合并命中次数精确放大。
- 奖励界面显示本次需要分配的总次数；奖励计划必须覆盖当前全部命中。
- 奖励提交会一次领取当前聚合的全部次数，支持未来奖励继续预留。
- 中枢界面增加当前寰宇之心次数和待结算次数显示。

### 兼容与稳定性

- 奖励次数输入上限提升到 100000 位，支持更大的科学计数法奖励计划。
- 更新中英文界面和 CurseForge 发布说明。
- 保持 NeoForge 1.21.1、Flux Networks 8.0.0 和原有存档 NBT 兼容迁移。

## English

### Settlement

- Reworked structure-material settlement so one direct input inventory is aggregated in one calculation instead of being forced through repeated `1E65536` safety chunks.
- Numeric safety boundaries remain available internally, but they no longer represent extra player-visible settlement waits.
- Progress now describes the full direct settlement rather than one safety chunk.
- Dark Matter, Primordial Qi, and other batch events continue to use aggregate counts without creating one object per star.

### Cosmic Heart

- Every Cosmic Heart hit from one full direct settlement is merged into one exact-count Cosmic Heart target.
- Beam and solar-sail requirements scale with the merged hit count.
- The reward screen shows the required total for the current aggregate and rejects plans that do not cover it.
- Submitting a valid plan claims all current aggregate rewards together while preserving future planned rewards.
- The Dyson Universe Hub now shows current and queued Cosmic Heart counts.

### Compatibility and stability

- Reward count input now accepts up to 100,000 decimal digits and scientific notation for larger repeatable plans.
- Updated Chinese and English localization and the CurseForge changelog.
- Keeps NeoForge 1.21.1, Flux Networks 8.0.0, and existing NBT migration compatibility.
