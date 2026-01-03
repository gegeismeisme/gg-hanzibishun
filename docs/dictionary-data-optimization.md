# 字典数据结构优化方案（word.json → 可查询索引）

> 背景：早期字典数据为 `word.json`（约 27MB），运行时全量解析会带来等待与内存压力。现在已迁移为预置 SQLite：App 读取 `app/src/main/assets/word/word.db`，而 JSON 作为生成源保留在 `tools/dictionary/word.json`。

## 目标

- **性能**：首次查询可控（< 300ms～800ms，取决于机型），不出现明显卡顿/ANR。
- **内存**：避免常驻全量词条对象；查询按需读取。
- **检索能力**：支持 word 精确/前缀/包含、拼音（含声调数字）检索，多结果排序与分页。
- **可维护**：数据生成可重复、可校验、可回滚；应用内版本可追踪。

## 方案 A（推荐）：预置 SQLite/Room（可加 FTS）

### 数据库结构（当前实现）

- 表 `words`
  - `oldword TEXT`
  - `strokes TEXT`
  - `pinyin TEXT`（保留原始带音调拼音）
  - `radicals TEXT`
  - `explanation TEXT`
  - `more TEXT`
  - `pinyin_plain_compact TEXT`（去音调、ü→v、去空格，如 `yong`）
  - `pinyin_tone_compact TEXT`（声调数字、去空格，如 `yong3`）
- 索引
  - `CREATE INDEX idx_pinyin_plain_compact ON words(pinyin_plain_compact);`
  - `CREATE INDEX idx_pinyin_tone_compact ON words(pinyin_tone_compact);`
- 可选：FTS5
  - `words_fts(word, pinyin_plain_compact, pinyin_tone_compact, explanation)`，用于包含检索与排序（注意中文分词策略与体积权衡）。

### App 端接入方式

- 方式 1：Room + `createFromAsset("word/word.db")`
  - 优点：开发体验好，查询类型安全，可维护性强。
  - 注意：需要定义 `@Database`、`@Entity`、`@Dao`；升级时走 Room migration 或重新 `createFromAsset`（通常需要版本号判断）。
- 方式 2：直接使用 `android.database.sqlite`/SQLDelight
  - 优点：更轻量、可控。
  - 缺点：类型与迁移约束需要自建。

### 数据生成（必须做成脚本）

- 输入：`tools/dictionary/word.json`
- 输出：`app/src/main/assets/word/word.db`
- 生成命令：
  - `python tools/dictionary/build_word_db.py`
  - 或 `./gradlew buildWordDb`
- 产物校验：
  - `python tools/dictionary/verify_word_db.py`（校验行数、抽样字段一致、拼音索引一致）
  - 或 `./gradlew verifyWordDb`
  - 一键更新：`./gradlew updateWordDb`
- 建议技术栈：Python（sqlite3）或 Node（better-sqlite3）。

### 验收

- 冷启动后第一次字典查询不明显卡顿；多次查询稳定。
- 常用检索（单字、拼音）稳定返回结果，排序符合预期。

## 方案 B：JSON 继续用，但加“轻量索引/分片”

适合不想引入数据库的情况：

- 将 `word.json` 拆分为：
  - `single_char.json`（单字词条）
  - `words.json`（多字词条）
  - `pinyin_index.json`（拼音 → word 列表）
- 或把词条按首字/拼音首字母分片：`w_a.json`、`w_b.json`…

优点：资产仍是 JSON、生成简单。缺点：查询能力与维护成本仍高于数据库方案。

## 推荐落地路线（与当前代码兼容）

1) **短期（已完成/在做）**：按需加载 + UI 结果列表/拼音检索（不影响首屏）。
2) **中期（已完成）**：引入预置 SQLite（不改 UI），`WordRepository` 使用 DB 查询实现（见 `app/src/main/java/com/yourstudio/hskstroke/bishun/data/word/WordRepository.kt`）。
3) **长期**：加入 FTS/更复杂排序，支持收藏/生词本与跨版本数据迁移（必要时引入 Room/迁移策略）。
