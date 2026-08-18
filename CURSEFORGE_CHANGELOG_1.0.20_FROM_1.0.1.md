# Dyson Cube Project Addon - Complete Changelog Since 1.0.1

## 中文

### 1.0.2

- 修复宇宙采矿枢纽处理超大产量时约 150 ms 的主线程卡顿，以及普通物品输出反复转换完整 `AbsoluteInteger` 的问题。
- 超大整数产量改为直接写入 Flux 8 的二进制分层整数，优化十万位整数写入性能。
- 优化戴森寰宇中枢的大数计算与文本渲染，修复 LV10000 材料使发电量变为 0、换星清空储能、鸿蒙之气显示错误和选择数据包回落的问题。
- 星体数量、寰宇大道倍率和暗物质共鸣改用有界嵌套科学计数法，并在悬浮框中解释计数法和 Flux 存储式。

### 1.0.3

- 修复 LV100000 等极高压缩等级造成的持续严重卡顿。
- 发电改为缓存和批量合并，中枢聚合与完整戴森球同步降频。
- 修复 Flux 分层指数显示为内部调试文本的问题。
- 总发电量、能量缓存和 BigNumber 输出端口统一支持紧凑分层指数。

### 1.0.4

- 修复戴森物品存储元件被客户端摘要覆盖导致丢失内容的问题，旧数据中的重复 ID 会合并数量。
- 新增戴森大数流体存储元件，以及物品/流体严格分离的大数 I/O 转移。
- 戴森终端槽位改为显示实际紧凑数量，不再显示十进制位数。
- 恒星最高命名等级保持 Lv.8，批量容量按十进制门槛逐步乘 10。
- 中枢显示当前批量上限和下一门槛，并说明整批成本、最终落点和特殊命中规则。
- 修复鸿蒙之气在超大批量中最多触发一次，以及矿产中枢存在能量却不显示戴森缓存的问题。

### 1.0.5

- 修复超大星体总数下电磁轨道弹射器卡死于 `AbsoluteInteger.add` 的问题。
- 动态批量数量级改为根据最高二进制位直接计算，加法只处理增量与进位，减法使用直接借位。
- 超大批量转换只保留 Flux `BigNumber` 可表达的有效精度，并在材料除法前先受结算上限约束。
- 新增每 tick `1E65536` 颗星体的单次结算安全上限，并在 UI 显示封顶状态。

### 1.0.6

- 完成戴森大数流体元件与普通 AE2 流体网络的双向传输。
- 戴森 I/O 端口可在普通 AE2 物品/流体库存与对应戴森大数元件之间搬运，大数元件间保持精确直传。
- 普通 AE2 存储使用有界窗口、AE2 供能 API 和每 tick 扫描上限，避免卡死、覆盖及物品丢失。
- 戴森终端槽位显示紧凑精确数量；中枢和弹射器显示恒星等级、每批数量及下一次 `x10` 门槛。
- 鸿蒙之气层数改用 `AbsoluteInteger`；跨多个里程碑时使用一个聚合选择界面。
- 寰宇之心每批最多出现一个，出现时暂停剩余结算并说明下次继续处理。

### 1.0.7

- 修复极高压缩材料触发超大高精度 `2^N` 运算而长时间停顿的问题。
- 超大整数到 `BigNumber` 改为固定精度对数归一化，同一批次复用转换结果，同时保留精确星体计数。
- 普通包裹完成不再逐次刷全服聊天和日志。
- 鸿蒙之气可从旧版 `2147483647` 上限继续增长，选择界面使用科学计数法和持久化待处理状态。
- 道痕选择改为累计层数每增长 10 倍触发一次，断线后低频重发，不能用 ESC 丢弃。

### 1.0.8

- 修复超大暗物质共鸣在进入存档首个 tick 重算功率造成的长时间卡顿。
- `AbsoluteInteger` 小常数乘除改为直接处理 Flux 分层数字，不再展开数百万位 `BigInteger`。
- 超大 `2^共鸣次数` 使用有界有效数字，存档中的共鸣和星体计数仍保持完整精度。
- 降低 Torcherino 等加速模组造成阻塞的风险。

### 1.0.9

- 修复超大结构数量在发电重算和鸿蒙之气倍率刷新时造成的严重延迟。
- `AbsoluteInteger` 指数直接映射到 Flux 分层数字。
- 增加 Torcherino 兼容：每台弹射器每个世界 tick 最多执行一次附属结算。
- 重做电磁轨道弹射器信息面板，常驻显示进度、发电、材料、恒星等级和批量数，详细说明移至悬浮提示。

### 1.0.10

- 将反复出现的寰宇之心单选窗口改为 7 项批量奖励计划界面。
- 当前完成的寰宇之心立即消费计划一次，未来寰宇之心逐颗消费计划。
- 修复领取奖励时替换整个寰宇之心对象导致永久规则和重复层数被清空的问题。
- 奖励界面新增已生效次数显示。

### 1.0.11

- 移除可重复奖励每批 `1,000,000` 次的限制。
- 计划剩余次数和已生效次数改用 Flux `AbsoluteInteger`，不受 `int` 或 `long` 上限约束。
- 每项奖励支持最多 4096 位非负整数和 `1E1000` 等科学计数法。
- 每完成一颗寰宇之心只精确减一，不循环展开超大计划；旧计数自动迁移。

### 1.0.12

- 7 种寰宇之心奖励全部改为可重复。
- “星体数量增加 100 万”现在每次领取都会真实增加 `1,000,000` 颗星体。
- “层级解锁阈值减半”改为“所有恒星层级效果 ×2”，并受 `1E65536` 安全上限保护。
- “反物质解锁阈值减半”改为“反物质效果 ×2”。
- 修复“所有星体产出 ×2”只记录次数但未进入发电计算的问题。
- 旧存档中的阈值奖励次数会自动继承为新效果倍率。

### 1.0.13

- 修复寰宇之心奖励选择界面的标题、说明和奖励文字模糊问题。
- 调整 GUI 绘制顺序，使自定义文字与输入框一样清晰。

### 1.0.14

- 新增电磁轨道弹射器和射线接收站的计算进度显示，区分处理中、等待材料、已完成和寰宇之心暂停状态。
- 修复多人游戏中寰宇之心奖励 UI 锁定所有玩家的问题。
- 选择期间服务端临时启用无敌保护，单人游戏奖励界面暂停世界。

### 1.0.15

- 重做结构材料结算，按整次库存聚合计算，保留批量事件的聚合数量。
- 一次整次结算中的寰宇之心命中合并为一个带精确次数的目标。
- 奖励界面显示当前聚合次数，提交时一次领取全部当前奖励。
- 奖励次数输入上限提升到 100000 位，保持 Flux 8.0.0 和存档兼容。

### 1.0.16

- 修复 `8.82253374E199979` 等合法科学计数法无法提交的问题。
- 客户端和服务端统一奖励数量解析与 Flux 8.0.0 容量校验。
- 奖励输入支持约 4,971,500 位的 `AbsoluteInteger` 可表示范围。

### 1.0.17

- 奖励界面每个可重复词条增加 `MAX` 按钮。
- `MAX` 会清空其他分配并将全部寰宇之心次数分配到当前词条。
- 保留服务端总数校验，避免科学计数法输入不足导致奖励不完整。

### 1.0.18

- 修复已生效过的可重复词条上 `MAX` 按钮点击无反应的问题。
- 同步修正 jar 内部 NeoForge 模组版本号。

### 1.0.19

- 修复 4,000,000 级压缩材料在电磁轨道弹射器中触发无界 `BigInteger` 除法导致主线程卡死的问题。
- 单次结算使用恒星层级和寰宇之心倍率上限；材料足够时直接通过 Flux `BigNumber` 判定，跳过超大整数展开。
- 时间预算中断后的后续 tick 从精确剩余量继续，避免重复结算。

### 1.0.20

- 修复 `1.0.19` 针对原模组不存在的 `processStructureReserve` 方法注入导致的启动崩溃。
- 保留超大压缩材料结算优化和单次结算限额逻辑。

### 1.0.21

- 修复寰宇之心奖励界面点击 `MAX` 后应用导致单人游戏连接丢失的问题。
- 戴森 AE 存储元件、矿机库存、加工产物、寰宇之心和戴森球进度改用紧凑绝对整数标签，避免区块 NBT 超过 2 MiB。
- 兼容读取旧版 Flux 分层标签，并在读取时自动迁移。

## English

### 1.0.2

- Fixed the approximately 150 ms main-thread stall in huge Cosmic Mining Hub outputs and repeated full `AbsoluteInteger` conversions.
- Huge outputs now write directly to Flux 8 layered integers, with faster large-number writes.
- Reduced huge-number calculation and rendering overhead in the Dyson Universe Hub.
- Fixed LV10000 power reset, stored-energy clearing on star changes, incorrect Primordial Qi display, and selection packet fallback.
- Added bounded nested scientific notation and explanatory tooltips.

### 1.0.3

- Fixed persistent severe lag at compression levels such as LV100000.
- Added cached and batched power generation, lower-frequency hub aggregation, and lower-frequency sphere synchronization.
- Fixed raw Flux layered exponent debug text in the UI.

### 1.0.4

- Fixed Dyson item storage cells being overwritten by client summaries and losing contents; legacy duplicate IDs are merged.
- Added Dyson BigNumber fluid storage and strict item/fluid large-number I/O transfer.
- Terminal slots now show compact actual amounts instead of decimal digit counts.
- Kept Lv.8 as the highest named tier and added tenfold batch thresholds.
- Added current and next batch information, aggregate-cost details, and special-hit explanations to the hub.
- Fixed Primordial Qi triggering only once in huge batches and the mining hub hiding a stored Dyson cache.

### 1.0.5

- Fixed an EM Rail Ejector freeze in `AbsoluteInteger.add` at huge wrapped-star totals.
- Derived dynamic batch order from the highest binary digit and optimized addition/subtraction.
- Bounded huge-batch conversion before material division.
- Added the `1E65536` per-tick settlement safety cap and capped UI state.

### 1.0.6

- Completed bidirectional Dyson BigNumber fluid and ordinary AE2 fluid transfer.
- Added exact Dyson-to-Dyson item/fluid transfer and bounded ordinary AE2 windows.
- Prevented freezes, overwrites, and item loss in ordinary AE2 storage paths.
- Added compact terminal amounts, stellar tier and batch details, and aggregate Primordial Qi/Cosmic Heart handling.

### 1.0.7

- Fixed long stalls from huge high-precision `2^N` calculations.
- Used fixed-precision logarithmic normalization for huge `BigNumber` conversion while preserving exact counters.
- Reduced routine chat/log spam and made Primordial Qi and Dao Mark choices persistent and reconnect-safe.

### 1.0.8

- Fixed huge Dark Matter resonance recalculation stalls on the first world tick.
- Small-constant `AbsoluteInteger` arithmetic now uses Flux layered digits instead of million-digit `BigInteger` expansion.
- Bounded huge `2^resonance` power calculations and reduced acceleration-mod compatibility risks.

### 1.0.9

- Fixed severe delays in huge structure power recalculation and Primordial Qi multiplier refresh.
- Mapped `AbsoluteInteger` exponents directly into Flux layered numbers.
- Added Torcherino compatibility with one addon settlement per ejector per world tick.
- Redesigned the EM Rail Ejector panel with persistent progress, power, materials, tier, and batch information.

### 1.0.10

- Replaced repeated single-choice Cosmic Heart windows with a seven-rule batch reward planner.
- Added persisted plan consumption, applied-count display, and safe Cosmic Heart object updates.

### 1.0.11

- Removed the 1,000,000 repeatable-reward limit.
- Migrated remaining and applied counts to Flux `AbsoluteInteger`.
- Added large scientific-notation input and exact one-at-a-time heart decrement with legacy migration.

### 1.0.12

- Made all seven Cosmic Heart rewards repeatable.
- Made the 1,000,000 star reward apply on every claim.
- Changed threshold-halving rewards into stellar-effect and antimatter-effect doubling.
- Fixed stellar output doubling being recorded but not applied to generation.

### 1.0.13

- Fixed blurry Cosmic Heart reward titles, descriptions, and reward text by correcting GUI draw order.

### 1.0.14

- Added exact calculation-progress displays for EM Rail Ejectors and Ray Receivers.
- Fixed multiplayer reward UI locking every player.
- Added temporary selector invulnerability and singleplayer pause behavior.

### 1.0.15

- Reworked structure settlement to aggregate a direct inventory input and preserve aggregate special events.
- Merged all Cosmic Heart hits from one aggregate settlement and claimed the current aggregate in one submission.
- Increased reward input support to 100,000 digits while keeping Flux 8.0.0 and save compatibility.

### 1.0.16

- Fixed valid scientific notation such as `8.82253374E199979` being rejected.
- Unified client/server reward parsing and Flux 8.0.0 capacity validation.
- Increased reward input support to approximately 4,971,500 representable digits.

### 1.0.17

- Added a `MAX` button to every repeatable reward row.
- `MAX` clears other allocations and assigns the full current Cosmic Heart count to the selected entry.
- Preserved server-side coverage validation.

### 1.0.18

- Fixed `MAX` doing nothing on previously applied repeatable rewards.
- Synchronized the internal NeoForge metadata version.

### 1.0.19

- Fixed unbounded `BigInteger` division freezing the server when the EM Rail Ejector handled level-4,000,000 compressed materials.
- Added finite settlement limits from stellar hierarchy and Cosmic Heart effects, with a sparse Flux `BigNumber` fast path that skips huge integer expansion.
- Continued time-budgeted settlements from exact remaining work without duplicate processing.

### 1.0.20

- Fixed the startup crash caused by the `1.0.19` injection targeting the nonexistent `processStructureReserve` method in the original mod.
- Preserved the huge-compression settlement optimization and finite per-settlement limit.

### 1.0.21

- Fixed singleplayer disconnects after applying a Cosmic Heart reward plan with `MAX`.
- Added compact absolute-integer tags for Dyson AE cells, mining inventories, processing products, Cosmic Heart data, and Dyson sphere progress to keep chunk NBT below 2 MiB.
- Legacy Flux layered tags remain readable and are migrated during load.
