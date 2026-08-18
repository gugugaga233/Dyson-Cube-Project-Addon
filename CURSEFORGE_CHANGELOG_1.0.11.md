# Dyson Cube Project Addon 1.0.11

## 中文

### 超大批量奖励计划

- 移除可重复寰宇之心奖励每批 `1,000,000` 次的限制。
- 奖励计划剩余次数和已生效次数由 `int` 改为 Flux `AbsoluteInteger`，不受 `int` 或 `long` 上限约束。
- 每种可重复奖励可直接输入最多 4096 位的非负整数，也支持 `1E1000` 等科学计数法。
- 每颗寰宇之心完成时只对计划执行一次精确减一，不会循环展开或一次性应用整个超大计划。
- 超大计划、剩余次数和已生效次数均以 Flux 分层整数写入世界存档。
- 旧版整数奖励计数及 `1.0.10` 奖励计划会自动迁移，不会清空。

## English

### Huge Batch Reward Plans

- Removed the `1,000,000` per-plan limit for repeatable Cosmic Heart rewards.
- Remaining planned choices and applied counts now use Flux `AbsoluteInteger` instead of `int`, with no `int` or `long` ceiling.
- Each repeatable reward accepts a non-negative integer of up to 4096 decimal digits, including scientific notation such as `1E1000`.
- Completing one Cosmic Heart performs exactly one decrement. Huge plans are never expanded into a settlement loop or applied all at once.
- Huge plans, remaining counts, and applied counts persist through Flux's layered integer NBT format.
- Legacy integer reward counts and plans from `1.0.10` migrate automatically without being reset.
