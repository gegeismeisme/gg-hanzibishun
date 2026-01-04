---
title: Pro 一次性买断内容（hanzi_pro_lifetime）
updated: 2026-01-04
---

本文件用于明确：用户购买一次性买断（Google Play In-app / non-consumable）后，Pro 权益具体包含哪些内容，便于在 App 内/商店文案/客服解释中保持一致。

## Pro（一次性买断）包含什么

### 1) 个性化外观（不改汉字楷体/笔画渲染）
- **主题强调色（Accent）**：免费 2 个；Pro 解锁更多强调色选项（仅影响 UI 主题主色）。
- **笔迹粗细（Brush thickness）**：免费 Thin/Regular；Pro 解锁 Bold（更适合重度练习/大屏）。

### 2) 学习效率工具
- **字典收藏/历史的批量一键练习**：在 `Dict` 的 `Saved/History` 编辑模式中选多条，点击 “Practice characters” 生成去重汉字队列并开始练习（Pro）。

### 3) 购买体验与恢复
- **离线可用**：权益状态本地缓存，离线启动不会误降级。
- **无感恢复**：同一 Google 账号换机/重装后，App 启动静默查询并自动恢复（无需手动输入/登录）。

## 不包含（当前阶段的边界）
- 不引入广告 SDK，不以广告换权益。
- 不做云端账号体系/自建后端验签（仍以 Google Play 为单一事实源）。

## 验收清单
- 设置页 `Me/Account`：Appearance 里能看到 Accent/Brush thickness；未购买时 Pro 选项展示但不可点。
- 字典页 `Dict`：Saved/History 编辑模式出现批量练习入口；未购买时按钮禁用并标记 Pro。

