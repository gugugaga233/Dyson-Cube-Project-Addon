# Dyson Cube Project Addon 1.0.13

> Complete changelog since the last public release, 1.0.1.
> 自上一个公开版本 1.0.1 以来的完整更新日志。

## 中文

### 1.0.2

- 修复宇宙采矿枢纽处理超大产量时约 150 ms 的主线程卡顿，以及普通物品输出反复转换完整 `AbsoluteInteger` 的问题。
- 超大整数产量改为直接写入 Flux 8 的二进制分层整数；十万位整数写入基准由约 3.5-18.5 ms 降至约 0.75-2.21 ms。
- 优化戴森寰宇中枢的大数计算与文本渲染，修复剩余矿脉质量分支的昂贵十进制转换。
- 修复 LV10000 材料使已完成戴森球发电量变为 0、换星清空储能、鸿蒙之气始终显示第 0 层，以及选择数据包总回落到第一项的问题。
- 星体数量、寰宇大道倍率和暗物质共鸣改用有界嵌套科学计数法，并在悬浮框中解释计数法和 Flux 存储式。

### 1.0.3

- 修复 LV100000 等极高压缩等级造成的持续严重卡顿；戴森功率仅在结构、星体或倍率变化时重算。
- 发电改为每 5 tick 等量合并入账，中枢每秒聚合一次，完整戴森球同步频率由每 4 tick 降至每 20 tick。
- 修复 Flux 分层指数显示为内部 `E[layer=...,index=...,digit=...]` 文本的问题。
- 总发电量、能量缓存和 BigNumber 输出端口统一支持紧凑分层指数与悬浮说明。

### 1.0.4

- 修复戴森物品存储元件被客户端摘要覆盖导致丢失内容的问题；旧数据中的重复 ID 会合并数量。
- 新增最多 256 种流体的戴森大数流体存储元件，以及物品/流体严格分离的大数 I/O 转移。
- 戴森终端槽位改为显示实际紧凑数量，不再显示十进制位数。
- 恒星最高命名等级保持 Lv.8；从 `5E9` 星体的每批 `1E9` 开始，之后每跨一个十进制门槛将批量容量乘 10。
- 中枢显示当前批量上限和下一门槛，并说明整批期望成本、最终落点星体和特殊命中规则。
- 修复鸿蒙之气在超大批量中最多触发一次，以及矿产中枢存在能量却不显示戴森缓存的问题。

### 1.0.5

- 修复电磁轨道弹射器在超大星体总数下卡死于 `AbsoluteInteger.add` 的问题。
- 动态批量数量级改为根据最高二进制位直接计算；加法只处理增量与进位，减法使用直接借位。
- 超大批量转换只保留 Flux `BigNumber` 可表达的有效精度，并在材料除法前先受结算上限约束。
- 新增每 tick `1E65536` 颗星体的单次结算安全上限；达到后 UI 明确显示封顶状态。

### 1.0.6

- 完成戴森大数流体元件与普通 AE2 流体网络的双向传输。
- 戴森 I/O 端口可在普通 AE2 物品/流体库存与对应戴森大数元件之间搬运；大数元件间保持 `AbsoluteInteger` 精确直传。
- 普通 AE2 存储使用有界 `long` 窗口、AE2 供能 API 和每 tick 扫描上限，避免卡死、覆盖及物品丢失。
- 戴森终端槽位显示如 `4E9992` 的紧凑精确数量。
- 中枢和弹射器面板显示恒星等级、每批数量及下一次 `x10` 门槛；等级统一由全局精确星体总数计算。
- 鸿蒙之气层数改为 `AbsoluteInteger`，通知显示本批命中和累计层数；跨多个里程碑时只出现一个代表整批的选择界面。
- 寰宇之心每批最多出现一个，出现时暂停剩余结算并说明材料会在下一次继续处理。

### 1.0.7

- 修复极高压缩材料触发超大 `2^N` 高精度幂运算而长时间停顿的问题。
- 超大整数到 `BigNumber` 改为固定精度对数归一化，同一批次复用转换结果；精确星体计数仍完整保留。
- 普通包裹完成不再逐次刷全服聊天和日志，关键解锁与寰宇之心事件仍会提示。
- 鸿蒙之气可从旧版 `2147483647` 上限继续增长，选择界面使用科学计数法字符串和持久化待处理状态。
- 道痕选择改为累计层数每增长 10 倍触发一次；断线后低频重发，不能用 ESC 丢弃，奖励只应用到触发玩家的戴森球。

### 1.0.8

- 修复超大暗物质共鸣在进入存档首个 tick 重算功率、单次卡顿约 4 分钟的问题。
- `AbsoluteInteger` 小常数乘除改为直接处理 Flux 的 `2^63` 分层数字，不再展开数百万位 `BigInteger`。
- 超大 `2^共鸣次数` 使用有界有效数字，存档中的共鸣和星体计数仍保持完整精度。
- 最大 Flux 指数层也能在固定时间完成重算，并降低 Torcherino 等加速模组造成阻塞的风险。

### 1.0.9

- 修复超大结构数量在发电重算和鸿蒙之气倍率刷新时造成的严重延迟。
- `AbsoluteInteger` 指数直接映射到 Flux `BigNumber` 的 `2^63` 分层数字，不再逐位输入十进制指数。
- 兼容 Torcherino：每台弹射器每个世界 tick 最多执行一次附属结算。
- 重做电磁轨道弹射器信息面板为紧凑 `108 x 58` 区域，不再遮挡槽位、按钮和背包。
- 常驻区域显示进度、发电、材料、恒星等级和批量数；完整说明与下一门槛移至悬浮提示。

### 1.0.10

- 将反复出现的寰宇之心单选窗口改为 7 项批量奖励计划界面。
- 当前完成的寰宇之心立即消费计划一次；以后每完成一颗自动消费一次，计划耗尽后才再次弹窗。
- 每颗寰宇之心仍只发放一次奖励，计划及剩余次数写入世界存档。
- 修复领取奖励时替换整个寰宇之心对象，导致永久规则和重复层数被清空的问题。
- 奖励界面新增已生效次数显示。

### 1.0.11

- 移除可重复奖励每批 `1,000,000` 次的限制。
- 计划剩余次数和已生效次数改用 Flux `AbsoluteInteger`，不受 `int` 或 `long` 上限约束。
- 每项奖励可输入最多 4096 位非负整数，并支持 `1E1000` 等科学计数法。
- 每完成一颗寰宇之心只精确减一，不循环展开超大计划；旧整数计数和 1.0.10 计划会自动迁移。

### 1.0.12

- 7 种寰宇之心奖励全部改为可重复。
- “星体数量增加 100 万”现在每次领取都会真实增加 `1,000,000` 颗星体。
- “层级解锁阈值减半”改为“所有恒星层级效果 ×2”，每次领取令聚合容量再次翻倍，并受 `1E65536` 安全上限保护。
- “反物质解锁阈值减半”改为“反物质效果 ×2”，每次领取令实际反物质产出再次翻倍。
- 修复“所有星体产出 ×2”只记录次数但未进入发电计算的问题。
- 保留旧存档键，已有阈值奖励次数会自动继承为新效果倍率。

### 1.0.13

- 修复寰宇之心奖励选择界面的标题、说明和奖励文字模糊问题。
- 调整 GUI 绘制顺序，使自定义文字与输入框一样清晰，不改变奖励计划和科学计数法输入逻辑。

## English

### 1.0.2

- Fixed an approximately 150 ms main-thread stall in huge Cosmic Mining Hub outputs and repeated full `AbsoluteInteger` conversions during normal item output.
- Huge outputs now write directly to Flux 8 layered binary integers. A 100,000-digit write benchmark dropped from roughly 3.5-18.5 ms to 0.75-2.21 ms.
- Reduced huge-number calculation and rendering overhead in the Dyson Universe Hub and removed an expensive decimal conversion from remaining-mass checks.
- Fixed LV10000 materials resetting completed-sphere generation to zero, stored energy being cleared on star changes, Primordial Qi always showing layer 0, and selection packets falling back to the first choice.
- Wrapped-star count, Cosmic Path multiplier, and Dark Matter resonance now use bounded nested scientific notation with explanatory tooltips.

### 1.0.3

- Fixed persistent severe lag at compression levels such as LV100000. Dyson power now recalculates only when structures, stars, or multipliers change.
- Generation is accumulated in equivalent five-tick batches, hub aggregation runs once per second, and full sphere synchronization changed from every 4 ticks to every 20 ticks.
- Fixed Flux layered exponents appearing as raw `E[layer=...,index=...,digit=...]` text.
- Total generation, energy buffers, and BigNumber output ports now share compact layered-exponent formatting and tooltips.

### 1.0.4

- Fixed client summaries overwriting Dyson item cells and losing contents. Duplicate IDs in legacy data are now merged.
- Added a Dyson BigNumber Fluid Storage Cell with 256 fluid types and strict item/fluid large-number I/O transfer.
- Dyson Terminal slots now show compact actual amounts instead of decimal digit counts.
- Lv.8 remains the highest named stellar tier. Starting at `1E9` per settlement at `5E9` stars, each later decimal threshold multiplies capacity by 10.
- The hub shows current capacity and the next threshold, with aggregate-cost, final-star, and special-hit rules in tooltips.
- Fixed Primordial Qi triggering only once per huge batch and the mining hub showing an empty Dyson cache despite stored energy.

### 1.0.5

- Fixed an EM Rail Ejector server freeze in `AbsoluteInteger.add` at huge wrapped-star totals.
- Dynamic batch order now derives directly from the highest binary digit. Addition touches only incoming digits and carry, while subtraction uses direct borrowing.
- Huge batches convert only to Flux `BigNumber` precision and are bounded before material division.
- Added a `1E65536` per-tick settlement safety cap with an explicit capped UI state.

### 1.0.6

- Completed bidirectional transfer between Dyson BigNumber fluid cells and ordinary AE2 fluid networks.
- The Dyson I/O Port transfers ordinary AE2 item/fluid storage to matching Dyson cells, while Dyson-to-Dyson transfers remain exact `AbsoluteInteger` operations.
- Ordinary AE2 uses bounded `long` windows, powered AE2 APIs, and per-tick scan limits to prevent freezes, overwrites, and loss.
- Dyson Terminal slots now show compact exact values such as `4E9992`.
- Hub and ejector panels show stellar tier, settlement amount, and the next `x10` threshold, derived from the exact global wrapped-star count.
- Primordial Qi layers now use `AbsoluteInteger`; notices show batch hits and cumulative layers, and multiple crossed milestones use one aggregate choice screen.
- At most one Cosmic Heart appears per batch. Remaining materials continue in the next settlement.

### 1.0.7

- Fixed long stalls from huge high-precision `2^N` calculations after inserting extremely compressed materials.
- Huge integer-to-`BigNumber` conversion now uses fixed-precision logarithmic normalization and reuses one conversion per batch while preserving exact wrapped-star counters.
- Routine wrap completions no longer flood global chat and logs; key unlocks and Cosmic Heart events still notify players.
- Primordial Qi can grow beyond legacy `2147483647` saves. Its screen uses scientific-notation strings and a persistent pending state.
- Dao Mark choices now trigger at each tenfold cumulative growth, resend slowly after reconnect, cannot be discarded with ESC, and apply only to the triggering sphere.

### 1.0.8

- Fixed huge Dark Matter resonance causing roughly four-minute power recalculations on the first world tick.
- Small-constant `AbsoluteInteger` multiplication and division now operate directly on Flux base-`2^63` digits instead of expanding million-bit `BigInteger` values.
- Huge `2^resonance` values use bounded significant digits while saved resonance and star counters remain exact.
- Maximum Flux exponent layers now recalculate in bounded time, reducing stall risk with tick accelerators such as Torcherino.

### 1.0.9

- Fixed severe delays in power recalculation and Primordial Qi multiplier refresh with huge structure counts.
- `AbsoluteInteger` exponents now map directly into Flux `BigNumber` base-`2^63` layers without per-digit decimal exponent input.
- Added Torcherino compatibility: each ejector performs addon settlement at most once per world tick.
- Redesigned the ejector information panel as a compact `108 x 58` area that no longer covers slots, controls, or inventory.
- Persistent lines show progress, generation, materials, tier, and batch size; full details and next thresholds moved to tooltips.

### 1.0.10

- Replaced repeated single-choice Cosmic Heart windows with a seven-rule batch reward planner.
- The current completed heart consumes one planned choice immediately; future hearts consume one each until the plan is exhausted.
- Every heart still grants exactly one reward, and plans persist in world saves.
- Fixed reward claims replacing the whole Cosmic Heart object and erasing permanent rules and repeatable levels.
- Applied reward counts are now visible in the planner.

### 1.0.11

- Removed the `1,000,000` per-plan limit for repeatable rewards.
- Remaining and applied counts now use Flux `AbsoluteInteger` without `int` or `long` limits.
- Each reward accepts a non-negative integer up to 4096 digits, including scientific notation such as `1E1000`.
- One completed heart performs exactly one decrement without expanding huge plans. Legacy integer counts and 1.0.10 plans migrate automatically.

### 1.0.12

- Made all seven Cosmic Heart rewards repeatable.
- “Add 1,000,000 wrapped stars” now applies the full `1,000,000` increase on every claim.
- “Halve hierarchy unlock thresholds” became “Double all stellar hierarchy effects”; every claim doubles settlement capacity again under the `1E65536` safety cap.
- “Halve the antimatter threshold” became “Double antimatter effects”; every claim doubles actual antimatter output again.
- Fixed “Double all stellar output” being recorded but not included in real generation.
- Legacy save keys are retained, so previous threshold reward counts carry over to the new effects.

### 1.0.13

- Fixed blurry title, description, and reward text on the Cosmic Heart reward selection screen.
- Adjusted GUI draw order so custom text is as crisp as input fields without changing reward planning or scientific-notation input.
