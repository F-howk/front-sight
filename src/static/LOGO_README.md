# Logo 使用说明

## 文件说明

| 文件 | 格式 | 用途 |
|------|------|------|
| `logo.svg` | SVG | 主logo(带文字),用于官网、宣传材料 |
| `logo-icon.svg` | SVG | App图标(无文字),用于应用内显示 |
| `logo-light.svg` | SVG | 浅色主题版本 |
| `logo-favicon.svg` | SVG | 简化版,用于favicon |
| `logo-transparent.svg` | SVG | 透明背景,用于水印、覆盖层 |
| `logo.png` | PNG | 旧版图标(待替换) |

## 更新记录

- [x] 2024-01: 创建新logo系统
- [x] 更新主页 logo 引用为 SVG 版本

## 待办事项

### 1. 生成PNG图标
uni-app 打包 App 需要以下尺寸的 PNG 图标:
- 192x192 - Android adaptive icon
- 144x144 - Android HDPI
- 96x96 - Android MDPI
- 72x72 - Android HDPI
- 48x48 - Android MDPI
- 512x512 - App Store / Google Play

**工具推荐:**
- 在线工具: https://cloudconvert.com/svg-to-png
- 在线工具: https://convertio.co/zh/svg-png/
- 本地工具: ImageMagick `convert logo.svg -resize 512x512 logo-512.png`

### 2. 更新 manifest.json
HBuilderX 可视化界面配置应用图标:
1. 打开 HBuilderX
2. 右键项目 → 发行 → 原生App-云打包
3. 在"图标配置"中上传新的 PNG 图标

### 3. 更新 pages.json (可选)
如需配置导航栏标题图标等。

## 设计规范

- **主色调**: #00ff88 (荧光绿)
- **背景色**: #1a1a2e → #16213e (深蓝渐变)
- **设计元素**: 十字准星、战术角标记、中心点
- **风格**: 现代、专业、科技感

## SVG 使用方法

```vue
<!-- 应用内图标 -->
<image src="/static/logo-icon.svg" mode="aspectFit" />

<!-- 背景图 -->
<view style="background-image: url('/static/logo-transparent.svg')"></view>

```

## 注意事项

1. SVG 在部分小程序平台可能不支持,建议测试后使用
2. App 打包时优先使用 PNG 格式的图标
3. 旧版 `logo.png` 可以在测试无误后删除
