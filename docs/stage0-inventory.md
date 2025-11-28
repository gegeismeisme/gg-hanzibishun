# Stage 0 – hanzi-writer 模块梳理

> 目的：明确必须迁移到 Android/Kotlin 的核心模型、渲染状态与交互逻辑，为 Stage 1+ 的实现提供输入。文件/行号来自 `exam/hanzi-writer`.

## 数据与类型
- `src/typings/types.ts:7-86` 定义了 `CharacterJson`, `Point`, `ColorOptions`, `QuizOptions` 等所有公共配置；Kotlin 需要等价的数据类以及 `ParsedHanziWriterOptions`.
- `src/Positioner.ts:1-51` 提供 makemeahanzi 坐标系 → 目标画布的缩放/偏移计算；Compose 端需要构建同等 `Positioner`，否则触控与渲染会错位。
- `src/models/Character.ts:1-9`, `src/models/Stroke.ts:1-41`, `src/models/UserStroke.ts`（未展示）是最小数据对象：字符含多笔画，每笔画包含 path 字符串、控制点、是否属偏旁；用户笔画用于记录交互。

## 渲染状态与动画
- `src/RenderState.ts:1-120` 描述了 `RenderStateObject` 结构（主字形、轮廓、强调、用户笔画集合）以及 Mutation 链路，负责响应动作并生成动画。
- `src/Mutation.ts:1-160` 定义 Mutation 的运行、暂停、取消机制，支持 scope-based 冲突检测与 requestAnimationFrame 驱动；迁移时需要以 `CoroutineScope + Animatable`(或自研 ticker) 实现同等功能。
- `src/renderers/HanziWriterRendererBase.ts`（未展开）提供渲染器接口 (`createCharacter`, `updateCharacter`, `drawGrid` 等)，在 Kotlin 端应抽象成可插拔渲染层（Compose Canvas 实现优先）。

## 交互流程
- `src/HanziWriter.ts:1-80` 是入口类，组合 `RenderState`, `LoadingManager`, `characterActions`, `Quiz`，暴露动画控制/quiz API；Android 端可拆解为 ViewModel + UseCase。
- `src/characterActions.ts:1-40` 定义“展示/隐藏笔画、字符”等状态变换，底层返回 `Mutation` 列表；Compose 端可实现成 reducer/命令列表。
- `src/quizActions.ts:1-60` 管理用户笔画集合（开始/更新/淡出）与高亮状态，是 Quiz 状态机输出；必须迁移以支持实时反馈。
- `src/Quiz.ts:1-120` 维护当前笔画索引、错误次数，并驱动 `strokeMatches` 判断；也是继续/结束练习的中心。
- `src/strokeMatches.ts:1-80` 引入余弦相似度、Frechet 距离、起止点距离等指标判断正确与否；`options.leniency` 等参数来自 `QuizOptions`.

## 几何工具链
- `src/geometry.ts`（未展开）包含距离、向量、贝塞尔插值、路径字符串创建、Frechet 计算，`Stroke.getVectors()`、`strokeMatches` 都依赖它。迁移该文件并配套单元测试是 Stage 2 的重点。
- `src/parseCharData.ts` 将 JSON strokes/medians 转换为 `Character` + `Stroke` 实例；Android 需提供 Asset/Network 输入 → Kotlin 对象解析逻辑。

## 加载 & 资源
- `src/defaultCharDataLoader.ts:1-24` 通过 `XMLHttpRequest` 拉取 CDN JSON；Android 需要对应的 OkHttp/Retrofit + 缓存层。
- `src/LoadingManager.ts`（未展开）封装并发加载、缓存与错误回调，后续可转成仓库/UseCase。

## 结论
- MVP 阶段必须优先迁移：`types`, `Positioner`, `models/*`, `parseCharData`, `geometry`, `strokeMatches`, `characterActions`, `quizActions`, `RenderState`, `Mutation`, `Quiz`.
- 高层 API (`HanziWriter.ts`, `LoadingManager.ts`) 可拆解为 Kotlin service/view-model，逐步替换 Compose UI 占位逻辑。
