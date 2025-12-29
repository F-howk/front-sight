<template>
  <view class="settings-container" :style="{ paddingTop: statusBarHeight + 'px' }">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="nav-title">准星设置</text>
      <view class="nav-placeholder" />
    </view>

    <!-- 主预览区域 -->
    <view class="hero-preview">
      <view class="preview-wrapper">
        <view class="sight-display">
          <view class="target-bg">
            <view class="target-ring" v-for="i in 4" :key="i" />
            <view class="target-cross-h" />
            <view class="target-cross-v" />
            <view class="target-grid">
              <view class="grid-line" v-for="i in 9" :key="i" />
            </view>
          </view>
          <SightRenderer
            :config="currentConfig"
            :visible="sightVisible"
            :fixed="false"
          />
        </view>

        <!-- 快捷操作栏 -->
        <view class="quick-actions">
          <view
            class="action-btn"
            :class="{ active: sightVisible }"
            @tap="toggleSight"
          >
            <text class="action-icon">{{ sightVisible ? "👁️" : "🚫" }}</text>
            <text class="action-label">{{
              sightVisible ? "显示中" : "已隐藏"
            }}</text>
          </view>
          <view class="action-divider" />
          <view class="action-btn" @tap="resetToDefault">
            <text class="action-icon">🔄</text>
            <text class="action-label">重置</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 设置内容滚动区 -->
    <scroll-view class="scroll-content" scroll-y>
      <!-- 准星样式选择 -->
      <view class="card">
        <view class="card-header">
          <text class="card-title">🔘 准星样式</text>
          <text class="card-subtitle">选择预设样式</text>
        </view>
        <view class="sight-grid">
          <view
            v-for="preset in PRESET_SIGHTS"
            :key="preset.id"
            class="sight-item"
            :class="{ active: isActivePreset(preset) }"
            @tap="selectPreset(preset)"
          >
            <view class="sight-preview-box">
              <view class="preview-bg">
                <SightRenderer
                  :config="previewConfig(preset.config)"
                  :visible="true"
                  :fixed="false"
                />
              </view>
            </view>
            <text class="sight-label">{{ preset.name }}</text>
            <view v-if="isActivePreset(preset)" class="check-mark">✓</view>
          </view>
        </view>
      </view>

      <!-- 颜色选择 -->
      <view class="card">
        <view class="card-header">
          <text class="card-title">🎨 准星颜色</text>
          <view
            class="current-color-preview"
            :style="{ backgroundColor: currentConfig.color }"
          />
        </view>
        <view class="color-grid">
          <view
            v-for="color in COLORS"
            :key="color"
            class="color-item"
            :class="{ active: currentConfig.color === color }"
            :style="{ backgroundColor: color }"
            @tap="setColor(color)"
          >
            <view v-if="currentConfig.color === color" class="color-check"
              >✓</view
            >
          </view>
        </view>
      </view>

      <!-- 参数调节 -->
      <view class="card">
        <view class="card-header">
          <text class="card-title">⚙️ 参数调节</text>
          <text class="card-subtitle">自定义调整</text>
        </view>

        <!-- 大小 -->
        <view class="slider-row">
          <view class="slider-header">
            <text class="slider-label">大小</text>
            <view class="slider-badge"
              >{{ currentConfig.size }}<text class="badge-unit">px</text></view
            >
          </view>
          <slider
            :value="currentConfig.size"
            :min="5"
            :max="60"
            @change="setSightSize"
            active-color="#007AFF"
            block-size="24"
            class="custom-slider"
          />
        </view>

        <!-- 粗细 -->
        <view class="slider-row">
          <view class="slider-header">
            <text class="slider-label">粗细</text>
            <view class="slider-badge"
              >{{ currentConfig.thickness
              }}<text class="badge-unit">px</text></view
            >
          </view>
          <slider
            :value="currentConfig.thickness"
            :min="1"
            :max="5"
            @change="setSightThickness"
            active-color="#007AFF"
            block-size="24"
            class="custom-slider"
          />
        </view>

        <!-- 透明度 -->
        <view class="slider-row">
          <view class="slider-header">
            <text class="slider-label">透明度</text>
            <view class="slider-badge"
              >{{ Math.round(currentConfig.opacity * 100)
              }}<text class="badge-unit">%</text></view
            >
          </view>
          <slider
            :value="currentConfig.opacity * 100"
            :min="20"
            :max="100"
            @change="setSightOpacity"
            active-color="#007AFF"
            block-size="24"
            class="custom-slider"
          />
        </view>

        <!-- 中心点开关 -->
        <view class="switch-row">
          <text class="switch-label">显示中心点</text>
          <switch
            :checked="currentConfig.showDot"
            @change="toggleDot"
            color="#007AFF"
          />
        </view>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-spacer" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import SightRenderer from "../../components/SightRenderer.vue";
import type { SightConfig } from "../../types/sight";
import { PRESET_SIGHTS, DEFAULT_SIGHT_CONFIG } from "../../types/sight";
// #ifdef APP-PLUS
import { sightOverlayManager } from "../../utils/sightOverlay";
// #endif

const statusBarHeight = ref(0);
const COLORS = [
  "#FF0000", // 红
  "#00FF00", // 绿
  "#00FFFF", // 青
  "#FFFF00", // 黄
  "#FF00FF", // 紫
  "#FFFFFF", // 白
  "#FFA500", // 橙
  "#0080FF", // 蓝
];

const sightVisible = ref(true);
const currentConfig = ref<SightConfig>({ ...DEFAULT_SIGHT_CONFIG });
const currentPresetId = ref<string | null>(null);

// #ifdef APP-PLUS
const useOverlay = ref(false);
const hasPermission = ref(false);
// #endif

// uni-app switch 事件类型
interface SwitchChangeEvent {
  detail: {
    value: boolean;
  };
}

// 从本地存储加载配置
onMounted(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  loadSettings();

  // #ifdef APP-PLUS
  // 初始化悬浮窗管理器
  sightOverlayManager.init();
  // 加载悬浮窗模式状态
  useOverlay.value = uni.getStorageSync('use_overlay_mode') || false;
  hasPermission.value = sightOverlayManager.checkPermission();
  // #endif
});

const loadSettings = () => {
  try {
    const saved = uni.getStorageSync("sight_settings");
    if (saved) {
      currentConfig.value = { ...DEFAULT_SIGHT_CONFIG, ...saved.config };
      sightVisible.value = saved.visible ?? true;
      currentPresetId.value = saved.presetId ?? null;
    }
  } catch (e) {
    console.error("加载设置失败:", e);
  }
};

const saveSettings = () => {
  try {
    uni.setStorageSync("sight_settings", {
      config: currentConfig.value,
      visible: sightVisible.value,
      presetId: currentPresetId.value,
    });

    // #ifdef APP-PLUS
    // 同步更新悬浮窗配置
    const useOverlay = uni.getStorageSync("use_overlay_mode");
    if (useOverlay && sightVisible.value) {
      sightOverlayManager.update(currentConfig.value);
    }
    // #endif
  } catch (e) {
    console.error("保存设置失败:", e);
  }
};

const goBack = () => {
  // #ifdef APP-PLUS
  // 通知首页配置已更新
  uni.$emit("sightConfigChanged", currentConfig.value);
  // #endif
  uni.navigateBack();
};

const toggleSight = () => {
  sightVisible.value = !sightVisible.value;
  saveSettings();

  // #ifdef APP-PLUS
  // 如果使用悬浮窗模式，同步控制系统悬浮窗
  if (useOverlay.value) {
    if (sightVisible.value) {
      if (hasPermission.value) {
        sightOverlayManager.show(currentConfig.value);
      } else {
        uni.showToast({
          title: '请先授权悬浮窗权限',
          icon: 'none',
        });
      }
    } else {
      sightOverlayManager.hide();
    }
  }
  // #endif
};

const resetToDefault = () => {
  currentConfig.value = { ...DEFAULT_SIGHT_CONFIG };
  currentPresetId.value = PRESET_SIGHTS[0].id;
  sightVisible.value = true;
  saveSettings();
};

const isActivePreset = (preset: {
  id: string;
  name: string;
  config: SightConfig;
}) => {
  return currentPresetId.value === preset.id;
};

const selectPreset = (preset: {
  id: string;
  name: string;
  config: SightConfig;
}) => {
  currentConfig.value = { ...preset.config };
  currentPresetId.value = preset.id;
  saveSettings();
};

const setColor = (color: string) => {
  currentConfig.value.color = color;
  currentPresetId.value = null;
  saveSettings();
};

const setSightSize = (e: { detail: { value: number } }) => {
  currentConfig.value.size = Math.round(e.detail.value);
  currentPresetId.value = null;
  saveSettings();
};

const setSightThickness = (e: { detail: { value: number } }) => {
  currentConfig.value.thickness = Math.round(e.detail.value);
  currentPresetId.value = null;
  saveSettings();
};

const setSightOpacity = (e: { detail: { value: number } }) => {
  currentConfig.value.opacity = e.detail.value / 100;
  currentPresetId.value = null;
  saveSettings();
};

const toggleDot = (e: SwitchChangeEvent | Event) => {
  const value =
    "detail" in e
      ? (e as SwitchChangeEvent).detail.value
      : (e as any).detail.value;
  currentConfig.value.showDot = value;
  currentPresetId.value = null;
  saveSettings();
};

const previewConfig = (config: SightConfig): SightConfig => {
  return {
    ...config,
    size: Math.min(config.size, 20),
    opacity: 1,
  };
};
</script>

<style scoped>
.settings-container {
  height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  box-sizing: border-box;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 56rpx;
  color: #fff;
  font-weight: 300;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.nav-placeholder {
  width: 64rpx;
}

/* 主预览区域 */
.hero-preview {
  padding: 20rpx;
  background: linear-gradient(
    180deg,
    rgba(102, 126, 234, 0.15) 0%,
    transparent 100%
  );
  padding-top: 10rpx;
}

.preview-wrapper {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(20px);
  border-radius: 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.sight-display {
  width: 100%;
  aspect-ratio: 1;
  max-height: 260rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.target-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.03) 0%,
    transparent 60%
  );
}

.target-ring {
  position: absolute;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.target-ring:nth-child(1) {
  width: 100%;
  height: 100%;
}
.target-ring:nth-child(2) {
  width: 75%;
  height: 75%;
}
.target-ring:nth-child(3) {
  width: 50%;
  height: 50%;
}
.target-ring:nth-child(4) {
  width: 25%;
  height: 25%;
}

.target-cross-h,
.target-cross-v {
  position: absolute;
  background: rgba(255, 255, 255, 0.08);
}

.target-cross-h {
  width: 100%;
  height: 1rpx;
}
.target-cross-v {
  width: 1rpx;
  height: 100%;
}

.target-grid {
  position: absolute;
  inset: 20rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-content: space-between;
}

.grid-line {
  width: 1rpx;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
}

/* 快捷操作栏 */
.quick-actions {
  display: flex;
  align-items: center;
  padding: 10rpx;
  background: rgba(255, 255, 255, 0.03);
  border-top: 1rpx solid rgba(255, 255, 255, 0.05);
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  /* padding: 16rpx; */
  border-radius: 16rpx;
  transition: all 0.2s ease;
}

.action-btn:active {
  background: rgba(255, 255, 255, 0.08);
}

.action-btn.active .action-icon {
  filter: drop-shadow(0 0 12rpx rgba(0, 122, 255, 0.8));
}

.action-icon {
  font-size: 40rpx;
  transition: all 0.3s ease;
}

.action-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.6);
}

.action-btn.active .action-label {
  color: #007aff;
}

.action-divider {
  width: 1rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.1);
  margin: 0 8rpx;
}

/* 滚动内容区 */
.scroll-content {
  height: calc(100% - 106rpx - 414rpx);
}

.scroll-content::-webkit-scrollbar {
  display: none;
}

.scroll-content {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 卡片 */
.card {
  margin: 20rpx;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  border-radius: 20rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
}

.card-header {
  padding: 20rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
}

.card-subtitle {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.4);
}

/* 准星网格 */
.sight-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 12rpx;
  gap: 10rpx;
}

.sight-item {
  width: calc(25% - 8rpx);
  position: relative;
  border-radius: 14rpx;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.3);
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.sight-item :deep(.sight-svg){
  width: 100% !important;
  height: 100% !important;
}

.sight-item.active {
  border-color: #007aff;
  background: rgba(0, 122, 255, 0.15);
  transform: scale(1.05);
}

.sight-preview-box {
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.preview-bg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.08) 0%,
    transparent 70%
  );
}

.sight-label {
  display: block;
  text-align: center;
  padding: 10rpx 6rpx;
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.6);
  background: rgba(0, 0, 0, 0.3);
}

.check-mark {
  position: absolute;
  top: 6rpx;
  right: 6rpx;
  width: 28rpx;
  height: 28rpx;
  background: #007aff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: #fff;
}

/* 颜色网格 */
.color-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx;
  gap: 14rpx;
}

.color-item {
  width: 68rpx;
  height: 68rpx;
  border-radius: 14rpx;
  position: relative;
  border: 2rpx solid rgba(255, 255, 255, 0.15);
  transition: all 0.2s ease;
}

.color-item.active {
  border-color: #fff;
  transform: scale(1.1);
  box-shadow: 0 0 20rpx rgba(255, 255, 255, 0.3);
}

.color-check {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #fff;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

.current-color-preview {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.25);
}

/* 滑块行 */
.slider-row {
  padding: 24rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.04);
}

.slider-row:last-of-type {
  border-bottom: none;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.slider-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.slider-badge {
  font-size: 24rpx;
  font-weight: 600;
  color: #007aff;
  background: rgba(0, 122, 255, 0.15);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.badge-unit {
  font-size: 18rpx;
  margin-left: 2rpx;
}

.custom-slider {
  /* width: 100%; */
}

/* 开关行 */
.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.04);
}

.switch-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
}

/* 底部间距 */
.bottom-spacer {
  height: 40rpx;
}
</style>
