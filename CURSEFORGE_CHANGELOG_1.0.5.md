# Dyson Cube Project Addon 1.0.5

## 中文

### 紧急性能修复

- 修复电磁轨道弹射器处理超大星体总数时，服务器主线程卡死在 `AbsoluteInteger.add` 的问题。
- Lv.8 动态批量现在通过最高二进制位直接计算十进制数量级，不再从 1 开始反复乘 10。
- 向超大累计计数器写入本次批量时，只处理增量的有效位和进位，不再扫描累计总数的全部位。
- 任意位数减法改为直接借位运算，移除批量结算中的完整 `BigInteger` 往返转换。
- 超大批量转换为 Flux `BigNumber` 时只保留其可表达的有效精度，不再展开数万到数百万位整数。
- 材料可负担数量会先受本次结算上限约束，超大库存不再为了计算一个有限批量而整体展开。

### 批量安全上限

- Lv.8 的动态结算上限仍按累计星体数量逐级乘 10。
- 为防止单次服务器 tick 构造不可控的大整数，单次结算最高限制为 `1E65536` 颗星体。
- 达到安全上限后，戴森寰宇中枢会显示已封顶状态，不再显示不存在的下一门槛。

## English

### Emergency Performance Fixes

- Fixed a server-thread freeze in `AbsoluteInteger.add` when an EM Rail Ejector processed an extremely large wrapped-star total.
- Lv.8 dynamic batch sizing now derives the decimal order directly from the highest binary digit instead of repeatedly multiplying from 1 by 10.
- Crediting a batch to a huge cumulative counter now touches only the incoming batch digits and carry, rather than scanning the entire total.
- Arbitrary-size subtraction now uses direct digit borrowing and no longer round-trips through a full `BigInteger` during batch settlement.
- Huge batches are converted to Flux `BigNumber` at its representable precision without materializing integers containing tens of thousands or millions of digits.
- Material affordability is bounded by the current settlement cap before inventory division, so a huge reserve is not fully expanded to calculate a finite batch.

### Batch Safety Cap

- The Lv.8 dynamic settlement cap still increases tenfold with the cumulative wrapped-star count.
- A single server tick is limited to at most `1E65536` wrapped stars to prevent uncontrolled large-integer work.
- Once this safety cap is reached, the Dyson Universe Hub displays a capped state instead of a nonexistent next threshold.
