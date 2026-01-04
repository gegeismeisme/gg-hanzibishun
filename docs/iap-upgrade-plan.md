---
title: Hanzi 内购升级方案（离线优先 + 无感恢复）
updated: 2026-01-04
---

# 0. 目标与边界

## 目标
- 在不破坏“离线优先、学习不被打扰”的前提下，引入 Google Play 内购，实现 **购买后即时解锁 + 换机自动恢复 + 离线可用**。
- 技术上把支付逻辑封装在 Repository 层，UI 只观察本地状态，避免闪烁与复杂分支。
- 合规上保持透明：隐私/数据安全表单/商店信息与实际权限/行为一致。

进度跟踪：`docs/iap-upgrade-progress.md`

## 不做（本阶段）
- 不引入广告（会显著增加合规与体验风险，且通常需要 `INTERNET` 与第三方 SDK）。
- 不自建后端做强校验（先做“轻量可落地”的离线方案；未来如需防破解再评估服务端）。

# 1. 产品策略建议（先不定价）

## 推荐：先做一次性买断（INAPP / Non‑consumable）
原因：实现与维护成本最低、学习工具的用户接受度更高、可控性强，适合早期版本。

建议商品（示例 Product ID）：
- `hanzi_pro_lifetime`：一次性买断 Pro 权益
- 可选 `hanzi_tip_small` / `hanzi_tip_medium` / `hanzi_tip_large`：支持开发（Consumable）

## 订阅（SUBS）：等“持续交付/持续成本”具备后再上
当且仅当你能稳定交付以下持续价值时再考虑：
- 云同步/多设备备份
- 持续更新的课程/词库包（按月持续上新）
- 个性化学习计划（若未来接入在线能力）

# 2. 付费权益怎么设计（不伤核心体验）

原则：核心“查字/发音/笔顺/基础练习”尽量保持可用；付费解锁“进阶价值”和“效率工具”。

## 2.1 本次版本（v1）建议的 Pro 买断内容（可感知、低打扰）
优先让用户一眼能理解“买断买到什么”，且不影响核心学习功能。

- **主题强调色（Accent）**：免费 2 个颜色；Pro 解锁更多强调色（仅改变 UI 主题主色，不改汉字楷体与笔画渲染）。
- **笔迹粗细（Brush thickness）**：免费 Thin/Regular；Pro 解锁 Bold（更适合大屏/重度练习）。
- **Saved/History 批量一键练习**：在字典的收藏/历史里进入编辑模式，选多条 → “Practice characters” 一键生成“去重汉字队列”并开始练习（属于效率工具，适合放入 Pro）。

验收位置（代码落地后）：
- 设置页 `Me/Account`：Appearance 中可选 Accent/Brush thickness；Pro 未解锁时 Pro 选项显示但不可点。
- 字典页 `Dict`：Saved/History 编辑模式出现 “Practice characters” 动作（Pro 才可用）。

可选的 Pro 解锁方向（按推荐优先级）：
1) **更强的学习闭环**：更细的复习队列/间隔重复、错题本、按目标生成练习集
2) **更强的数据面板**：更完整的统计（周/月/技能拆分）、复盘建议、成就体系
3) **更强的个性化**：更多主题色方案/板书设置预设、Widget 更多布局与快捷动作

避免把“最基础的查字/笔顺展示”做成强付费墙，容易引发早期口碑下滑。

# 3. 技术架构（离线优先 + 无感恢复）

## 3.1 模块划分（MVVM + Repository）
- `BillingClient`：Google Play Billing 官方库，负责连接/查询/发起购买/回调
- `EntitlementPreferences`（DataStore）：本地缓存（快路径、离线兜底）
- `BillingRepository`：中间层，协调 Google 状态与本地缓存，提供统一的“权益状态流”

> 关键点：UI 只订阅本地 `StateFlow` / `Flow`，网络/Play 同步在后台发生，避免 UI 逻辑污染。
>
> 本项目已存在 `UserPreferencesStore`（DataStore）与 `Me/Account` 设置页，可直接在现有偏好存储里扩展 Pro 字段，避免引入第二套 DataStore。

## 3.2 本地缓存的内容（建议比 is_pro 更丰富）
最小可行是 `isPro:Boolean`，但建议至少增加：
- `entitledProducts:Set<String>`（例如 `hanzi_pro_lifetime`）
- `lastSuccessfulSyncEpochMs:Long`（避免“误清空权益”难排查）
- `lastBillingErrorCode:Int?`（用于设置页提示：无法连接 Play / 需要从商店安装等）

## 3.3 同步策略（单一事实源 + 本地快路径）
1) App 启动：立即读取本地缓存 → UI 立刻稳定展示（不闪）
2) 背景静默同步：
   - Billing 连接成功后，`queryPurchasesAsync(INAPP)`（未来如加订阅，再查 `SUBS`）
   - **成功且返回列表**：计算 `hasPro`，写入本地缓存；对 `PURCHASED && !acknowledged` 的订单执行 `acknowledge`
   - **成功但空列表**：写入本地 `hasPro=false`（处理退款/撤销/换号）
   - **失败/断网/不可用**：保持本地不变（离线兜底），仅记录错误码用于提示

> 这就是“信任 Google 作为单一事实源，但依赖本地缓存保证速度与离线体验”的落地版本。

# 4. 关键流程与边界场景（验收要点）

## 4.1 首次安装（未购买）
- 同步返回空列表 → 本地为 `false` → UI 显示锁/升级入口（但不强打扰）

## 4.2 购买成功（解锁）
- `PurchasesUpdatedListener` 回调 → 校验 `purchaseState==PURCHASED` → `acknowledge` → 写入本地 `true`

## 4.3 换机/重装（无感恢复）
- 从 Play 商店安装并登录同一 Google 账号 → 启动后静默 `queryPurchases` → 自动恢复本地 `true`

## 4.4 离线打开（离线可用）
- Billing 连接失败/查询失败 → **不改本地** → 仍可使用已缓存的 Pro 权益

## 4.5 退款/撤销/换号
- 在线查询成功且返回空列表 → 本地写 `false`（确保权益回收）

## 4.6 Pending purchase
- `purchaseState==PENDING`：不发货、不 acknowledge；UI 提示“处理中/等待确认”

# 5. UI/产品节奏（减少负面反馈）
- 入口建议放在 `Me/Account` 页：`Upgrade to Pro` + `Restore purchases` + `Purchase status`
- 锁定功能采用“轻提示”而非强弹窗：点击锁功能时再引导购买
- 加一个“隐私/权益说明”短摘要：说明离线存储、换机自动恢复、无网络权限等核心点

# 6. 测试与发布（Play Billing 的现实约束）
- 必须上传到 Play Console（至少 Internal testing），并从 Play 安装才能稳定测试购买/恢复
- 在 Play Console 配置 License Testers（许可测试账号）
- 重点测试用例（至少覆盖）：
  - 新用户未购买
  - 购买成功 → 立即解锁
  - 重装/换机 → 自动恢复
  - 离线启动（已有缓存）→ 权益保持
  - 退款/撤销 → 在线同步后回收权益
  - Pending purchase → 不发货/不 acknowledge

# 7. 合规与文档清单（上线前必须同步）
当我们真正上线内购时，需要同步更新：
- `docs/privacy-policy.md`：移除“no in-app purchases”的表述；补充“购买由 Google Play 处理，本地仅缓存权益状态”等描述
- App 内隐私弹窗文案（`LocalizedStrings`）：同上
- Play Console Data safety 表单、商店文案与截图（展示 Pro 功能时要一致）

# 8. 对你提供方案的质量评审（采纳/不采纳）

## 总体评价
这是一份 **高质量的“入门到可上线”方案**：方向正确、实现成本低、非常符合“离线优先 + 无感恢复”的目标。

## 建议直接采纳的点（A 级）
- **Google 作为单一事实源 + 本地缓存兜底** 的总体哲学
- `queryPurchasesAsync` 成功才覆盖本地；失败不把本地置 `false`（避免离线误伤）
- **必须 acknowledge**（否则自动退款）这一关键提醒
- Repository 封装、UI 只观察本地 Flow 的思路（避免 UI 闪烁）

## 建议调整后采纳的点（B 级）
- 订单处理要加上 `purchaseState` 判断：仅对 `PURCHASED` 订单发货/ack；`PENDING` 不 ack
- 不要在每次回调里临时 new `CoroutineScope`；建议用注入的 `CoroutineScope` 或 repository 内部单例 scope（避免泄漏）
- 本地缓存建议不仅存 `is_pro`，至少加 `lastSuccessfulSync` 与 `entitledProducts`，便于排障与扩展订阅
- 断线重连与“Billing 不可用/Play 未安装/签名不匹配（未从 Play 安装）”的提示需要补齐（否则线上会出现“点购买没反应”类反馈）

## 不建议原样采纳的点（C 级）
- “Google 返回空列表就一定是没买/退款”这句话本身没错，但实现上建议以 **“查询成功且 Billing 可用”** 为前提；并记录 `lastSuccessfulSync`，避免排障困难
- 示例里提到“显示广告/锁 HSK 3-6”等属于产品决策，不建议直接照搬到本项目；应按我们实际的 Pro 权益设计落地
