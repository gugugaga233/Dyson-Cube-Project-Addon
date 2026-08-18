# Dyson Cube Project Addon 1.0.7

## 中文

### 紧急卡顿修复

- 修复投入极高压缩等级材料后，服务器反复执行超大 `2^N` 高精度幂运算而长时间停顿的问题。
- 超大 `AbsoluteInteger` 转换为 Flux `BigNumber` 现在使用固定精度的对数归一化，耗时不再随批量指数线性增长。
- 同一次星体聚合结算会复用已转换的批量值，不再为太阳帆、结构梁和最终扣除重复转换同一个超大整数。
- 仍保留精确的 `AbsoluteInteger` 星体计数；近似只用于本来就是期望值模型的聚合材料成本。

### 日志与聊天治理

- 普通包裹完成不再向全服聊天和日志逐次广播，进度统一在戴森界面查看。
- 黑洞池、反物质池、寰宇之心等一次性关键事件仍会正常提示。
- 修复大批量结算时数千条“包裹完成”和“鸿蒙之气”消息堵塞服务器、网络与客户端聊天渲染的问题。

### 鸿蒙之气大数修复

- 鸿蒙之气累计层数继续使用 `AbsoluteInteger`，从旧版 `2147483647` 存档值开始可以继续增长。
- 道痕选择界面的层数改为科学计数法字符串，不再通过 `int` 传输或显示。
- 道痕选择增加持久化待处理状态，同一奖励不会在每个服务器 tick 重复弹窗。
- 选择频率改为累计鸿蒙层数每增长 10 倍触发一次；一次选择代表该阶段已经聚合的全部命中。
- 玩家断线后，未完成的选择会低频重新发送；选择界面不能用 ESC 意外丢弃。
- 服务端只对触发玩家对应的戴森球应用奖励，不再错误地给所有戴森球各增加一层。

## English

### Emergency Stall Fix

- Fixed long server stalls caused by repeatedly evaluating huge high-precision `2^N` powers after inserting extremely compressed materials.
- Huge `AbsoluteInteger` to Flux `BigNumber` conversion now uses fixed-precision logarithmic normalization, so conversion cost no longer grows linearly with the settlement exponent.
- A settlement reuses its converted batch value for sail checks, beam checks, and final material deduction.
- Wrapped-star counts remain exact `AbsoluteInteger` values. Approximation is limited to aggregate material cost, which already uses an expected-value model.

### Log and Chat Control

- Routine wrap completions no longer broadcast one message per settlement. Progress remains available in the Dyson interfaces.
- One-time progression events such as black-hole unlocks, antimatter unlocks, and the Cosmic Heart still notify players.
- Prevented thousands of wrap and Primordial Qi messages from blocking the server, network, and client chat renderer during large settlements.

### Primordial Qi Big-Number Fix

- Primordial Qi layers remain stored as `AbsoluteInteger`; saves previously capped at `2147483647` can continue growing.
- The Dao Mark screen now receives a compact scientific-notation string instead of an `int`.
- Dao Mark choices use a persistent pending state and cannot reopen every server tick.
- A choice is now offered when cumulative Primordial Qi grows by another factor of ten; one choice represents all aggregated hits in that stage.
- Pending choices are resent at a low frequency after reconnecting and cannot be accidentally discarded with ESC.
- Rewards now apply only to the triggering player's Dyson sphere instead of adding one layer to every sphere.
