#!/bin/bash

# Logo PNG 生成脚本
# 需要安装 ImageMagick: brew install imagemagick (macOS) 或 apt-get install imagemagick (Linux)

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
SVG_FILE="$PROJECT_ROOT/src/static/logo-icon.svg"
OUTPUT_DIR="$PROJECT_ROOT/src/static/icons"

# 创建输出目录
mkdir -p "$OUTPUT_DIR"

# 定义需要生成的尺寸
SIZES=(512 192 144 96 72 48)

echo "🎨 开始生成 PNG 图标..."
echo "SVG 源文件: $SVG_FILE"
echo "输出目录: $OUTPUT_DIR"
echo ""

# 检查 ImageMagick 是否安装
if ! command -v convert &> /dev/null; then
    echo "❌ 错误: 未安装 ImageMagick"
    echo ""
    echo "请安装 ImageMagick:"
    echo "  macOS:   brew install imagemagick"
    echo "  Ubuntu:  sudo apt-get install imagemagick"
    echo "  Windows: 下载 https://imagemagick.org/script/download.php"
    exit 1
fi

# 检查 SVG 文件是否存在
if [ ! -f "$SVG_FILE" ]; then
    echo "❌ 错误: 找不到 SVG 文件 $SVG_FILE"
    exit 1
fi

# 生成各个尺寸的 PNG
for size in "${SIZES[@]}"; do
    OUTPUT_FILE="$OUTPUT_DIR/logo-${size}x${size}.png"
    echo "⏳ 生成 ${size}x${size}..."
    convert -background none -density 300 -resize "${size}x${size}" "$SVG_FILE" "$OUTPUT_FILE"

    if [ $? -eq 0 ]; then
        echo "✅ 已生成: $OUTPUT_FILE"
    else
        echo "❌ 生成失败: $OUTPUT_FILE"
    fi
done

echo ""
echo "🎉 所有图标生成完成!"
echo "📁 输出目录: $OUTPUT_DIR"
ls -lh "$OUTPUT_DIR"
