const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const svgPath = path.join(__dirname, '../src/static/splash.svg');
const pngPath = path.join(__dirname, '../src/static/splash.png');

async function convertSvgToPng() {
  try {
    const svgBuffer = fs.readFileSync(svgPath);

    await sharp(svgBuffer, { density: 144 })
      .resize(1080, 1920, { fit: 'cover', background: '#1a1a2e' })
      .png()
      .toFile(pngPath);

    console.log('启动页生成成功:', pngPath);
  } catch (error) {
    console.error('转换失败:', error.message);
    process.exit(1);
  }
}

convertSvgToPng();
