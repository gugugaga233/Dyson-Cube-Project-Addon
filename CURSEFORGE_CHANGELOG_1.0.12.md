# Dyson Cube Project Addon 1.0.12

## 中文

### 寰宇之心奖励重制

- 7 种寰宇之心奖励现在全部可重复选择，并继续支持最多 4096 位整数及科学计数法的批量计划。
- “星体数量直接增加 100 万颗”现在每次领取都会真实增加 1,000,000 颗星体，不再只是记录奖励次数。
- “层级解锁阈值全部减半”改为“所有恒星层级效果 ×2”；每次领取会令恒星层级的单次聚合结算容量再次翻倍。
- 恒星层级效果采用有界大数计算，达到 `1E65536` 的单次结算安全上限后显示并保持封顶，不会按奖励次数循环计算。
- “反物质解锁阈值减半”改为“反物质效果 ×2”；每次领取会令实际反物质产出再次翻倍。
- 修复“所有星体产出 ×2”只记录次数、未进入发电计算的问题；现在使用 Flux `BigNumber` 精确参与最终功率计算。
- 保留旧奖励的存档键，已有“阈值减半”奖励次数会自动继承为新的效果倍率。

## English

### Cosmic Heart Reward Rework

- All seven Cosmic Heart rewards are now repeatable and remain compatible with batch plans of up to 4096 digits and scientific notation.
- "Add 1,000,000 wrapped stars" now applies the full 1,000,000-star increase every time it is claimed instead of only recording a reward count.
- "Halve all hierarchy unlock thresholds" is now "Double all stellar hierarchy effects". Each claim doubles the hierarchy settlement capacity again.
- Hierarchy scaling uses bounded big-number arithmetic. It stops cleanly at the `1E65536` per-settlement safety cap without iterating over reward levels.
- "Halve the antimatter unlock threshold" is now "Double antimatter effects". Each claim doubles actual antimatter output again.
- Fixed "Double all stellar output" being recorded but not applied. It now participates in final power generation through Flux `BigNumber` arithmetic.
- Legacy reward save keys are retained, so previously claimed threshold rewards automatically carry over as the new effect multipliers.
