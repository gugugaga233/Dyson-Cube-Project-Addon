# Dyson Cube Project Addon 1.0.19

## 中文

- 修复高压缩等级结构材料在电磁轨道弹射器结算时卡死主线程的问题。
- `Integer.MAX_VALUE` 结算请求现在严格使用当前恒星层级和寰宇之心倍率计算出的单次结算上限，不再把超大材料储备直接送入无界 `BigInteger` 除法。
- 材料足够覆盖整个单次结算上限时，直接在 Flux `BigNumber` 稀疏表示中判定并返回上限，避免展开数百万位整数。
- 时间预算中断后的后续 tick 现在从本次结算的精确剩余量继续，不会重复计算或超过单次结算上限。
- 保留结构材料余量、恒星批量结算和寰宇之心聚合逻辑；未结算材料会安全留在储备中等待后续 tick。
- 更新 NeoForge 模组元数据版本至 `1.0.19`。

## English

- Fixed main-thread freezes when the EM Rail Ejector settled structure materials at very high compression levels.
- `Integer.MAX_VALUE` settlement requests now use the finite per-settlement limit derived from the current stellar hierarchy and Cosmic Heart effects instead of entering an unbounded `BigInteger` division.
- When the reserve can cover the complete settlement limit, the limit is returned directly through Flux `BigNumber` sparse comparison without expanding a multi-million-digit integer.
- Follow-up ticks after the time budget expires now continue from the exact remaining amount of the same settlement, preventing duplicate work and limit overruns.
- Structure reserves, aggregate stellar settlement, and Cosmic Heart batching are preserved; unprocessed material remains safely queued for later ticks.
- Updated the NeoForge mod metadata version to `1.0.19`.
