# Dyson Cube Project Addon 1.0.6

## 中文

### 戴森 AE 与流体存储

- 修复戴森 AE 终端槽位把精确库存显示成“位数”的问题；槽位现在直接显示紧凑科学计数法，例如 `4E9992`，悬浮提示仍保留数值解释。
- 完成戴森大数流体存储元件与普通 AE2 流体网络的双向传输。
- 戴森 I/O 端口现在可在普通 AE2 物品/流体库存与对应戴森大数元件之间搬运。
- 戴森大数元件之间继续使用 `AbsoluteInteger` 精确直传；普通 AE2 存储使用有界 `long` 窗口和 AE2 供能 API，避免卡死、覆盖和物品丢失。
- 每 tick 限制普通网络扫描种类数，超大库存会连续处理，不会为一次传输展开全部大数。

### 恒星等级与聚合结算

- 戴森寰宇中枢和电磁轨道弹射器信息面板都会显示当前恒星等级、每次聚合结算数量以及下一次 `x10` 的累计星体门槛。
- 最高命名等级保持 Lv.8；Lv.8 后每达到下一档累计星体门槛，单次结算量继续扩大 10 倍。
- 界面明确标注批量规则：特殊命中按整批累计，星体界面只显示本批最后一颗落点星体。
- 修复寰宇中枢从任意第一个戴森球读取等级造成等级显示不一致的问题；等级现在始终由全局精确星体总数计算。

### 特殊事件

- 鸿蒙之气改用 `AbsoluteInteger` 保存累计层数，大批量命中不再受 `int` 上限截断。
- 鸿蒙之气通知现在同时显示本批命中次数与累计层数。
- 一批跨越多个道痕里程碑时只打开一个选择界面，并明确说明该选择代表整批结算。
- 寰宇之心每批最多出现一个；出现时暂停剩余结算，广播会说明剩余材料将在下一次继续处理。
- 包裹完成通知改用精确批量数量，并注明当前显示的星体只是本批最后落点。

## English

### Dyson AE and Fluid Storage

- Fixed Dyson AE Terminal slots showing a digit count instead of the compact exact amount. Slots now render values such as `4E9992` directly, while tooltips retain the notation explanation.
- Completed bidirectional transfer between Dyson BigNumber Fluid Storage Cells and ordinary AE2 fluid storage.
- The Dyson I/O Port can now move both item and fluid channels between ordinary AE2 storage and matching Dyson BigNumber cells.
- Dyson-to-Dyson transfers remain exact `AbsoluteInteger` operations. Ordinary AE2 storage uses bounded `long` windows and AE2 powered transfer APIs to avoid freezes, overwrites, and loss.
- Ordinary network key scanning is bounded per tick; large inventories continue over subsequent ticks without expanding the full arbitrary-precision value.

### Stellar Tiers and Aggregate Settlement

- Both the Dyson Universe Hub and EM Rail Ejector information panel now show the current stellar tier, per-settlement star count, and the next cumulative threshold that increases settlement size by `x10`.
- Lv.8 remains the highest named tier. Beyond Lv.8, each later star-count threshold continues to increase the settlement size tenfold.
- The UI now states the aggregate rule directly: special hits count across the entire batch, while only the final landing star is displayed.
- Fixed the Universe Hub deriving its tier from an arbitrary first sphere. It now always derives the tier from the exact global wrapped-star count.

### Special Events

- Primordial Qi layers now use `AbsoluteInteger`, so large aggregate hit counts are no longer truncated at the Java `int` limit.
- Primordial Qi notifications show both hits in the current batch and cumulative layers.
- When one batch crosses multiple Dao Mark milestones, a single choice screen represents the entire aggregate settlement and says so explicitly.
- At most one Cosmic Heart appears per batch. Its appearance pauses the remaining settlement, and the broadcast explains that remaining materials continue next time.
- Wrap completion messages now use the exact batch count and identify the displayed star as the batch's final landing target.
