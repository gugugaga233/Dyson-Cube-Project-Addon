# Dyson Cube Project Addon 1.0.16

## 中文

### 寰宇之心奖励输入修复

- 修复奖励界面仍使用 `100000` 位上限，导致 `8.82253374E199979` 这类合法科学计数法无法提交的问题。
- 客户端和服务端现在共用同一套奖励数量解析与 Flux 8.0.0 容量校验。
- 奖励输入支持约 `4971500` 位的 Flux `AbsoluteInteger` 可表示范围；科学计数法不会要求在输入框中展开全部数字。
- 保留总分配必须覆盖本次全部寰宇之心命中的规则，避免少分配导致奖励丢失。
- 更新错误提示，明确说明 `8E199979` 小于 `8.82253374E199979`，以及容量限制的含义。

## English

### Cosmic Heart Reward Input Fix

- Fixed the old `100000`-digit limit that rejected valid scientific-notation values such as `8.82253374E199979`.
- Client and server now share the same reward-count parser and Flux 8.0.0 capacity check.
- Reward inputs support approximately `4,971,500` decimal digits, within the representable range of Flux `AbsoluteInteger`; scientific notation remains compact in the UI.
- The requirement that allocations cover every Cosmic Heart hit is preserved, preventing under-allocation and lost rewards.
- Updated validation text to explain that `8E199979` is smaller than `8.82253374E199979` and to clarify the capacity limit.
