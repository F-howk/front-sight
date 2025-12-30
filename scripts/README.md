# 资源生成工具集

<div align="center">

Front Sight 内部自动化工具

图标生成 • 启动页转换 • 资源处理

</div>

---

## 目录

- [概述](#概述)
- [工具列表](#工具列表)
- [快速开始](#快速开始)
- [详细说明](#详细说明)
- [文件结构](#文件结构)
- [设计规范](#设计规范)
- [故障排除](#故障排除)

---

## 概述

本项目包含一系列自动化工具，用于生成应用所需的各类资源文件。所有工具均支持跨平台运行（Windows / macOS / Linux）。

**核心功能：**
- 🖼️ 图标生成：从 SVG 源文件自动生成多尺寸 PNG 图标
- 📱 启动页转换：SVG 启动页转 PNG 格式
- 🔄 批量处理：一次性生成所有所需尺寸
- 🎨 高质量输出：使用 Sharp 库保证图片质量

---

## 工具列表

| 工具 | 功能 | 输入 | 输出 | 推荐度 |
|------|------|------|------|--------|
| `generate-icons.js` | 生成多尺寸图标 | SVG | PNG | ⭐⭐⭐⭐⭐ |
| `convert-splash.js` | 转换启动页 | SVG | PNG | ⭐⭐⭐⭐⭐ |
| `generate-icons.sh` | Bash 版本图标生成 | SVG | PNG | ⭐⭐⭐ |
| `generate-icons.bat` | Windows 批处理版 | SVG | PNG | ⭐⭐⭐ |

---

## 快速开始

### 前置要求

```bash
# 安装项目依赖（包含 Sharp）
npm install

# 如果单独使用 Sharp
npm install sharp
```

### 一键生成所有资源

```bash
# 生成图标
node scripts/generate-icons.js

# 生成启动页
npm run splash:png
```

---

## 详细说明

### 1. 图标生成工具

#### Node.js 版本（推荐）

**使用方法：**

```bash
node scripts/generate-icons.js
```

**优点：**
- ✅ 跨平台兼容
- ✅ 无需额外依赖
- ✅ 高质量输出
- ✅ 详细日志输出

**输出示例：**

```
========================================
   Logo PNG 图标生成工具
========================================

SVG 源文件: d:\demo\front-sight\src/static/logo-icon.svg
输出目录: d:\demo\front-sight\src/static/icons

⏳ 正在生成 PNG 图标...

  ✅ logo-512x512.png
  ✅ logo-192x192.png
  ✅ logo-144x144.png
  ✅ logo-96x96.png
  ✅ logo-72x72.png
  ✅ logo-48x48.png

========================================
🎉 所有图标生成完成!
========================================

📁 输出目录: d:\demo\front-sight\src/static/icons

   logo-512x512.png (12.5 KB)
   logo-192x192.png (4.2 KB)
   logo-144x144.png (2.8 KB)
   logo-96x96.png (1.6 KB)
   logo-72x72.png (1.1 KB)
   logo-48x48.png (0.7 KB)
```

---

#### Bash 版本（macOS/Linux）

**前置要求：**

```bash
# macOS
brew install imagemagick

# Ubuntu/Debian
sudo apt-get install imagemagick

# Fedora/RHEL
sudo dnf install imagemagick
```

**使用方法：**

```bash
bash scripts/generate-icons.sh
```

---

#### Windows 批处理版

**前置要求：**

1. 下载 ImageMagick: https://imagemagick.org/script/download.php
2. 安装时确保勾选 "Add to system PATH"

**使用方法：**

```cmd
scripts\generate-icons.bat
```

---

### 2. 启动页转换工具

将 SVG 启动页转换为 PNG 格式，用于 App 打包。

**使用方法：**

```bash
npm run splash:png
# 或
node scripts/convert-splash.js
```

**输出规格：**

| 属性 | 值 |
|------|-----|
| 输入文件 | `src/static/splash.svg` |
| 输出文件 | `src/static/splash.png` |
| 输出尺寸 | 1080 x 1920 像素 |
| 背景色 | #1a1a2e |
| 密度 | 144 DPI |

---

## 文件结构

```
scripts/
├── generate-icons.js      # Node.js 图标生成工具 ⭐
├── generate-icons.sh      # Bash 图标生成工具
├── generate-icons.bat     # Windows 批处理工具
├── convert-splash.js      # 启动页转换工具 ⭐
└── README.md              # 本文档

src/static/
├── logo.svg                # 主 Logo（带文字）
├── logo-icon.svg           # App 图标源（无文字）⭐
├── logo-light.svg          # 浅色主题版本
├── logo-favicon.svg        # 网站图标
├── logo-transparent.svg    # 透明背景版本
├── splash.svg              # 启动页源文件 ⭐
├── splash.png              # 生成的启动页 PNG
├── logo.png                # 旧版图标（兼容）
├── icons/                  # 生成的图标目录 ⭐
│   ├── logo-512x512.png    # App Store / Google Play
│   ├── logo-192x192.png    # Android xxxhdpi
│   ├── logo-144x144.png    # Android xxhdpi
│   ├── logo-96x96.png      # Android xhdpi
│   ├── logo-72x72.png      # Android hdpi
│   └── logo-48x48.png      # Android mdpi
└── android/                # Android 原生图标
    ├── mipmap-mdpi/
    │   └── ic_launcher.png
    ├── mipmap-hdpi/
    │   └── ic_launcher.png
    ├── mipmap-xhdpi/
    │   └── ic_launcher.png
    ├── mipmap-xxhdpi/
    │   └── ic_launcher.png
    └── mipmap-xxxhdpi/
        └── ic_launcher.png
```

---

## 设计规范

### Logo 设计

| 属性 | 值 | 说明 |
|------|-----|------|
| 主色调 | #00ff88 | 荧光绿，高辨识度 |
| 背景色 | #1a1a2e → #16213e | 深蓝渐变 |
| 设计元素 | 十字准星 + 战术角标记 | 符合应用主题 |
| 风格 | 现代、科技感、专业 | 面向游戏用户 |
| 字体 | 无衬线 / 等宽 | 技术感 |

### 图标规格

| 用途 | 尺寸 | 文件 | 平台 |
|------|------|------|------|
| App Store | 512x512 | logo-512x512.png | iOS |
| Google Play | 512x512 | logo-512x512.png | Android |
| Android xxxhdpi | 192x192 | logo-192x192.png | Android (4x) |
| Android xxhdpi | 144x144 | logo-144x144.png | Android (3x) |
| Android xhdpi | 96x96 | logo-96x96.png | Android (2x) |
| Android hdpi | 72x72 | logo-72x72.png | Android (1.5x) |
| Android mdpi | 48x48 | logo-48x48.png | Android (1x) |

### 启动页规格

| 属性 | 值 |
|------|-----|
| 尺寸 | 1080 x 1920 (竖屏) |
| 格式 | PNG-24 |
| 背景色 | #1a1a2e |
| 内容 | Logo + 应用名称 |

---

## 故障排除

### 问题 1: Node.js 脚本报错 "Cannot find module 'sharp'"

**原因：** 未安装 Sharp 依赖

**解决方案：**

```bash
# 安装 Sharp
npm install sharp

# 或作为开发依赖
npm install --save-dev sharp
```

---

### 问题 2: Sharp 安装失败（Windows）

**原因：** 缺少 Visual C++ 构建工具

**解决方案：**

```bash
# 使用 Windows 构建工具
npm install --global windows-build-tools

# 然后重新安装 Sharp
npm install sharp
```

或直接安装预编译版本：

```bash
npm install sharp --no-optional
```

---

### 问题 3: convert 命令不可用

**原因：** 未安装 ImageMagick

**解决方案：**

| 平台 | 安装命令 |
|------|----------|
| Windows | 下载 https://imagemagick.org/script/download.php |
| macOS | `brew install imagemagick` |
| Ubuntu | `sudo apt-get install imagemagick` |
| Fedora | `sudo dnf install imagemagick` |

---

### 问题 4: 生成的 PNG 显示异常

**可能原因及解决方案：**

| 症状 | 原因 | 解决方案 |
|------|------|----------|
| 背景不透明 | SVG 缺少透明声明 | 检查 SVG 源文件 |
| 颜色偏差 | 色彩空间问题 | 使用 sRGB 色彩空间 |
| 模糊 | 分辨率不足 | 提高源文件 DPI |
| 裁剪 | ViewBox 设置错误 | 检查 SVG viewBox |

---

### 问题 5: HBuilderX 打包时图标未生效

**解决方案：**

1. 确认图标路径：`src/static/icons/logo-512x512.png`
2. 打开 HBuilderX
3. 右键项目 → **发行** → **原生 App-云打包**
4. 点击 **图标** 配置区域
5. 上传 `logo-512x512.png`
6. 系统会自动生成其他所需尺寸

---

### 问题 6: SVG 文件无法识别

**检查清单：**

- ✅ 文件扩展名为 `.svg`
- ✅ 文件格式为有效的 XML
- ✅ SVG 命名空间正确：`xmlns="http://www.w3.org/2000/svg"`
- ✅ 文件编码为 UTF-8
- ✅ 文件路径不含特殊字符

---

## 高级用法

### 批量生成所有资源

创建一个批处理脚本：

```bash
#!/bin/bash
# generate-all.sh

echo "开始生成所有资源..."

# 生成图标
node scripts/generate-icons.js

# 生成启动页
npm run splash:png

# 复制到 Android 目录
cp src/static/icons/*.png src/static/android/mipmap-xxxhdpi/

echo "✅ 所有资源生成完成!"
```

### 自定义图标尺寸

编辑 `generate-icons.js`，修改 `SIZES` 数组：

```javascript
// 添加自定义尺寸
const SIZES = [512, 192, 144, 96, 72, 48, 256, 128];
```

### 调整输出质量

在 Sharp 配置中添加质量参数：

```javascript
await sharp(SVG_FILE)
  .resize(size, size)
  .png({ quality: 90, compressionLevel: 9 })  // 高质量
  .toFile(outputFile);
```

---

## 最佳实践

### 1. SVG 设计原则

- ✅ 使用矢量路径，避免栅格化
- ✅ 保持简洁，减少节点数量
- ✅ 使用相对坐标，便于缩放
- ✅ 设置合适的 viewBox
- ✅ 避免使用外部字体

### 2. 版本控制

```
# .gitignore 示例

# SVG 源文件 - 保留
src/static/*.svg

# 生成的 PNG - 可选（建议保留）
src/static/icons/*.png

# 转换的启动页 - 可选
src/static/splash.png
```

### 3. 持续集成

在 CI/CD 流程中自动生成图标：

```yaml
# .github/workflows/assets.yml
name: Generate Assets

on: [push, pull_request]

jobs:
  generate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      - name: Install dependencies
        run: npm install
      - name: Generate icons
        run: node scripts/generate-icons.js
      - name: Upload artifacts
        uses: actions/upload-artifact@v2
        with:
          name: icons
          path: src/static/icons/
```

---

## 相关文档

- [主 README](../README.md)
- [Logo 使用说明](../src/static/LOGO_README.md)
- [Java 模块文档](../java/README.md)
- [Sharp 文档](https://sharp.pixelplumbing.app/)
- [ImageMagick 文档](https://imagemagick.org/index.php)
