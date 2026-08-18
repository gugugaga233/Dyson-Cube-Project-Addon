# Dyson Cube Project Addon 1.0.8

## 中文

### 紧急存档加载修复

- 修复超大暗物质共鸣在进入存档后的首个服务器 tick 中重算发电量，导致单次卡顿约 4 分钟的问题。
- `AbsoluteInteger` 的小常数乘法和除法改为直接按 Flux `2^63` 数字逐位计算，不再把数百万位数完整展开为 Java `BigInteger`。
- 超过精确计算阈值的 `2^共鸣次数` 只保留科学计数指数的最高有效数字；存档内的共鸣次数和星体总数仍保持完整精度，不会截断或清零。
- 最大 4 层、每层 65536 位的 Flux 指数现在也能在固定时间内完成发电量重算。
- 降低 Torcherino 等方块加速模组重复触发弹射器 tick 时造成的主线程阻塞风险。

### 开发与验证

- 增加跨 `2^63` 数字边界、`Long.MAX_VALUE` 除数和最大 Flux 层级的算术回归测试。

## English

### Emergency World-Load Fix

- Fixed huge dark-matter resonance values recalculating power on the first server tick and stalling world entry for roughly four minutes per calculation.
- Small-constant multiplication and division for `AbsoluteInteger` now operate directly on Flux's base-`2^63` digits instead of expanding millions of bits into a Java `BigInteger`.
- For `2^resonance` above the exact-computation threshold, only the significant leading digits of the scientific exponent are retained. Saved resonance and wrapped-star counters remain fully precise and are never truncated or reset.
- Power recalculation now completes in bounded time even at Flux's maximum four-layer, 65,536-digit-per-layer exponent capacity.
- Reduced main-thread stall risk when tick accelerators such as Torcherino invoke the rail ejector repeatedly.

### Development and Verification

- Added arithmetic regression coverage across base-`2^63` boundaries, `Long.MAX_VALUE` divisors, and the maximum Flux exponent layer.
