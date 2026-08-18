# Dyson Cube Project Addon 1.0.4

## 中文

### 戴森存储与 AE2

- 修复戴森物品存储元件可能被客户端摘要覆盖、导致已有内容丢失的问题；元件内容现在使用完整服务端权威数据同步。
- 读取旧数据时会合并重复条目，不再让后出现的同 ID 条目覆盖先前数量。
- 新增戴森大数流体存储元件，支持最多 256 种流体和 Flux `AbsoluteInteger` 精确数量。
- 戴森 I/O 端口现在可在矿产流体输出端口与戴森流体元件之间进行大数整批转移，并严格区分物品盘和流体盘。
- 戴森终端槽位显示实际紧凑数量，不再显示十进制位数；悬浮框补充计数法含义与 Flux 存储式。

### 恒星层级与批量结算

- 恒星星级仍以 Lv.8 星寰为最高命名等级，但 Lv.8 后批量容量会继续成长。
- 在累计 `5E9` 颗星体时每次最多结算 `1E9` 颗；达到 `5E10` 后提升为 `1E10`，以后每跨一个十进制数量级继续乘 10。
- 戴森寰宇中枢现在直接显示本次结算上限，悬浮框显示下一次乘 10 的累计星体门槛。
- 批量结算说明已明确：材料按整批期望成本处理，只显示最后一颗落点星体；暗物质和鸿蒙之气按整批命中数结算，寰宇之心仍为唯一事件。
- 修复超大批量中鸿蒙之气最多只触发一次的问题，现在可在一次聚合结算中累计多次命中。

### 界面修复

- 修复戴森寰宇矿产中枢存在真实戴森能量缓存时，`戴森缓存` 数值仍显示为空的问题。
- 缓存值统一使用支持 Flux 8 分层指数的紧凑大数格式，并为空字段提供安全回退。

## English

### Dyson Storage and AE2

- Fixed Dyson item cells being overwritten by a client-side summary, which could discard existing contents. Cell data now uses complete server-authoritative synchronization.
- Duplicate IDs in legacy cell data are merged instead of allowing later entries to overwrite earlier amounts.
- Added the Dyson BigNumber Fluid Storage Cell with up to 256 fluid types and exact Flux `AbsoluteInteger` amounts.
- The Dyson I/O Port can now transfer huge exact fluid amounts between mining fluid output ports and Dyson fluid cells while strictly separating item and fluid cells.
- Dyson Terminal slots now show the actual compact amount instead of its decimal digit count. Tooltips include notation meaning and the Flux storage form.

### Stellar Tiers and Batch Settlement

- Lv.8 Star Universe remains the highest named tier, while its batch capacity continues to grow beyond Lv.8.
- At `5E9` wrapped stars, each settlement handles up to `1E9`; at `5E10`, this becomes `1E10`, and each later decimal order multiplies the cap by 10 again.
- The Dyson Universe Hub now displays the current settlement cap and shows the next tenfold threshold in its tooltip.
- The UI now explains aggregate settlement: material cost is evaluated for the whole batch and only the final landing star is shown. Dark Matter and Primordial Qi count all batch hits, while the Cosmic Heart remains unique.
- Fixed Primordial Qi being limited to one trigger per huge aggregate batch. Multiple hits can now be credited in one settlement.

### Interface Fixes

- Fixed the Cosmic Mining Hub showing a blank Dyson cache value even when the shared Dyson energy ledger contained energy.
- Cache values now consistently use the compact huge-number formatter with Flux 8 layered-exponent support and a safe fallback for empty packet fields.
