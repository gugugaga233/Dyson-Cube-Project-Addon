# Dyson Cube Project Addon 1.0.14

## 中文

- 新增电磁轨道弹射器和射线接收站的计算进度显示：显示当前批次已完成量、批次容量，并区分处理中、等待材料、已完成和寰宇之心暂停状态。
- 计算进度使用 Flux `AbsoluteInteger` 精确同步和存档，不将超大星体数量展开成 `BigInteger`，避免 UI 查询再次造成卡顿。
- 修复寰宇之心奖励界面在多人游戏中同时锁定所有玩家的问题；现在只向一个选择者发送全局奖励计划界面，其他玩家不会被重复窗口阻塞。
- 选择期间服务端临时启用无敌保护，提交有效奖励后恢复玩家原有状态，避免打开界面时因服务器仍在运行而死亡。
- 单人游戏中寰宇之心奖励界面暂停世界；ESC 仍要求完成有效选择，避免奖励完成但没有领取计划。
- 修复寰宇之心选择界面状态处理和计算进度显示的语言文本。

## English

- Added a calculation-progress line to EM Rail Ejectors and Ray Receivers. It reports completed work, batch capacity, and whether settlement is processing, waiting for materials, complete, or paused by the Cosmic Heart.
- Progress is synchronized and persisted as Flux `AbsoluteInteger` values. The client never expands huge star counts into `BigInteger` just to render the overlay.
- Fixed the Cosmic Heart reward UI locking every online player at once. The global reward plan is now sent to one selector, so other players are not blocked by duplicate screens.
- The selector receives temporary server-side invulnerability and returns to the player's previous state after a valid reward plan is submitted.
- In singleplayer, the reward screen pauses the world. ESC still requires a valid selection so a completed heart cannot be left in an unclaimed state.
- Added localized text for the new reward-selection and calculation-progress states.
