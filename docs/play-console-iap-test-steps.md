---
title: Play Console 内购（Pro 买断）配置与测试清单
updated: 2026-01-04
---

本清单用于把 `hanzi_pro_lifetime`（一次性买断 / INAPP / non-consumable）在 Google Play 上配置到“可真实验证”的状态，并覆盖关键测试用例。

相关文档：
- 总方案与原理：`docs/iap-upgrade-plan.md`
- 当前进度：`docs/iap-upgrade-progress.md`
- Pro 权益定义：`docs/pro-lifetime-offer.md`

---

# 0. 前置确认（开始前 5 分钟）

1) **包名（package name）一致**
- 本项目包名：`com.yourstudio.hskstroke.bishun`
- Play Console 里的 App 必须是同一个包名（不能改）。

2) **商品 ID 一致**
- 代码里 Product ID：`hanzi_pro_lifetime`（见 `app/src/main/java/com/yourstudio/hskstroke/bishun/data/billing/BillingProducts.kt:1`）
- Play Console 里创建的 Product ID 必须逐字一致。

3) **测试账号准备**
- 准备 1~2 个用于测试的 Google 账号（Gmail）。
- 建议：不要用开发者主账号直接测购买；用 License tester 账号更安全、流程更接近真实用户。

---

# 1. Play Console：添加 License testers（许可测试）

目的：让测试购买不会真实扣款，并且可用于“侧载调试包”调试购买流程（开发阶段）。

步骤（Play Console）：
1) 进入 Play Console → **Settings（设置）** → **Developer account（开发者账号）** → **License testing（许可测试）**
2) 在 License testers 中添加你的测试 Gmail（可多个，用逗号/换行分隔）
3) 保存

注意：
- License testers 也可以同时加入 Internal testing/Closed testing 的测试名单。
- 设备上登录的 Google 账号必须包含这个 License tester 账号。

---

# 2. Play Console：创建 In-app product（一次性买断）

目的：让 Billing 能查询到商品详情（价格/标题）并发起购买。

步骤（Play Console）：
1) 进入 App → **Monetize（变现）** → **Products（产品）** → **In-app products（应用内商品）**
2) **Create product**
3) Product ID：`hanzi_pro_lifetime`
4) Product type：**Managed product（一次性买断 / Non-consumable）**
5) 填写：
   - Name（名称）：建议“Pro（一次性买断）”
   - Description（描述）：简要说明（可参考 `docs/pro-lifetime-offer.md`）
6) 设置价格（可先用最低价档，测试完再改）
7) 保存并 **Activate（激活）**（或确保状态为 Active）

常见坑：
- 商品没激活 → App 内会查不到 price/productDetails，按钮可能不可点。
- Product ID 写错 1 个字符 → 永远查不到。

---

# 3. 生成并上传 AAB（推荐：Internal testing）

目的：用“从 Google Play 安装的版本”进行最终验证（最接近真实发布）。

## 3.1 本地生成 AAB
在项目根目录执行：
- `.\gradlew.bat :app:bundleRelease`

产物路径（默认）：
- `app/build/outputs/bundle/release/app-release.aab`

如果上传提示 `versionCode` 重复：
- 先把 `app/build.gradle.kts` 的 `versionCode` +1，再重新打包生成 AAB。

## 3.2 上传到 Internal testing
步骤（Play Console）：
1) 进入 App → **Release（发布）** → **Testing（测试）** → **Internal testing（内部测试）**
2) Create new release / Edit release
3) 上传 `app-release.aab`
4) 填写 Release notes（随便写一句用于测试）
5) 保存 → Review → **Publish**（发布到内部测试轨道）

## 3.3 添加 Internal testing 测试者并安装
步骤（Play Console）：
1) Internal testing → **Testers**：添加测试者邮箱（或用 Google Groups）
2) 获取 **Opt-in URL（加入测试链接）**

步骤（测试手机）：
1) 用同一个测试账号登录 Google Play
2) 打开 Opt-in URL → 加入测试 → 从 Play 安装

提示：
- 发布到测试轨道后，有时需要等待一段时间（几十分钟到数小时）才对测试账号可见。

---

# 4. 开发阶段可选：不上传新版本也能测（侧载 + License tester）

当你只是想快速迭代 UI/逻辑，不想每次都上传 AAB 时：

前提：
- Play Console 已存在该 App（同包名）
- 已配置 License testers
- 已创建并激活 `hanzi_pro_lifetime`
- 设备安装了最新 Google Play

做法：
- 用 Android Studio / `adb install` 安装本地 debug/release 包到手机（侧载）
- 用 License tester 账号在设备上发起购买

注意：
- 这适合“开发迭代”，但**最终上线前仍建议用 Internal testing 从 Play 安装做一次全量验证**。

---

# 5. 测试用例（逐条勾选）

> 建议在测试前：清掉旧状态，保证用例可靠。
> - 卸载 App（或系统设置里清除数据）
> - 确保网络可用（除“离线用例”外）

## 5.1 未购买 → 保持免费（功能受限）
步骤：
1) 从 Play 安装（或侧载）后首次打开
2) 打开 `Me/Account` 页面
3) 观察 Pro 卡片状态与购买按钮是否展示价格
4) 验证 Pro 功能为“不可用/锁定”：
   - Accent：Pro 颜色不可选
   - Brush thickness：Bold 不可选
   - Dict Saved/History 编辑模式：批量练习按钮不可用（或标记 Pro）

预期：
- Pro 状态为 Free（或可用但未解锁）
- Pro 权益不可用，但核心学习功能可正常使用

## 5.2 购买成功 → 立即解锁
步骤：
1) 在 Pro 卡片点击 Buy
2) 使用测试支付方式完成购买（License tester 通常会出现“测试卡”选项）
3) 购买完成后回到 App
4) 立即验证 Pro 状态变为 Pro，并验证三项权益全部解锁（同 5.1）

预期：
- 购买成功后无需重启 App 即解锁

## 5.3 重装/换机 → 自动恢复（无感恢复）
步骤（重装）：
1) 保持同一 Google 账号
2) 卸载 App
3) 从 Play 重新安装并打开

预期：
- App 启动后自动恢复 Pro（无需再次购买）
- 如未自动恢复，点 `Restore purchases` 后应恢复

## 5.4 离线启动（已解锁）→ 权益保持
步骤：
1) 确保已购买并显示 Pro
2) 打开飞行模式（断网）
3) 强制停止 App（系统设置 → 应用 → 强制停止）
4) 再次打开 App

预期：
- 仍保持 Pro（离线可用）
- 说明：如果你“卸载后离线重装”，由于本地缓存被清空，离线状态下无法恢复，这是正常的；联网后会恢复。

## 5.5 退款/撤销 → 在线同步后回收权益
步骤（Play Console）：
1) Play Console → **Orders（订单）**
2) 找到对应测试订单（test purchase）
3) 执行 **Refund / Revoke**（退款并撤销）

步骤（手机）：
1) 确保联网
2) 打开 App → `Me/Account` → 点击 `Restore purchases`（或重启 App 等待自动同步）

预期：
- 在线同步后 `isPro` 变回 Free
- Pro 颜色/粗细/批量练习重新锁定

## 5.6 Pending purchase（挂起购买）→ 不发货/不 acknowledge
说明：挂起购买在部分支付方式/地区更常见。你可以用测试支付方式里的“Slow test card”来模拟（若你的测试环境出现该选项）。

步骤：
1) Buy → 选择 Slow test card（或等价的延迟/挂起支付方式）
2) 观察购买完成前的状态

预期：
- App 显示 Pending purchase（不解锁 Pro）
- 在变为 PURCHASED 前，不应解锁、不应 acknowledge
- 如果最终支付成功（变为 PURCHASED），应在回调/刷新后解锁并 acknowledge

---

# 6. 常见问题排查（出现问题先看这里）

1) **Buy 按钮不可点 / 不显示价格**
- 先确认 In-app product 已 Active
- 确认手机登录的是测试账号（建议 License tester）
- 等待一段时间（Play 后台同步可能延迟）
- 重新打开 App 或点 `Restore purchases`

2) **点击购买没反应 / Pro 卡片显示 Not available**
- 大概率 Billing 没连上：请确认手机有 Google Play、网络可用、账号正确
- 确认包名和 Play Console 的 App 一致

3) **重复购买同一个一次性商品失败**
- Non-consumable 不能重复购买；要再次测试请在 Orders 里 Refund/Revoke 后再买

4) **购买后过几分钟又变回免费**
- 这通常是“未 acknowledge 导致 3 分钟后自动退款”的典型症状
- 但本项目代码已自动 acknowledge；若出现，请把 Orders 状态截图发我排查

---

# 7. 完成标准（Definition of Done）

满足以下条件即可认为“内购上线闭环完成”：
- `hanzi_pro_lifetime` 商品已创建并激活
- Internal testing 从 Play 安装的版本，5.1~5.5 用例全部通过（5.6 尽量覆盖）
- 隐私政策与 Data safety、商店文案与 Pro 权益保持一致（参考 `docs/play-store-checklist.md`）

