# 汉字字形资产（`assets/characters/`）包体策略

## 现状（当前实现）

- 资产目录：`app/src/main/assets/characters/`
- 文件数量：约 9.6k（当前统计：9576）
- 未压缩体积：约 30.7MB（当前统计：32,246,320 bytes）
- 加载方式：`AssetManager.open("characters/uXXXX.json")` → `kotlinx.serialization` 解析成 `CharacterDefinition`
- 运行期优化：已有 `CharacterDiskCache`（落盘缓存），但仍然受“资产条目数量过多”的包体/安装成本影响

## 问题与风险

1) **包体与安装**
   - `aapt2` 需要处理大量 asset entry，构建与安装/解压都会变慢。
   - Play 分发时 AAB/APK 的资源打包也会受文件数量影响。

2) **运行时 IO 开销**
   - 单字加载是按路径直开文件，复杂度不高，但频繁 `open()` 9k+ 条目会带来额外 zip entry/文件句柄开销（尤其冷启动/低端机/频繁查字）。

3) **维护成本**
   - 大量小文件对 Git 操作、审阅、增量更新都不友好。

## 目标（不引入新依赖的前提）

- 离线优先（不依赖网络）
- 单字随机访问稳定（最好 O(1)）
- 可渐进迁移：新方案上线可回退到旧 assets
- 包体与安装成本可量化下降

## 方案对比（推荐从可回退的方案开始）

### A. 保持小文件 + 强化缓存（短期/最低风险）

- **做法**：保留 `characters/uXXXX.json`，增强 `CharacterDiskCache` 命中率与预热策略。
- **优点**：改动小、风险低、可快速上线。
- **缺点**：无法解决“包体 asset entry 数量过多”的结构性问题。

### B. 单文件 Zip 包（中期/实现成本低）

- **做法**：构建期生成 `characters.pack.zip`（或 `characters.zip`）+ 可选索引；运行期将 zip 复制到 app 私有目录，用 `ZipFile` 读取并解压单条 json。
- **优点**：显著减少 asset entry 数量；实现简单；可与旧方案并存（fallback）。
- **缺点**：仍需解压/解析 json；首次复制 zip 有一次性成本；索引与随机访问需设计。

### C. 预置 SQLite（中期/随机访问强，综合推荐）

- **做法**：构建期生成 `characters.db`，表结构例如：
  - `codepoint TEXT PRIMARY KEY`
  - `payload BLOB`（json 或压缩后的 json / 二进制）
- **优点**：随机访问强；可复用项目中已存在的 SQLite 经验（字典 `word.db`）；asset entry 数量大幅下降。
- **缺点**：需要生成脚本与迁移方案；数据库文件体积可能更大（但可压缩/分包）。

### D. 单一 JSON 大文件（不推荐）

- **做法**：把所有 json 合并到 `all.json`，运行期按 key 查。
- **缺点**：读取/解析成本大，容易造成内存峰值与卡顿；在 Android 上不友好。

## 推荐落地路线（可回退）

1) **Phase 1（本地实验）**
   - 保留现有加载逻辑不动。
   - 新增打包脚本（放 `tools/`），可生成 `characters.db` 或 `characters.zip`（二选一先做）。
   - 输出包体/加载耗时对比数据（同机型/同字集合）。

2) **Phase 2（双轨运行）**
   - 新增 `CharacterPackedDataSource`（优先读 `characters.db/zip`，失败再 fallback 读旧 `assets/characters/`）。
   - 增加关键路径测试：读一个常用字（如 `永`）能稳定解析。

3) **Phase 3（收敛资产）**
   - 当新方案稳定后，移除 `assets/characters/` 的小文件，仅保留单文件包与索引/数据库。

## 验收指标（建议在同一台设备上对比）

- APK/AAB 包体下降（目标：asset entry 数量显著下降，体积下降视压缩策略而定）
- 首次安装/冷启动时间下降
- “首次加载一个字（默认 `永`）”耗时下降或至少不回退
- 内存峰值不明显上升（避免大 json 全量解析）

## 当前落地状态（Zip Pack 双轨）

- 已实现 Zip Pack 双轨加载：新增 `CharacterPackedZipDataSource`，并在 `DefaultCharacterDataRepository` 中优先读取 `characters/characters.pack.zip`（复制到 `filesDir` 后用 `ZipFile` 读取），失败回退到原 `assets/characters/*.json`。
- 已补打包脚本：`tools/characters/build_characters_pack_zip.py`（默认输出到 `app/src/main/assets/characters/characters.pack.zip`）。
- 已补关键路径测试：`CharacterPackedZipDataSourceTest` 生成本地 zip 并验证常用字（`永`）可被稳定解析。
