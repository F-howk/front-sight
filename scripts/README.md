# Logo 图标生成脚本

本项目包含自动生成 PNG 图标的脚本工具。

## 已生成的图标

✅ 所有PNG图标已生成在 `src/static/icons/` 目录:

| 尺寸 | 文件名 | 用途 |
|------|--------|------|
| 512x512 | logo-512x512.png | App Store / Google Play 图标 |
| 192x192 | logo-192x192.png | Android xxxhdpi (4x) |
| 144x144 | logo-144x144.png | Android xxhdpi (3x) |
| 96x96 | logo-96x96.png | Android xhdpi (2x) |
| 72x72 | logo-72x72.png | Android hdpi (1.5x) |
| 48x48 | logo-48x48.png | Android mdpi (1x) |

## 快速开始

### 方法 1: 使用 Node.js (推荐)

```bash
# 已安装依赖,直接运行
node scripts/generate-icons.js
```

### 方法 2: 使用 Bash (macOS/Linux)

```bash
# 安装 ImageMagick (如未安装)
brew install imagemagick  # macOS
sudo apt-get install imagemagick  # Ubuntu

# 运行脚本
bash scripts/generate-icons.sh
```

### 方法 3: 使用 Windows 批处理

```cmd
# 安装 ImageMagick: https://imagemagick.org/script/download.php
# 运行脚本
scripts\generate-icons.bat
```

## 文件结构

```
scripts/
├── generate-icons.js    # Node.js 脚本 (推荐)
├── generate-icons.sh    # Bash 脚本
└── generate-icons.bat   # Windows 批处理

src/static/
├── logo.svg                # 主 logo (带文字)
├── logo-icon.svg           # App 图标源 (用于生成 PNG)
├── logo-light.svg          # 浅色主题
├── logo-favicon.svg        # Favicon
├── logo-transparent.svg    # 透明背景
├── logo.png                # 主 PNG 图标 (512x512)
└── icons/                  # 生成的各种尺寸 PNG
    ├── logo-512x512.png
    ├── logo-192x192.png
    ├── logo-144x144.png
    ├── logo-96x96.png
    ├── logo-72x72.png
    └── logo-48x48.png
```

## 重新生成图标

如果修改了 SVG 源文件,重新运行生成脚本即可:

```bash
node scripts/generate-icons.js
```

## 在 HBuilderX 中配置应用图标

1. 打开 HBuilderX
2. 右键项目 → 发行 → 原生App-云打包
3. 点击"图标"配置区域
4. 上传 `src/static/icons/logo-512x512.png` 作为应用图标
5. 系统会自动生成其他所需尺寸

## 设计规范

- **主色**: #00ff88 (荧光绿)
- **背景**: #1a1a2e → #16213e (深蓝渐变)
- **元素**: 十字准星 + 战术角标记
- **风格**: 现代、科技感、专业

## 故障排除

### Node.js 脚本报错 "Cannot find module 'sharp'"

```bash
npm install --save-dev sharp
```

### convert 命令不可用

安装 ImageMagick:
- Windows: https://imagemagick.org/script/download.php
- macOS: `brew install imagemagick`
- Linux: `sudo apt-get install imagemagick`

### 生成的 PNG 显示异常

检查 SVG 源文件是否正确,确保使用 `logo-icon.svg` 作为源文件。
