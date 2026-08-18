# Dyson Cube Project Addon 1.0.10

## 中文

### 寰宇之心批量奖励计划

- 将反复出现的单选奖励窗口改为批量奖励计划界面。
- 可使用 `1 / 10 / 100` 步进，为 7 种规则分别安排后续领取次数并一次确认。
- 当前完成的寰宇之心立即消费计划中的 1 次；以后每完成一颗寰宇之心自动消费 1 次，计划耗尽后才会再次弹出界面。
- 批量计划不会凭空发放奖励，每颗完成的寰宇之心仍然只对应 1 次规则奖励。
- 单次规则最多安排 1 次，已经生效后会在界面中禁用；可重复规则每次计划最多安排 1,000,000 次。
- 奖励计划及剩余次数会写入世界存档，退出或重启后不会丢失。

### 持久化修复

- 修复领取奖励后重建寰宇之心对象，导致已经应用的永久规则和可重复层数被清空的问题。
- 已生效次数现在会显示在奖励界面，并随存档持续累计。

## English

### Cosmic Heart Batch Reward Plans

- Replaced the repeatedly appearing single-choice reward screen with a batch reward planner.
- Use `1 / 10 / 100` adjustment steps to allocate future claims across all seven rules and confirm once.
- The currently completed Cosmic Heart consumes one planned choice immediately. Each future completed heart automatically consumes one more choice, and the screen returns only when the plan is exhausted.
- A batch plan does not grant free rewards: every completed Cosmic Heart still pays for exactly one rule reward.
- One-time rules can be planned at most once and become unavailable after application. Repeatable rules accept up to 1,000,000 entries per plan.
- Plans and remaining choices persist in world saves across exits and restarts.

### Persistence Fixes

- Fixed claimed rewards replacing the entire Cosmic Heart object and erasing permanent rule state and repeatable levels.
- Applied counts are now shown in the reward planner and persist correctly.
