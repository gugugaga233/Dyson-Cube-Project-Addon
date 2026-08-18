# Dyson Cube Project Addon 1.0.2

## 中文

### 性能优化

- 修复宇宙采矿枢纽处理超大产量时可能造成约 150 ms 主线程卡顿甚至假死的问题。
- 超大整数产量现在直接写入 Flux Networks 8.0.0 的二进制分层整数，不再展开或解析十万位以上的十进制文本。
- 优化 63 位 Flux digit 的写入算法。十万位整数的写入基准由约 3.5-18.5 ms 降至约 0.75-2.21 ms。
- 优化普通物品输出查询与传输，避免反复将完整 AbsoluteInteger 转换成 BigInteger。
- 优化戴森寰宇中枢打开时的大数计算与文本渲染。

### 问题修复

- 修复投入 LV10000 太阳帆和结构梁后，已完成戴森球的发电量被下一颗空星体覆盖为 0 的问题。
- 切换到下一颗星体时不再清空已经储存的戴森能量。
- 修复鸿蒙之气道痕界面始终显示第 0 层的问题。
- 修复鸿蒙之气与寰宇之心选择数据包始终回落到第一个选项的问题。
- 修复超大产量在剩余矿脉质量限制分支中触发昂贵十进制转换的问题。

### 界面优化

- 戴森寰宇中枢的星体数量、寰宇大道倍率和暗物质共鸣改用有界嵌套科学计数法。
- 示例：`1E(7.49E9)`、`1E(1E(7.49E9))`、`2^(1E(7.49E9))`。
- 鼠标悬浮时会显示计数法含义及 Flux 存储式，主界面不再渲染超长整数。
- 超大数的显示长度始终受限，不会因指数的指数继续增长而撑出界面。

## English

### Performance

- Fixed an approximately 150 ms main-thread stall or apparent freeze when the Cosmic Mining Hub processed extremely large outputs.
- Huge integer outputs are now written directly into Flux Networks 8.0.0's layered binary integer representation instead of expanding or parsing decimal strings with over 100,000 digits.
- Optimized 63-bit Flux digit writes. In benchmarks, writing a 100,000-digit integer dropped from roughly 3.5-18.5 ms to 0.75-2.21 ms.
- Optimized normal item output probing and transfer to avoid repeatedly converting complete AbsoluteInteger values into BigInteger.
- Reduced huge-number calculation and text-rendering overhead when opening the Dyson Universe Hub.

### Fixes

- Fixed completed Dyson spheres reporting zero generation after LV10000 sails and beams were inserted while the next star was empty.
- Stored Dyson energy is no longer cleared when advancing to the next star.
- Fixed the Primordial Qi imprint screen always displaying layer 0.
- Fixed Primordial Qi and Cosmic Heart selection packets always falling back to the first option.
- Fixed an expensive decimal conversion in the remaining-mining-mass output path.

### Interface

- Wrapped-star count, cosmic multiplier and dark-matter resonance now use bounded nested scientific notation.
- Examples: `1E(7.49E9)`, `1E(1E(7.49E9))`, and `2^(1E(7.49E9))`.
- Hover details explain the notation and show the bounded Flux storage form without rendering enormous integers on the main screen.
- Display length remains bounded even when an exponent's exponent becomes extremely large.
