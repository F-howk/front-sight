/**
 * 悬浮窗工具类 - JS 桥接模式调用原生 Android 代码
 */

import type { SightConfig } from '../types/sight';

// #ifdef APP-PLUS
/**
 * 原生模块桥接类
 */
class SightOverlayNative {
  private moduleClass: any = null;

  constructor() {
    try {
      // 导入原生模块类
      // @ts-ignore
      this.moduleClass = plus.android.importClass('com.sight.front.sight_overlay');
      console.log('SightOverlayModule 类加载成功:', this.moduleClass);
    } catch (e) {
      console.error('加载 SightOverlayModule 失败:', e);
    }
  }

  /**
   * 检查模块是否可用
   */
  isAvailable(): boolean {
    return this.moduleClass !== null;
  }

  /**
   * 初始化
   */
  init(context: any): void {
    if (this.moduleClass) {
      // 调用静态方法 init
      this.moduleClass.init(context);
    }
  }

  /**
   * 显示悬浮窗
   */
  show(type: string, color: string, size: number, thickness: number, showDot: boolean, opacity: number): void {
    if (this.moduleClass) {
      // 调用静态方法 show
      this.moduleClass.show(type, color, size, thickness, showDot, opacity);
    }
  }

  /**
   * 隐藏悬浮窗
   */
  hide(): void {
    if (this.moduleClass) {
      // 调用静态方法 hide
      this.moduleClass.hide();
    }
  }

  /**
   * 更新悬浮窗
   */
  update(type: string, color: string, size: number, thickness: number, showDot: boolean, opacity: number): void {
    if (this.moduleClass) {
      // 调用静态方法 update
      this.moduleClass.update(type, color, size, thickness, showDot, opacity);
    }
  }

  /**
   * 检查权限
   */
  checkPermission(): boolean {
    if (this.moduleClass) {
      // 调用静态方法 checkPermission
      return this.moduleClass.checkPermission();
    }
    return false;
  }

  /**
   * 请求权限
   */
  requestPermission(): void {
    if (this.moduleClass) {
      // 调用静态方法 requestPermission
      this.moduleClass.requestPermission();
    }
  }

  /**
   * 释放资源
   */
  release(): void {
    if (this.moduleClass) {
      // 调用静态方法 release
      this.moduleClass.release();
    }
  }
}

// 创建原生模块实例
const sightOverlayNative = new SightOverlayNative();
// #endif

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
      // 获取 Android 上下文
      // @ts-ignore
      const context = plus.android.runtimeMainActivity();
      console.log('初始化悬浮窗，context:', context);

      if (!sightOverlayNative.isAvailable()) {
        console.error('原生模块未加载');
        return false;
      }

      sightOverlayNative.init(context);
      this.initialized = true;
      console.log('悬浮窗插件初始化成功');
      return true;
      // #endif

      // #ifndef APP-PLUS
      console.warn('悬浮窗插件仅支持 App 平台');
      return false;
      // #endif
    } catch (e) {
      // #ifndef APP-PLUS
      console.error('初始化悬浮窗失败:', e);
      // #endif
      return false;
    }
  }

  /**
   * 检查悬浮窗权限
   */
  checkPermission(): boolean {
    // #ifdef APP-PLUS
    try {
      return sightOverlayNative.checkPermission();
    } catch (e) {
      console.error('检查权限失败:', e);
      return false;
    }
    // #endif

    // #ifndef APP-PLUS
    return false;
    // #endif
  }

  /**
   * 请求悬浮窗权限
   */
  requestPermission(): void {
    // #ifdef APP-PLUS
    try {
      sightOverlayNative.requestPermission();
    } catch (e) {
      console.error('请求权限失败:', e);
      // 备用方案：跳转到系统设置
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
      // @ts-ignore
      const Intent = plus.android.importClass('android.content.Intent');
      // @ts-ignore
      const intent = plus.android.newObject(Intent);

      // 跳转到应用设置
      // @ts-ignore
      intent.setAction('android.settings.APPLICATION_DETAILS_SETTINGS');

      // @ts-ignore
      const packageName = plus.android.invoke(
        plus.android.runtimeMainActivity(),
        'getPackageName'
      );

      // @ts-ignore
      const Uri = plus.android.importClass('android.net.Uri');
      // @ts-ignore
      intent.setData(Uri.parse('package:' + packageName));

      // @ts-ignore
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      // @ts-ignore
      plus.android.runtimeMainActivity().startActivity(intent);

      console.log('已跳转到应用设置页面');
    } catch (e) {
      console.error('原生权限请求失败:', e);
    }
  }
  // #endif

  /**
   * 显示悬浮窗
   */
  show(config: SightConfig): void {
    // #ifdef APP-PLUS
    try {
      this.config = config;
      console.log('显示悬浮窗:', config);
      sightOverlayNative.show(
        config.type,
        config.color,
        config.size,
        config.thickness,
        config.showDot,
        config.opacity
      );
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
      sightOverlayNative.hide();
      console.log('隐藏悬浮窗');
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
      this.config = config;
      console.log('更新悬浮窗配置:', config);
      sightOverlayNative.update(
        config.type,
        config.color,
        config.size,
        config.thickness,
        config.showDot,
        config.opacity
      );
    } catch (e) {
      console.error('更新悬浮窗失败:', e);
    }
    // #endif
  }

  /**
   * 释放资源
   */
  async release(): Promise<void> {
    // #ifdef APP-PLUS
    try {
      sightOverlayNative.release();
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
