<template>
  <view class="sight-container" :class="{ fixed: fixed, relative: !fixed }" :style="{ opacity: config.opacity }">
    <!-- Canvas 绘制准星（非 H5 端） -->
    <canvas
      v-if="isVisible && !isH5"
      class="sight-canvas"
      :canvas-id="canvasId"
      :id="canvasId"
      :style="canvasStyle"
    />

    <!-- SVG 渲染方式（H5 端） -->
    <svg
      v-if="isVisible && isH5"
      class="sight-svg"
      :width="realCanvasWidth"
      :height="realCanvasHeight"
      :style="canvasStyle"
      viewBox="0 0 200 200"
    >      <g :stroke="config.color" :stroke-width="config.thickness" fill="none">
        <!-- 十字准星 -->
        <template v-if="config.type === 'cross'">
          <line
            :x1="logicalCenter"
            :y1="logicalCenter - config.size"
            :x2="logicalCenter"
            :y2="logicalCenter - gap"
          />
          <line
            :x1="logicalCenter"
            :y1="logicalCenter + gap"
            :x2="logicalCenter"
            :y2="logicalCenter + config.size"
          />
          <line
            :x1="logicalCenter - config.size"
            :y1="logicalCenter"
            :x2="logicalCenter - gap"
            :y2="logicalCenter"
          />
          <line
            :x1="logicalCenter + gap"
            :y1="logicalCenter"
            :x2="logicalCenter + config.size"
            :y2="logicalCenter"
          />
        </template>

        <!-- 圆形准星 -->
        <template v-else-if="config.type === 'circle'">
          <circle
            :cx="logicalCenter"
            :cy="logicalCenter"
            :r="config.size"
            :stroke-dasharray="dashArray"
          />
        </template>

        <!-- 战术准星 -->
        <template v-else-if="config.type === 'tactical'">
          <circle :cx="logicalCenter" :cy="logicalCenter" :r="config.size * 0.3" />
          <line
            :x1="logicalCenter - config.size"
            :y1="logicalCenter"
            :x2="logicalCenter - config.size * 0.5"
            :y2="logicalCenter"
          />
          <line
            :x1="logicalCenter + config.size * 0.5"
            :y1="logicalCenter"
            :x2="logicalCenter + config.size"
            :y2="logicalCenter"
          />
          <line
            :x1="logicalCenter"
            :y1="logicalCenter - config.size"
            :x2="logicalCenter"
            :y2="logicalCenter - config.size * 0.5"
          />
          <line
            :x1="logicalCenter"
            :y1="logicalCenter + config.size * 0.5"
            :x2="logicalCenter"
            :y2="logicalCenter + config.size"
          />
        </template>

        <!-- 方括号准星 -->
        <template v-else-if="config.type === 'bracket'">
          <!-- 左上 -->
          <path
            :d="`M ${logicalCenter - config.size} ${logicalCenter - config.size * 0.6}
                   L ${logicalCenter - config.size} ${logicalCenter - config.size}
                   L ${logicalCenter - config.size * 0.6} ${logicalCenter - config.size}`"
          />
          <!-- 右上 -->
          <path
            :d="`M ${logicalCenter + config.size * 0.6} ${logicalCenter - config.size}
                   L ${logicalCenter + config.size} ${logicalCenter - config.size}
                   L ${logicalCenter + config.size} ${logicalCenter - config.size * 0.6}`"
          />
          <!-- 左下 -->
          <path
            :d="`M ${logicalCenter - config.size} ${logicalCenter + config.size * 0.6}
                   L ${logicalCenter - config.size} ${logicalCenter + config.size}
                   L ${logicalCenter - config.size * 0.6} ${logicalCenter + config.size}`"
          />
          <!-- 右下 -->
          <path
            :d="`M ${logicalCenter + config.size * 0.6} ${logicalCenter + config.size}
                   L ${logicalCenter + config.size} ${logicalCenter + config.size}
                   L ${logicalCenter + config.size} ${logicalCenter + config.size * 0.6}`"
          />
        </template>

        <!-- V形准星 -->
        <template v-else-if="config.type === 'chevron'">
          <path
            :d="`M ${logicalCenter - config.size * 0.6} ${logicalCenter - config.size * 0.3}
                   L ${logicalCenter} ${logicalCenter + config.size * 0.5}
                   L ${logicalCenter + config.size * 0.6} ${logicalCenter - config.size * 0.3}`"
          />
          <line
            :x1="logicalCenter"
            :y1="logicalCenter - config.size"
            :x2="logicalCenter"
            :y2="logicalCenter - config.size * 0.3"
          />
        </template>

        <!-- 象限准星 -->
        <template v-else-if="config.type === 'quadrant'">
          <!-- 从中心向四个方向延伸到屏幕边缘 -->
          <!-- 右侧水平线 -->
          <line
            :x1="logicalCenter"
            :y1="logicalCenter"
            :x2="200"
            :y2="logicalCenter"
          />
          <!-- 上方垂直线 -->
          <line
            :x1="logicalCenter"
            :y1="logicalCenter"
            :x2="logicalCenter"
            :y2="0"
          />
          <!-- 左侧水平线 -->
          <line
            :x1="logicalCenter"
            :y1="logicalCenter"
            :x2="0"
            :y2="logicalCenter"
          />
          <!-- 下方垂直线 -->
          <line
            :x1="logicalCenter"
            :y1="logicalCenter"
            :x2="logicalCenter"
            :y2="200"
          />
        </template>
      </g>

      <!-- 中心点 -->
      <circle
        v-if="config.showDot && config.type !== 'dot'"
        :cx="logicalCenter"
        :cy="logicalCenter"
        :r="config.dotSize"
        :fill="config.color"
      />

      <!-- 单点准星 -->
      <circle
        v-if="config.type === 'dot'"
        :cx="logicalCenter"
        :cy="logicalCenter"
        :r="config.dotSize * 2"
        :fill="config.color"
      />
    </svg>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick, getCurrentInstance } from 'vue';
import type { SightConfig } from '../types/sight';

interface Props {
  config: SightConfig;
  visible?: boolean;
  fixed?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  fixed: true,
});

const canvasId = `sight-${Math.random().toString(36).substr(2, 9)}`;
const ctx = ref<UniApp.CanvasContext | null>(null);
const logicalCanvasSize = 200; // Internal logical drawing space
const logicalCenter = 100;     // Center of the logical drawing space

const realCanvasWidth = ref(logicalCanvasSize);  // Actual rendered width of the canvas/svg element
const realCanvasHeight = ref(logicalCanvasSize); // Actual rendered height of the canvas/svg element
const isH5 = ref(false);

const instance = getCurrentInstance(); // For createSelectorQuery().in(this)

const measureCanvasDimensions = () => {
  nextTick(() => { // Ensure DOM is updated
    uni.createSelectorQuery()
      .in(instance?.proxy || null) // Use current instance proxy if available, or null for global scope
      .select(`#${canvasId}`)
      .boundingClientRect((data) => {
        const rect = Array.isArray(data) ? data[0] : data;
        if (rect) {
          realCanvasWidth.value = rect?.width || 0;
          realCanvasHeight.value = rect?.height || 0;
          if (!isH5.value) { // Only redraw canvas if not H5 and canvas is active
            drawSight();
          }
        }
      }).exec();
  });
};

// 检测运行环境
onMounted(() => {
  // #ifdef H5
  isH5.value = true;
  // #endif
  // #ifndef H5
  isH5.value = false;
  // #endif

  if (!isH5.value) {
    initCanvas();
  }
  // Measure after initial render
  measureCanvasDimensions();
});

const isVisible = computed(() => props.visible);

const canvasStyle = computed(() => {
  if (props.fixed) {
    return {
      width: `${logicalCanvasSize}px`, // Use logical size for fixed mode container
      height: `${logicalCanvasSize}px`,
    };
  }
  return {
    width: '100%',
    height: '100%',
  };
});

const gap = computed(() => Math.max(props.config.size * 0.2, 5));

const dashArray = computed(() => {
  if (props.config.type === 'circle') {
    return `${props.config.size * 0.5} ${props.config.size * 0.3}`;
  }
  return '';
});

// 初始化 Canvas
const initCanvas = () => {
  ctx.value = uni.createCanvasContext(canvasId, instance?.proxy as any); // Pass component instance to ensure correct scope
  drawSight();
};

// 绘制准星
const drawSight = () => {
  if (!ctx.value) return;

  const { config } = props;
  const c = ctx.value;

  // Clear the actual rendered canvas size
  c.clearRect(0, 0, realCanvasWidth.value, realCanvasHeight.value);

  c.save(); // Save the current state of the context

  // Calculate scale factor
  const scaleX = realCanvasWidth.value / logicalCanvasSize;
  const scaleY = realCanvasHeight.value / logicalCanvasSize;
  const scale = Math.min(scaleX, scaleY); // Use the smaller scale factor to fit

  const translateX = (realCanvasWidth.value - logicalCanvasSize * scale) / 2;
  const translateY = (realCanvasHeight.value - logicalCanvasSize * scale) / 2;

  // First, translate to center the drawing area
  c.translate(translateX, translateY);
  // Then, apply uniform scaling
  c.scale(scale, scale);


  c.setStrokeStyle(config.color);
  // Line width is also affected by scale, so we need to adjust for it.
  c.setLineWidth(config.thickness / scale);
  c.setGlobalAlpha(config.opacity);

  switch (config.type) {
    case 'cross':
      drawCross(c, config);
      break;
    case 'dot':
      drawDot(c, config);
      break;
    case 'tactical':
      drawTactical(c, config);
      break;
    case 'circle':
      drawCircle(c, config);
      break;
    case 'bracket':
      drawBracket(c, config);
      break;
    case 'chevron':
      drawChevron(c, config);
      break;
    case 'quadrant':
      drawQuadrant(c, config);
      break;
  }

  if (config.showDot && config.type !== 'dot') {
    c.setFillStyle(config.color);
    c.beginPath();
    c.arc(logicalCenter, logicalCenter, config.dotSize, 0, Math.PI * 2);
    c.fill();
  }

  c.restore(); // Restore context state (undo scale)
  c.draw();
};

// 绘制十字准星
const drawCross = (c: UniApp.CanvasContext, config: SightConfig) => {
  const g = gap.value;

  c.beginPath();
  // 上
  c.moveTo(logicalCenter, logicalCenter - config.size);
  c.lineTo(logicalCenter, logicalCenter - g);
  // 下
  c.moveTo(logicalCenter, logicalCenter + g);
  c.lineTo(logicalCenter, logicalCenter + config.size);
  // 左
  c.moveTo(logicalCenter - config.size, logicalCenter);
  c.lineTo(logicalCenter - g, logicalCenter);
  // 右
  c.moveTo(logicalCenter + g, logicalCenter);
  c.lineTo(logicalCenter + config.size, logicalCenter);
  c.stroke();
};

// 绘制单点准星
const drawDot = (c: UniApp.CanvasContext, config: SightConfig) => {
  c.setFillStyle(config.color);
  c.beginPath();
  c.arc(logicalCenter, logicalCenter, config.dotSize * 2, 0, Math.PI * 2);
  c.fill();
};

// 绘制战术准星
const drawTactical = (c: UniApp.CanvasContext, config: SightConfig) => {
  const innerRadius = config.size * 0.3;

  // 内圆
  c.beginPath();
  c.arc(logicalCenter, logicalCenter, innerRadius, 0, Math.PI * 2);
  c.stroke();

  // 外围短线
  c.beginPath();
  c.moveTo(logicalCenter - config.size, logicalCenter);
  c.lineTo(logicalCenter - config.size * 0.5, logicalCenter);
  c.moveTo(logicalCenter + config.size * 0.5, logicalCenter);
  c.lineTo(logicalCenter + config.size, logicalCenter);
  c.moveTo(logicalCenter, logicalCenter - config.size);
  c.lineTo(logicalCenter, logicalCenter - config.size * 0.5);
  c.moveTo(logicalCenter, logicalCenter + config.size * 0.5);
  c.lineTo(logicalCenter, logicalCenter + config.size);
  c.stroke();
};

// 绘制圆形准星
const drawCircle = (c: UniApp.CanvasContext, config: SightConfig) => {
  c.beginPath();
  c.setLineDash([config.size * 0.5, config.size * 0.3], 0);
  c.arc(logicalCenter, logicalCenter, config.size, 0, Math.PI * 2);
  c.stroke();
  c.setLineDash([], 0);
};

// 绘制方括号准星
const drawBracket = (c: UniApp.CanvasContext, config: SightConfig) => {
  const offset = config.size * 0.6;

  c.beginPath();
  // 左上
  c.moveTo(logicalCenter - config.size, logicalCenter - offset);
  c.lineTo(logicalCenter - config.size, logicalCenter - config.size);
  c.lineTo(logicalCenter - offset, logicalCenter - config.size);
  // 右上
  c.moveTo(logicalCenter + offset, logicalCenter - config.size);
  c.lineTo(logicalCenter + config.size, logicalCenter - config.size);
  c.lineTo(logicalCenter + config.size, logicalCenter - offset);
  // 左下
  c.moveTo(logicalCenter - config.size, logicalCenter + offset);
  c.lineTo(logicalCenter - config.size, logicalCenter + config.size);
  c.lineTo(logicalCenter - offset, logicalCenter + config.size);
  // 右下
  c.moveTo(logicalCenter + offset, logicalCenter + config.size);
  c.lineTo(logicalCenter + config.size, logicalCenter + config.size);
  c.lineTo(logicalCenter + config.size, logicalCenter + offset);
  c.stroke();
};

// 绘制V形准星
const drawChevron = (c: UniApp.CanvasContext, config: SightConfig) => {
  c.beginPath();
  // V形
  c.moveTo(logicalCenter - config.size * 0.6, logicalCenter - config.size * 0.3);
  c.lineTo(logicalCenter, logicalCenter + config.size * 0.5);
  c.lineTo(logicalCenter + config.size * 0.6, logicalCenter - config.size * 0.3);
  // 上方短线
  c.moveTo(logicalCenter, logicalCenter - config.size);
  c.lineTo(logicalCenter, logicalCenter - config.size * 0.3);
  c.stroke();
};

// 绘制象限准星
const drawQuadrant = (c: UniApp.CanvasContext, config: SightConfig) => {
  // 象限准星：从中心向四个方向延伸到屏幕边缘
  c.beginPath();
  // 右侧水平线 - 从中心延伸到右边缘
  c.moveTo(logicalCenter, logicalCenter);
  c.lineTo(logicalCanvasSize, logicalCenter);
  // 上方垂直线 - 从中心延伸到顶部边缘
  c.moveTo(logicalCenter, logicalCenter);
  c.lineTo(logicalCenter, 0);
  // 左侧水平线 - 从中心延伸到左边缘
  c.moveTo(logicalCenter, logicalCenter);
  c.lineTo(0, logicalCenter);
  // 下方垂直线 - 从中心延伸到底部边缘
  c.moveTo(logicalCenter, logicalCenter);
  c.lineTo(logicalCenter, logicalCanvasSize);
  c.stroke();
};

// Watch for changes to the fixed prop, as it might change the parent's size,
// and thus the canvas's real rendered size.
watch(() => props.fixed, () => {
  measureCanvasDimensions();
});

// 监听配置变化
watch(() => props.config, () => {
  if (!isH5.value) {
    nextTick(() => {
      drawSight();
    });
  }
}, { deep: true });

watch(isVisible, () => {
  if (!isH5.value) {
    nextTick(() => {
      drawSight();
    });
  }
});
</script>

<style scoped>
.sight-container {
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sight-container.fixed {
  position: fixed;
  top: 50%;
  left: 50%;
  width: 200px;
  height: 200px;
  transform: translate(-50%, -50%);
  z-index: 9999;
}

.sight-container.relative {
  position: relative;
  width: 100%;
  height: 100%;
}

.sight-canvas,
.sight-svg {
  display: block;
  pointer-events: none;
}
</style>
