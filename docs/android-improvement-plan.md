# Android 端「汉字笔顺 / 字典」功能与代码完善计划（先不讨论收费）

> 背景：正式版刚上线（约 3 天），当前数据样本很小且波动大；先把核心体验做扎实与代码结构稳定下来，再基于更稳定的留存/口碑去决定广告、买断或订阅等变现策略。

## 0. 结论：先完善功能再定收费——可行且更稳

- 当前主使用场景偏「工具型」（查字 / 发音 / 笔顺 / 字典），上线早期更需要 **稳定性、速度、正确性、易用性** 来提升评分与自然增长；过早引入广告或付费墙容易伤害口碑，且会放大早期数据噪声。
- 代码层面已具备较完整的离线能力与基础学习流（练习/课程/进度/字典/账户），因此“先完善功能”是一个合理且可落地的路径。

## 当前状态（代码已在做的事）

- 已切换为底部 4 Tab 信息架构：`Home / Learn / Dict / Me`，其中 `Learn` 合并课程与进度（见 `app/src/main/java/com/yourstudio/hskstroke/bishun/ui/navigation/BishunApp.kt`、`app/src/main/java/com/yourstudio/hskstroke/bishun/ui/learn/LearnScreen.kt`）。
- 已把「词典词条」改为按需加载：进入练习页不再强制解析 `word.json`，仅在用户打开词典弹层时请求并显示 Loading/Retry（见 `app/src/main/java/com/yourstudio/hskstroke/bishun/ui/character/CharacterViewModel.kt`、`app/src/main/java/com/yourstudio/hskstroke/bishun/ui/practice/PracticeDialogs.kt`）。
- 已增强 TTS 初始化与可用性判断：只在引擎/语言可用时允许播放，并自动选择可用的中文 Locale（见 `app/src/main/java/com/yourstudio/hskstroke/bishun/ui/practice/PracticeTextToSpeech.kt`）。

## 1. 当前实现快照（基于仓库现状）

- 导航结构：Compose 底部导航，4 个主入口：Home / Learn / 字典 / 我的（课程与进度合并到 Learn）。见 `app/src/main/java/com/yourstudio/hskstroke/bishun/ui/navigation/BishunApp.kt`、`app/src/main/java/com/yourstudio/hskstroke/bishun/ui/learn/LearnScreen.kt`。
- 练习（笔顺/描红/判定）：共享的 `CharacterViewModel` 负责加载字形、渲染快照、练习状态、课程会话、历史等。见 `app/src/main/java/com/yourstudio/hskstroke/bishun/ui/character/CharacterViewModel.kt`、`app/src/main/java/com/yourstudio/hskstroke/bishun/ui/character/CharacterScreen.kt`、`app/src/main/java/com/yourstudio/hskstroke/bishun/ui/practice/PracticeScreen.kt`。
- 字典：`LibraryViewModel` + `WordRepository`，查找本地 `assets/word/word.json`。见 `app/src/main/java/com/yourstudio/hskstroke/bishun/ui/library/LibraryViewModel.kt`、`app/src/main/java/com/yourstudio/hskstroke/bishun/data/word/WordRepository.kt`。
- 发音：使用 Android `TextToSpeech`，根据引擎可用性与 Locale 支持情况进行初始化与降级。见 `app/src/main/java/com/yourstudio/hskstroke/bishun/ui/practice/PracticeTextToSpeech.kt`。
- 离线资产体量（需重点关注体验/包体/性能）：
  - `app/src/main/assets/word/word.json` ≈ 27MB，约 1.6 万条词条（含释义文本）。
  - `app/src/main/assets/characters/` ≈ 62MB（约 9577 个 JSON 文件），且存在 `all.json` ≈ 32MB（目前代码未引用）。

## 2. 关键风险点（优先级从高到低）

### P0：字典数据加载的性能与内存风险

- 现状：`WordRepository` 首次查询会把 `word.json` 整体读入内存并构建 Map（`JSONArray` + 全量 `WordEntry`）。虽然在 IO 线程执行，但仍然存在 **内存占用高、首次等待长、GC 压力大** 的风险；目前已改为「按需触发」（不再影响首轮进入练习页），但首次打开字典弹层仍可能等待明显。
- 目标：字典查询做到「**秒开/可渐进**」，不因词库体量导致卡顿/崩溃/无响应。

### P0：核心逻辑集中在单个 ViewModel，后续迭代成本高

- 现状：`CharacterViewModel` 同时管理：字加载、渲染与动画、练习判定、课程、进度、历史、偏好设置等。
- 风险：需求叠加后容易出现回归、测试困难、状态耦合导致 bug 难查。

### P1：发音（TTS）体验与可控性不足

- 现状：固定 `Locale.CHINA`，未依据语言设置/系统语言选择；未显式处理 TTS 引擎不可用/语言包缺失等情形；UI 侧缺少明显的“正在朗读/失败提示”统一体验。

### P1：离线资产体积与冗余

- 现状：`characters` 目录体积较大且包含未引用的 `all.json`；对 Play 下载体积、安装耗时、低端机空间敏感用户不友好。
- 目标：在不牺牲核心离线体验的前提下，逐步做 **包体与资产策略优化**（可分阶段）。

### P2：字典能力偏基础（检索维度少、结果呈现简单）

- 现状：当前更像“单词条展示”，缺少：多结果列表、模糊匹配、拼音检索、部首/笔画筛选、例词/近义关联、收藏/生词本等。

## 3. 分阶段完善计划（建议按 3 个迭代推进）

> 说明：下面每项都尽量给出“验收标准”，方便你们在每次发版后客观判断是否达标。

### Iteration A（1–2 周）：把「查字 + 发音 + 笔顺」体验打稳（P0）

1) 字典加载与首轮体验优化（P0）
- 将词库访问改为 **按需/渐进**：避免在默认字加载时必然触发全量词库解析（例如：仅在用户展开字典卡片/点击发音时触发，或先加载“单字索引/轻量字段”）。
- 提供可观测状态：加载中/失败/重试，避免用户误以为无数据。
- 验收：冷启动进入练习页后，UI 不因词库解析出现明显卡顿；首次打开字典卡片有明确反馈且在可接受时间内完成。

2) 发音（TTS）健壮性（P1→P0）
- 根据 `languageOverride` 或系统语言选择 TTS Locale；初始化失败/不支持语言时给出可理解提示，并降级（例如只显示拼音，不朗读）。
- 验收：无 TTS 引擎或语言包缺失时，不崩溃；用户能明确知道原因与如何处理。

3) 乱码与文本一致性清理（P1）
- 清理 UI 文案/注释中出现的异常字符（如 `тА?` 等），确保对外展示文本一致、可维护。
- 验收：Help/Privacy/按钮文案在目标语言下显示正常，无乱码。

### Iteration B（2–4 周）：字典“可用”→“好用”（P1）

1) 检索能力升级
- 支持多字符词条查询（放宽 `LibraryViewModel` 的长度限制），并支持“多结果列表 + 点选详情”。
- 支持拼音输入（a-z + 声调数字）与简易模糊匹配（可先做前缀/包含）。
- 验收：用户输入拼音或词语能得到结果列表；单字查询路径保持不变且更快。

2) 结果呈现与联动
- 字典页与练习页联动增强：从字典点“练习”能直接跳转并加载；从练习页可快速打开该字词条详情。
- 增加收藏/最近历史的可管理性（清空、固定、排序）。
- 验收：查词→练习→返回查词路径顺畅，状态保持合理。

3) 数据结构与存储策略（为后续扩展铺路）
- 评估把 `word.json` 转为更适合移动端查询的格式（如 SQLite/Room 预置库，或拆分为单字/多字两套索引）。
- 产出脚本与说明文档（数据如何生成、如何校验、如何更新）。

### Iteration C（4–6 周）：工程化与可维护性（P0/P1 长期）

1) ViewModel 拆分与边界清晰化
- 将 `CharacterViewModel` 的职责按“练习渲染/课程/进度/字典信息”拆分为更小的状态源（可以先拆 UseCase，再拆 ViewModel）。
- 验收：核心逻辑有可测试边界；单模块修改不引发大面积回归。

2) 自动化测试补齐
- 已有单元测试基础（`app/src/test/java/.../hanzi/*`），补齐字典解析/查询、课程目录、偏好设置等单元测试；增加关键 UI 的 Compose 测试（导航/查词/进入练习）。
- 验收：CI/本地一键跑测试能覆盖关键路径；发版前能快速发现回归。

3) 资产与包体策略（可选但推荐）
- 确认 `characters/all.json` 是否需要：若不使用，考虑移除以减小包体；若要使用，考虑用它替代大量小文件（权衡加载方式与启动成本）。
- 验收：包体下降或加载策略更可控；Play 下载与安装体验改善。

## 4. 里程碑后的“收费策略决策点”（先记录，不在本阶段做）

- 当完成 Iteration A/B 并至少积累 2–4 周更稳定数据后，再讨论收费策略更有依据。
- 需要的客观信号：评分与评论稳定、崩溃/ANR 低、核心漏斗（查字→发音/练习）顺畅、留存趋势明确。

## 5. 相关参考文档

- Play 提交清单：`docs/play-store-checklist.md`
- 帮助与隐私草案：`docs/help-guide.md`、`docs/privacy-policy.md`
- 早期 UI/功能规划（可参考但以现状为准）：`plan.md`、`docs/profile_menu_plan.md`
- UI/UX 全面美化计划（iOS 风格参考）：`docs/ui-ux-upgrade-plan.md`
