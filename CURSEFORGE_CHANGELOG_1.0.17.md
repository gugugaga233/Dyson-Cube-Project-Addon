# Dyson Cube Project Addon 1.0.17

## 中文

### 寰宇之心奖励

- 奖励界面每个可重复词条增加 `MAX` 按钮。
- 点击后会清空其他分配，并将本次寰宇之心的全部次数分配到当前词条。
- 修复玩家输入较小的科学计数法数量后界面仍显示待分配数量时缺少明确操作的问题。
- 保留服务端总数校验；例如 `8.8E199979` 小于 `8.82253374E199979`，不会被当作完整分配。

## English

### Cosmic Heart Rewards

- Added a `MAX` button to every repeatable reward row.
- Clicking it clears the other allocations and assigns the full current Cosmic Heart count to the selected reward.
- Improved the workflow when a manually entered scientific-notation amount is smaller than the required amount.
- The server-side coverage check remains enforced; for example, `8.8E199979` is smaller than `8.82253374E199979` and is not a complete allocation.
