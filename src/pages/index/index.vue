<template>
  <view class="container">
    <!-- 准星层 - 仅非 App 平台显示 -->
    <!-- #ifndef APP-PLUS -->
    <SightRenderer
      v-if="settings.visible"
      :config="settings.config"
      :visible="settings.visible"
    />
    <!-- #endif -->

    <!-- 主界面 -->
    <view class="main-content" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header">
        <image class="logo" src="/static/logo.png" />
        <text class="title">准星助手</text>
        <text class="subtitle">Front Sight</text>
      </view>

      <!-- 悬浮窗权限提示 -->
      <!-- #ifdef APP-PLUS -->
      <view v-if="!hasPermission" class="permission-banner">
        <text class="permission-text">需要悬浮窗权限才能在其他应用上层显示</text>
        <view class="permission-btn" @tap="requestPermission">
          <text class="permission-btn-text">去授权</text>
        </view>
      </view>
      <!-- #endif -->

      <!-- 悬浮窗模式开关 (App 平台) -->
      <!-- #ifdef APP-PLUS -->
      <view v-if="hasPermission" class="overlay-mode-card">
        <view class="overlay-mode-header">
          <text class="overlay-mode-title">悬浮窗模式</text>
          <switch
            :checked="useOverlay"
            @change="toggleOverlayMode"
            color="#667eea"
          />
        </view>
        <text class="overlay-mode-desc">
          开启后准星将显示在所有应用上层
        </text>
      </view>
      <!-- #endif -->

      <view class="quick-actions">
        <view class="action-btn primary" @tap="toggleSight">
          <text class="action-icon">{{ settings.visible ? '🔒' : '🎯' }}</text>
          <text class="action-text">{{ settings.visible ? '隐藏准星' : '显示准星' }}</text>
        </view>

        <view class="action-btn secondary" @tap="goToSettings">
          <text class="action-icon">⚙️</text>
          <text class="action-text">准星设置</text>
        </view>
      </view>

      <view class="info-section">
        <view class="info-card">
          <text class="info-label">当前样式</text>
          <text class="info-value">{{ getSightTypeName(settings.config.type) }}</text>
        </view>

        <view class="info-card">
          <text class="info-label">颜色</text>
          <view class="color-preview" :style="{ backgroundColor: settings.config.color }" />
        </view>

        <view class="info-card">
          <text class="info-label">大小</text>
          <text class="info-value">{{ settings.config.size }}px</text>
        </view>

        <!-- #ifdef APP-PLUS -->
        <view class="info-card">
          <text class="info-label">显示模式</text>
          <text class="info-value">{{ useOverlay ? '悬浮窗' : '应用内' }}</text>
        </view>
        <!-- #endif -->
      </view>

      <view class="tips">
        <text class="tips-text">
          <!-- #ifdef APP-PLUS -->
          💡 提示：开启悬浮窗模式后，准星将显示在所有应用上层，可用于游戏辅助
          <!-- #endif -->
          <!-- #ifndef APP-PLUS -->
          💡 提示：点击"准星设置"可更换多种准星样式
          <!-- #endif -->
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import SightRenderer from '../../components/SightRenderer.vue';
import type { SightConfig } from '../../types/sight';
import { DEFAULT_SIGHT_CONFIG } from '../../types/sight';
// #ifdef APP-PLUS
import { sightOverlayManager } from '../../utils/sightOverlay';
// #endif

interface Settings {
  visible: boolean;
  config: SightConfig;
}

const statusBarHeight = ref(0);
const settings = ref<Settings>({
  visible: true,
  config: { ...DEFAULT_SIGHT_CONFIG },
});

// 悬浮窗相关变量（所有平台都需要声明）
const useOverlay = ref(false);
// #ifdef APP-PLUS
const hasPermission = ref(false);
// #endif

const sightTypeNames: Record<string, string> = {
  cross: '十字准星',
  dot: '单点准星',
  tactical: '战术准星',
  circle: '圆形准星',
  bracket: '方括号准星',
  chevron: 'V形准星',
  custom: '自定义',
};

onMounted(async () => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  loadSettings();

  // #ifdef APP-PLUS
  // 初始化悬浮窗
  await sightOverlayManager.init();

  // 检查权限
  checkOverlayPermission();

  // 加载悬浮窗模式设置
  const savedOverlayMode = uni.getStorageSync('use_overlay_mode');
  if (savedOverlayMode !== undefined) {
    useOverlay.value = savedOverlayMode;
    if (savedOverlayMode && settings.value.visible) {
      showSystemOverlay();
    }
  }
  // #endif
});

onUnmounted(() => {
  // #ifdef APP-PLUS
  // 释放悬浮窗资源
  if (useOverlay.value) {
    sightOverlayManager.release();
  }
  // #endif
});

const loadSettings = () => {
  try {
    const saved = uni.getStorageSync('sight_settings');
    if (saved) {
      settings.value = {
        visible: saved.visible ?? true,
        config: { ...DEFAULT_SIGHT_CONFIG, ...saved.config },
      };
    }
  } catch (e) {
    console.error('加载设置失败:', e);
  }
};

const saveSettings = () => {
  try {
    uni.setStorageSync('sight_settings', {
      config: settings.value.config,
      visible: settings.value.visible,
    });
  } catch (e) {
    console.error('保存设置失败:', e);
  }
};

// #ifdef APP-PLUS
const checkOverlayPermission = () => {
  hasPermission.value = sightOverlayManager.checkPermission();
};

const requestPermission = () => {
  sightOverlayManager.requestPermission();
  // 延迟检查，因为用户需要去设置页面授权
  setTimeout(() => {
    checkOverlayPermission();
  }, 1000);
};

const toggleOverlayMode = (e: any) => {
  useOverlay.value = e.detail.value;

  // 保存设置
  uni.setStorageSync('use_overlay_mode', useOverlay.value);

  if (useOverlay.value) {
    // 切换到悬浮窗模式
    if (settings.value.visible) {
      showSystemOverlay();
    }
  } else {
    // 切换回应用内模式
    sightOverlayManager.hide();
  }
};

const showSystemOverlay = () => {
  if (!hasPermission.value) {
    uni.showToast({
      title: '请先授权悬浮窗权限',
      icon: 'none',
    });
    return;
  }
  sightOverlayManager.show(settings.value.config);
};

const hideSystemOverlay = () => {
  sightOverlayManager.hide();
};

const updateSystemOverlay = () => {
  if (useOverlay.value && settings.value.visible) {
    sightOverlayManager.update(settings.value.config);
  }
};
// #endif

const toggleSight = () => {
  settings.value.visible = !settings.value.visible;
  saveSettings();

  // #ifdef APP-PLUS
  if (useOverlay.value) {
    if (settings.value.visible) {
      showSystemOverlay();
    } else {
      hideSystemOverlay();
    }
  }
  // #endif
};

const goToSettings = () => {
  uni.navigateTo({ url: '/pages/settings/settings' });
};

const getSightTypeName = (type: string): string => {
  return sightTypeNames[type] || '未知';
};

// 监听设置更新（从设置页面返回时）
// #ifdef APP-PLUS
uni.$on('sightConfigChanged', (config: SightConfig) => {
  settings.value.config = config;
  updateSystemOverlay();
});
// #endif
</script>

<style scoped>
.container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.main-content {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  box-sizing: border-box;
}

.header {
  padding: 60rpx 40rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.subtitle {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}

/* 权限横幅 */
.permission-banner {
  margin: 0 40rpx 20rpx;
  padding: 24rpx;
  background: rgba(255, 152, 0, 0.15);
  border: 1rpx solid rgba(255, 152, 0, 0.3);
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.permission-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
}

.permission-btn {
  padding: 12rpx 32rpx;
  background: rgba(255, 152, 0, 0.3);
  border-radius: 20rpx;
}

.permission-btn-text {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
}

/* 悬浮窗模式卡片 */
.overlay-mode-card {
  margin: 0 40rpx 20rpx;
  padding: 24rpx;
  background: rgba(102, 126, 234, 0.15);
  border: 1rpx solid rgba(102, 126, 234, 0.3);
  border-radius: 12rpx;
}

.overlay-mode-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.overlay-mode-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

.overlay-mode-desc {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.6);
}

.quick-actions {
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx;
  border-radius: 16rpx;
  gap: 20rpx;
  transition: all 0.3s ease;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.4);
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.1);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.action-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.action-icon {
  font-size: 40rpx;
}

.action-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.info-section {
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.info-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 30rpx;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12rpx;
  backdrop-filter: blur(10px);
}

.info-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
}

.info-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

.color-preview {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
}

.tips {
  margin-top: auto;
  padding: 30rpx 40rpx;
}

.tips-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
  line-height: 1.6;
}
</style>
