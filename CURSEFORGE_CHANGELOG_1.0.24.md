# Dyson Cube Project Addon 1.0.24

## 中文

- 优化戴森 AE 终端的数量显示，物品格数量现在使用 AE 风格的 `K`、`M`、`G`、`T`、`P`、`E` 单位。
- 超过 AE 常用后缀范围的超大数量继续使用受控科学计数法，避免把超长整数绘制到物品格上。
- 戴森 AE 终端的悬浮框保留精确数量，并额外显示科学计数法解释，格子短显示与悬浮框精确值分离。
- 修复超大数量文本过长导致物品图标、数量文字互相覆盖的问题。
- 矿物处理中枢和相关数量面板统一使用紧凑数量格式，移除会截断有效指数的旧版 `...` 显示。
- 能耗数量显示也改用 AE 风格单位，提升大数值下的可读性。
- 英文界面的数量短显示、精确悬浮框和超大指数说明同步优化。

## English

- Improved quantity rendering in the Dyson AE terminal with AE-style `K`, `M`, `G`, `T`, `P`, and `E` suffixes.
- Extremely large values beyond the regular AE suffix range now use bounded scientific notation instead of overflowing the item slot.
- The Dyson AE terminal keeps the exact amount in the tooltip and adds a scientific-notation explanation, while the slot uses a compact value.
- Fixed long quantity labels overlapping item icons and other slot content.
- Unified compact number formatting across the Ore Processing Hub and related quantity panels; removed the old truncation that could hide meaningful exponents.
- Energy-cost values now use the same AE-style compact units for better readability at large scales.
- Synchronized the compact quantity labels, exact tooltips, and large-exponent explanations for English UI text.
