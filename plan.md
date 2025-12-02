# Hanzi Stroke Order – 上线前计划（v1.0）

> 目标：在 2026-01 前完成 Google Play 上架所需的合规、素材、测试与提交流程。此计划会随着任务推进勾选更新。

## 1. 可访问性与 UI 交付
| 状态 | 任务 | 说明 |
| --- | --- | --- |
| ☐ | 图标按钮无障碍检查 | 确认所有 `IconActionButton` 设置了准确的 `contentDescription`，必要时补充文案。 |
| ☐ | 真机 UI 审查 | 在 API 26 / 30 / 34 竖屏设备上验证演示、练习、课程、字卡、TTS、描红、设置，记录截图与问题。 |
| ☑ | 课程帮助 / Legend 文案 | 课程介绍、Legend、隐私弹窗已切换到多语言版，与后续截图一致。 |
| ☑ | 多语言入口 | 顶部新增 Language 菜单，可切换 System / English / Español / 日本語，并写入偏好数据。 |

## 2. 合规与文档
| 状态 | 任务 | 说明 |
| --- | --- | --- |
| ☑ | 隐私政策撰写 | `docs/privacy-policy.md` 完成，UI 亦可查看全文。 |
| ☐ | 隐私政策托管 URL | 将 Markdown 部署到可访问链接（GitHub Pages / 个人域名）以供 Play Console 提交。 |
| ☑ | Help / Onboarding 文档 | `docs/help-guide.md` 完成，帮助对话框同步。 |
| ☐ | 数据安全问卷 | 根据“仅本地存储、不收集”整理 Play Console 回答。 |

## 3. 素材与商店资产
| 状态 | 任务 | 说明 |
| --- | --- | --- |
| ☐ | 自适应图标 | 生成 512×512 透明前景 + 背景色，更新 `mipmap-anydpi-v26`。 |
| ☐ | Feature Graphic | 1024×500 PNG，展示关键场景。 |
| ☐ | 截图集 | ≥8 张竖屏手机截图（演示、练习、课程、字卡、设置等），如支持平板再补 2 张。 |
| ☐ | 商店文案 | 标题、短描述（≤80 字符）、完整描述（≤4000 字符）、首版 Release notes。 |

## 4. 工程与 QA
| 状态 | 任务 | 说明 |
| --- | --- | --- |
| ☑ | Progress / History 面板 | Profile → Progress 已展示统计、周视图、练习历史。 |
| ☑ | 课程目录与浮动控件 | 课程目录支持折叠、彩色标识，练习徽章合并完成。 |
| ☐ | 版本号 / 签名 | 在 `build.gradle` 设定 `versionCode` / `versionName`，配置 release keystore。 |
| ☐ | Release 构建 | `./gradlew clean bundleRelease`，保管 `.aab` 与 `mapping.txt`。 |
| ☐ | 手动回归清单 | 记录测试结果（Pass / Fail / 备注），包括真机截图或视频。 |
| ☐ | 崩溃 / 日志自检 | 检查 logcat，确认无 fatal；验证反馈日志写入 / 清理流程。 |

## 5. Play Console 操作
| 状态 | 任务 | 说明 |
| --- | --- | --- |
| ☐ | 创建应用条目 | 填写默认语言、分类、联系方式。 |
| ☐ | 填写应用内容 | 数据安全问卷、隐私政策链接、内容分级、目标受众。 |
| ☐ | 上传 Internal 测试版 | 提交 `.aab`，填写 release notes，邀请内测账号。 |
| ☐ | 处理审核反馈 | 若被驳回或需补材料，更新此计划并记录修改。 |

## 更新指引
1. 每完成或新增任务，直接在表格更新状态（☑/☐）。  
2. 若需要 Codex 协助，回复时引用任务条目或章节。  
3. 该计划作为上线准备唯一追踪表，直至应用进入 Production。  
