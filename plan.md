# 汉字笔顺原生 App MVP 规划

## 背景概述
- `exam/hanzi-writer` 提供完整的 Web 端笔顺动画与练习逻辑，核心由 `HanziWriter.ts` 协调，依赖 `characterActions.ts`、`geometry.ts`、`quizActions.ts` 等模块。
- 现有 Android 工程 (`app/`) 仅包含 Compose 模板，需要逐步迁移上述核心能力并保持全原生体验，不使用 WebView 包装。
- MVP 目标是先复刻参考项目的主要体验（单字动画 + 练习），为后续整合字典或更多资源打基础。

## MVP 范围
1. **数据访问**：支持离线加载指定汉字（可从 `hanzi-writer-data` 选取常用字集），允许按需联网补充。
2. **笔画动画**：在 Compose Canvas 中渲染字形轮廓、分步笔画，以及播放/暂停/步进控制。
3. **书写练习**：捕获用户手写轨迹，依据顺序及轨迹判定结果，给出实时反馈（参考 `Quiz.ts` 行为）。
4. **基础 UI**：提供单字查询入口、结果展示页面及练习视图，包含主题/暗色支持。
5. **质量保障**：核心几何、匹配与数据解析逻辑具备单元测试；关键交互含 Compose UI 测试。

## 技术原则
- **逻辑复用**：逐步将 TS 模块等价迁移为 Kotlin（数据类、sealed class、内联函数），保持接口对齐以降低偏差。
- **渲染抽象**：参考 `renderers/HanziWriterRendererBase.ts` 设计 Kotlin 渲染器接口，使 Canvas/潜在 OpenGL 实现可以互换。
- **层次分离**：数据层（仓库/缓存）→ 笔画/测评引擎 → 渲染状态 → Compose UI，便于将来扩展字典或多数据源。
- **可离线**：默认优先使用本地 JSON 资源，联网补充放在后台并缓存。

## 阶段任务
### 阶段 0：现有代码梳理
- 阅读 `exam/hanzi-writer` 关键模块，记录模型、渲染状态、Mutation 流程。
- 清点需要迁移的数据结构（如 `Character`, `Stroke`, `RenderState`）。

### 阶段 1：数据与加载
- 挑选初期需要支持的字符集合，并从 `hanzi-writer-data` 导出 JSON，放入 `app/src/main/assets/characters`.
- 构建 `CharacterDataRepository`：
  - 资产解析器：镜像 `parseCharData.ts` 行为。
  - 远程加载器：使用 OkHttp 下载 JSON，存入本地缓存。
  - API：`suspend fun loadChar(char: String): CharacterData`.
- 添加 Room/SQLDelight（视需要）记录最近使用字符与缓存元数据。

### 阶段 2：核心几何与笔画模型
- 将 `models/*.ts`、`geometry.ts`、`strokeMatches.ts` 等迁移为 Kotlin：
  - 建立 `Stroke`, `Point`, `Bezier`, `Character`, `UserStroke` 数据类。
  - 移植曲线插值、碰撞检测、距离计算、匹配阈值算法。
- 单元测试覆盖：使用原项目的测试用例数据，确保行为一致。

### 阶段 3：渲染状态与动画
- 设计 `RenderState`、`Mutation`、`Animator` Kotlin 版本，支持：
  - 笔画显示进度 (`displayPortion`)、透明度、描边/填充颜色控制。
  - 阶段动作（showStroke、highlightStroke、animateCharacter）的时间轴。
- 在 Compose 中封装 `StrokeCanvas` 组件，利用 `Path` & `drawIntoCanvas` 根据状态绘制笔画与辅助网格。
- 提供 `HanziPlayerViewModel` 管理动画命令与状态流（StateFlow）。

### 阶段 4：交互与练习
- 仿照 `Quiz.ts`，实现 Kotlin 版状态机：
  - 捕获 PointerInput 轨迹，转化为 `UserStroke`.
  - 匹配正确与否，触发提示/重试/下一笔流程。
  - 提供练习结果（正确率、时间）供 UI 展示。
- 添加触控手势：单指书写、双指缩放/拖动画布（可放到后续迭代）。

### 阶段 5：Compose UI & 应用框架
- Navigation：`SearchScreen` → `CharacterDetailScreen`（展示静态笔顺 + 按钮）→ `PracticeScreen`.
- 基础控件：
  - 搜索栏 + 历史列表（from repository/Room）。
  - 播放控制（Play/Pause/Step）+ 动画进度条。
  - 练习反馈卡片（正确/错误提示、下一笔按钮）。
- 主题与多语言：沿用 Material 3，文案中英文切换位于资源文件。

### 阶段 6：测试、性能与打包
- 单元测试：几何算法、数据解析、quiz 判定。
- UI 测试：Compose 流程（查询→播放→练习）Smoke 测试。
- 性能验证：Profile 大字符集合下动画流畅度、内存占用。
- Gradle 任务与 CI：配置 `lint`, `test`, `connectedAndroidTest`；后续可接 GitHub Actions。

## 后续扩展占位
- 集成更多字典/释义、笔画拆解、部首搜索等新特性。
- 引入手写识别、云同步、用户自定义字集等高级功能。
- 多渲染引擎（如 Compose Multiplatform）或导出 SDK 供其它应用使用。

> 以上计划聚焦 MVP 交付，后续如需整合额外字典或功能，可在阶段 6 后追加相应里程碑。
