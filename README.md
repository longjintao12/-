# NovaBox（星盒）

一个完全自研、不依赖 TVBox 代码的影视聚合播放器（Android 手机版）。

> 代码 100% 原创，不引用 TVBox/影视仓任何源码（规避 AGPL 协议传染风险）；
> 数据接口兼容主流影视源 HTTP API，因此现有影视源（饭太硬聚合、网盘源等）可直接使用。

## ✨ 特性

- 多源管理：添加 / 删除 / 启停 / 排序 / 连通性测试，支持带 token 的源
- 首页：推荐轮播、分类导航、分类内容横滑
- 分类浏览：网格列表 + 上拉分页 + 下拉刷新
- 聚合搜索：单源/多源合并结果、搜索历史
- 详情页：海报、简介、演员、多线路切换、选集
- 播放：Media3 (ExoPlayer)，手势亮度/音量/进度、倍速、自动连播、断点续播
- 收藏与历史：本地数据库
- 网盘 Cookie 管理：夸克 / UC / 阿里等，请求自动携带
- 自定义 UA / Referer（网盘直链必备）
- Material 3 风格，深色模式自适应

## 🛠 构建

GitHub Actions 云编译：push 到 `main` 自动构建 Debug APK，产物在 Actions 页面 Artifact 中下载。

```bash
# 本地构建（可选）
./gradlew assembleDebug
```

## 📡 数据源格式

兼容标准影视源 API：

| 接口 | 参数 | 说明 |
|---|---|---|
| 首页/分类 | `ac=list` | 返回 `class`（分类）与推荐 `list` |
| 内容列表 | `ac=detail&t={分类ID}&pg={页码}` | 分类分页 |
| 搜索 | `ac=detail&wd={关键词}` | 失败时自动回退 `ac=search` |
| 详情 | `ac=detail&ids={vod_id}` | 含线路与播放地址 |

可选 `token` 参数（部分源需要）。

## 📄 License

MIT
