/**
 * 悬浮窗工具类 - 封装 UTS 插件调用
 */

import type { SightConfig } from '../types/sight';

// 插件模块
// @ts-ignore - UTS 插件类型声明
let sightOverlayModule: any = null;

/**
 * 悬浮窗管理类
 */
export class SightOverlayManager {
  private initialized = false;
  private config: SightConfig | null = null;

  /**
   * 初始化悬浮窗
   */
  async init(): Promise<boolean> {
    try {
      // #ifdef APP-PLUS
      // 获取插件模块
      if (!sightOverlayModule) {
        sightOverlayModule = uni.requireNativePlugin('sight-overlay');
      }

      // @ts-ignore
      if (sightOverlayModule && sightOverlayModule.init) {
        // @ts-ignore
        const mainActivity = plus.android.runtimeMainActivity();
        // @ts-ignore
        sightOverlayModule.init(mainActivity);
        this.initialized = true;
        console.log('悬浮窗插件初始化成功');
        return true;
      }
      // #endif

      console.warn('悬浮窗插件仅支持 App 平台');
      return false;
    } catch (e) {
      console.error('初始化悬浮窗失败:', e);
      return false;
    }
  }

  /**
   * 检查悬浮窗权限
   */
  checkPermission(): boolean {
    // #ifdef APP-PLUS
    try {
      if (sightOverlayModule && sightOverlayModule.checkPermission) {
        // @ts-ignore
        const result = sightOverlayModule.checkPermission();
        console.log('检查悬浮窗权限:', result);
        return result === true || result === 'true';
      }
    } catch (e) {
      console.error('检查权限失败:', e);
    }
    // #endif
    return false;
  }

  /**
   * 请求悬浮窗权限
   */
  requestPermission(): void {
    // #ifdef APP-PLUS
    try {
      if (sightOverlayModule && sightOverlayModule.requestPermission) {
        console.log('请求悬浮窗权限...');
        // @ts-ignore
        sightOverlayModule.requestPermission();
      } else {
        // 如果插件方法不可用，使用原生方式跳转
        this.requestPermissionNative();
      }
    } catch (e) {
      console.error('请求权限失败:', e);
      this.requestPermissionNative();
    }
    // #endif
  }

  /**
   * 使用原生方式请求权限（备用方案）
   */
  // #ifdef APP-PLUS
  private requestPermissionNative(): void {
    try {
      const main = plus.android.runtimeMainActivity();
      const Context = plus.android.importClass('android.content.Context');

      // 使用 plus.android.invoke 方法调用
      // @ts-ignore
      const Intent = plus.android.importClass('android.content.Intent');
      // @ts-ignore
      const Settings = plus.android.importClass('android.provider.Settings');
      // @ts-ignore
      const Uri = plus.android.importClass('android.net.Uri');

      // @ts-ignore
      const packageName = main.getPackageName();

      // 创建 Intent
      // @ts-ignore
      const intent = plus.android.invoke(
        'android.content.Intent',
        'new',
        'android.settings.ACTION_MANAGE_OVERLAY_PERMISSION',
        null
      );

      // @ts-ignore
      plus.android.invoke(intent, 'setData', Uri.parse('package:' + packageName));
      // @ts-ignore
      plus.android.invoke(intent, 'addFlags', 0x10000000);
      // @ts-ignore
      main.startActivity(intent);

      console.log('已跳转到悬浮窗权限设置页面');
    } catch (e) {
      console.error('原生权限请求失败:', e);
      // 备用方案：提示用户手动去设置
      uni.showModal({
        title: '需要授权',
        content: '请前往: 设置 > 应用 > 准星助手 > 悬浮窗/在其他应用上层显示',
        showCancel: false
      });
    }
  }
  // #endif

  /**
   * 显示悬浮窗
   */
  show(config: SightConfig): void {
    // #ifdef APP-PLUS
    try {
      if (sightOverlayModule && sightOverlayModule.show) {
        this.config = config;
        const params = {
          type: config.type,
          color: config.color,
          size: config.size,
          thickness: config.thickness,
          showDot: config.showDot,
          opacity: config.opacity,
        };
        console.log('显示悬浮窗:', params);
        // @ts-ignore
        sightOverlayModule.show(params);
      } else {
        console.error('悬浮窗插件未正确加载');
      }
    } catch (e) {
      console.error('显示悬浮窗失败:', e);
    }
    // #endif
  }

  /**
   * 隐藏悬浮窗
   */
  hide(): void {
    // #ifdef APP-PLUS
    try {
      if (sightOverlayModule && sightOverlayModule.hide) {
        // @ts-ignore
        sightOverlayModule.hide();
        console.log('隐藏悬浮窗');
      }
    } catch (e) {
      console.error('隐藏悬浮窗失败:', e);
    }
    // #endif
  }

  /**
   * 更新悬浮窗配置
   */
  update(config: SightConfig): void {
    // #ifdef APP-PLUS
    try {
      if (sightOverlayModule && sightOverlayModule.update) {
        this.config = config;
        const params = {
          type: config.type,
          color: config.color,
          size: config.size,
          thickness: config.thickness,
          showDot: config.showDot,
          opacity: config.opacity,
        };
        console.log('更新悬浮窗配置:', params);
        // @ts-ignore
        sightOverlayModule.update(params);
      }
    } catch (e) {
      console.error('更新悬浮窗失败:', e);
    }
    // #endif
  }

  /**
   * 释放资源
   */
  release(): void {
    // #ifdef APP-PLUS
    try {
      if (sightOverlayModule && sightOverlayModule.release) {
        // @ts-ignore
        sightOverlayModule.release();
      }
    } catch (e) {
      console.error('释放资源失败:', e);
    }
    // #endif
    this.initialized = false;
    this.config = null;
  }

  /**
   * 获取当前配置
   */
  getCurrentConfig(): SightConfig | null {
    return this.config;
  }
}

// 导出单例
export const sightOverlayManager = new SightOverlayManager();
