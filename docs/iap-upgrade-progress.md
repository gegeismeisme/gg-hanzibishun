---
title: IAP 升级进度（Pro / 离线优先 + 无感恢复）
updated: 2026-01-04
---

本文件用于记录“Google Play 内购（Pro）接入”的实施进度与验收情况。

参考方案：`docs/iap-upgrade-plan.md`

## 里程碑清单

### A. 工程接入（本地可编译）
- [x] 引入 Billing 依赖（billing-ktx）
- [x] 定义商品常量（`hanzi_pro_lifetime`）
- [x] DataStore 扩展：Pro 权益缓存 + 同步时间 + 错误码
- [x] BillingRepository：启动静默同步、查询商品信息、购买回调、acknowledge、离线兜底
- [x] App 启动时连接 Billing（无感恢复）
- [x] `Me/Settings` 增加 Pro 卡片：状态 / 升级 / 恢复购买

### D. Pro 权益内容（v1，可感知）
- [x] 设置页：Accent 主题强调色选择（Pro 解锁更多颜色）
- [x] 设置页：笔迹粗细选择（Pro 解锁 Bold）
- [x] 字典页：Saved/History 编辑模式批量一键练习（Pro）

### B. Play Console 配置（可真实测试）
- [ ] 按操作清单完成配置与验证：`docs/play-console-iap-test-steps.md`
- [ ] Play Console 创建 In-app product：`hanzi_pro_lifetime`
- [ ] 添加许可测试账号（License testers）
- [ ] 上传 AAB 到 Internal testing（从商店安装测试）
- [ ] 覆盖测试用例：
  - [ ] 未购买 → 保持免费
  - [ ] 购买成功 → 立即解锁
  - [ ] 重装/换机 → 自动恢复
  - [ ] 离线启动（已解锁）→ 权益保持
  - [ ] 退款/撤销 → 在线同步后回收权益
  - [ ] Pending purchase → 不发货/不 ack

### C. 合规与文案（上线前）
- [x] 隐私政策补充“购买/权益缓存”说明：`docs/privacy-policy.md`
- [x] App 内隐私说明移除“无内购”的表述：`app/src/main/java/com/yourstudio/hskstroke/bishun/ui/character/LocalizedStrings.kt`
- [ ] Play Console Data safety 表单更新（如启用 Pro 并对外发布）
- [ ] 商店文案/截图同步（展示 Pro 权益时必须一致）

## 进度日志

### 2026-01-04
- 完成 A 阶段（工程接入）：依赖 + DataStore + BillingRepository + 启动连接 + 设置页入口
- 补齐基础合规文案（隐私政策与 App 内隐私说明）
- 增加 v1 Pro 可感知内容：Accent 主题强调色 / 笔迹粗细 / 字典批量练习
