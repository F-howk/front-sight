/**
 * Logo PNG 生成脚本 (Node.js)
 * 使用 sharp 库从 SVG 生成 PNG 图标
 *
 * 依赖安装: npm install sharp
 * 运行: node scripts/generate-icons.js
 */

const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

const PROJECT_ROOT = path.resolve(__dirname, '..');
const SVG_FILE = path.join(PROJECT_ROOT, 'src/static/logo-icon.svg');
const OUTPUT_DIR = path.join(PROJECT_ROOT, 'src/static/icons');

// 需要生成的尺寸
const SIZES = [512, 192, 144, 96, 72, 48];

async function generateIcons() {
  console.log('');
  console.log('========================================');
  console.log('   Logo PNG 图标生成工具');
  console.log('========================================');
  console.log('');
  console.log(`SVG 源文件: ${SVG_FILE}`);
  console.log(`输出目录: ${OUTPUT_DIR}`);
  console.log('');

  // 检查 sharp 是否安装
  try {
    sharp.format;
  } catch (err) {
    console.error('❌ 错误: 未安装 sharp 库');
    console.error('');
    console.error('请运行: npm install sharp');
    process.exit(1);
  }

  // 检查 SVG 文件是否存在
  if (!fs.existsSync(SVG_FILE)) {
    console.error('❌ 错误: 找不到 SVG 文件');
    console.error(`   ${SVG_FILE}`);
    process.exit(1);
  }

  // 创建输出目录
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  }

  console.log('⏳ 正在生成 PNG 图标...');
  console.log('');

  // 生成各个尺寸的 PNG
  for (const size of SIZES) {
    const outputFile = path.join(OUTPUT_DIR, `logo-${size}x${size}.png`);

    try {
      await sharp(SVG_FILE)
        .resize(size, size)
        .png()
        .toFile(outputFile);

      console.log(`  ✅ logo-${size}x${size}.png`);
    } catch (err) {
      console.error(`  ❌ ${size}x${size} 生成失败: ${err.message}`);
    }
  }

  console.log('');
  console.log('========================================');
  console.log('🎉 所有图标生成完成!');
  console.log('========================================');
  console.log('');
  console.log(`📁 输出目录: ${OUTPUT_DIR}`);

  // 列出生成的文件
  const files = fs.readdirSync(OUTPUT_DIR);
  console.log('');
  files.forEach(file => {
    const filePath = path.join(OUTPUT_DIR, file);
    const stats = fs.statSync(filePath);
    console.log(`   ${file} (${(stats.size / 1024).toFixed(1)} KB)`);
  });
  console.log('');
}

generateIcons().catch(err => {
  console.error('错误:', err);
  process.exit(1);
});
