# Java 原生模块说明

## 概述

本目录包含 Front Sight（准星助手）的 Android 原生 Java 代码，用于实现系统级悬浮窗功能。

## 目录结构

```
java/
└── com/
    └── sight/
        └── front/
            └── sight_overlay.java    # 悬浮窗核心实现
```

## 核心模块

### `sight_overlay.java`

Android 系统级悬浮窗实现，提供准星在所有应用上方显示的能力。

#### 架构设计

```
┌──────────────────────────────────────┐
│    sight_overlay (管理器)             │
│  - 单例模式                          │
│  - 权限管理                          │
│  - 窗口生命周期管理                   │
└────────────────┬─────────────────────┘
                 │
┌────────────────▼─────────────────────┐
│    OverlayView (自定义视图)           │
│  - Canvas 绘制                       │
│  - 准星图形渲染                      │
│  - 配置实时更新                      │
└──────────────────────────────────────┘
                 │
┌────────────────▼─────────────────────┐
│  SightOverlayConfig (配置类)          │
│  - 颜色、大小、粗细                  │
│  - 透明度、中心点开关                 │
└──────────────────────────────────────┘
```

#### 主要类

##### `SightOverlayConfig`

悬浮窗配置数据类。

**属性:**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `type` | String | `"cross"` | 准星类型 |
| `color` | String | `"#00FF00"` | 准星颜色（十六进制） |
| `size` | double | `20.0` | 准星大小（dp） |
| `thickness` | double | `2.0` | 线条粗细（dp） |
| `showDot` | boolean | `true` | 是否显示中心点 |
| `opacity` | double | `1.0` | 透明度（0-1） |

**支持的准星类型:**

| 类型值 | 名称 | 说明 |
|--------|------|------|
| `cross` | 十字准星 | 四方向线条，中间留空隙 |
| `dot` | 单点准星 | 单个中心圆点 |
| `tactical` | 战术准星 | 内圆 + 外围短线 |
| `circle` | 圆形准星 | 虚线圆环 |
| `bracket` | 方括号准星 | 四角方括号 |
| `chevron` | V形准星 | 倒 V 形 + 上方短线 |

---

##### `OverlayView`

自定义 View，负责准星的绘制。

**核心方法:**

- `onDraw(Canvas)`: 根据配置类型绘制对应准星
- `setConfig(SightOverlayConfig)`: 更新配置并触发重绘

**绘制方法:**

| 方法 | 说明 |
|------|------|
| `drawCross()` | 绘制十字准星 |
| `drawDot()` | 绘制单点准星 |
| `drawTactical()` | 绘制战术准星 |
| `drawCircle()` | 绘制圆形虚线准星 |
| `drawBracket()` | 绘制方括号准星 |
| `drawChevron()` | 绘制 V 形准星 |

---

##### `sight_overlay`

悬浮窗管理器，采用单例模式。

**公开方法:**

| 方法 | 说明 |
|------|------|
| `init(Context)` | 初始化管理器，获取系统服务 |
| `show(...)` | 显示悬浮窗 |
| `hide()` | 隐藏悬浮窗 |
| `update(...)` | 更新悬浮窗配置 |
| `checkPermission()` | 检查悬浮窗权限 |
| `requestPermission()` | 跳转系统设置请求权限 |
| `release()` | 释放资源 |

## 技术要点

### 1. WindowManager 悬浮窗

```java
// Android 8.0+ 使用新类型
WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

// Android 8.0 以下使用旧类型
WindowManager.LayoutParams.TYPE_PHONE
```

### 2. 布局参数配置

```java
params.flags = FLAG_NOT_FOCUSABLE |    // 不获取焦点
               FLAG_NOT_TOUCHABLE |     // 不拦截触摸事件
               FLAG_LAYOUT_IN_SCREEN;   // 全屏布局

params.format = PixelFormat.TRANSLUCENT; // 透明背景
params.gravity = Gravity.CENTER;         // 屏幕居中
```

### 3. 权限处理

- **Android 6.0+**: 需要 `Settings.canDrawOverlays()` 检查
- **权限请求**: 跳转到 `ACTION_MANAGE_OVERLAY_PERMISSION` 系统设置页
- **备用方案**: 失败时跳转到应用详情设置页

### 4. 单例模式

双重检查锁定（DCL）实现线程安全单例：

```java
if (instance == null) {
    synchronized (sight_overlay.class) {
        if (instance == null) {
            instance = new sight_overlay();
        }
    }
}
```

## 使用示例

### 基本用法

```java
// 1. 初始化
sight_overlay.init(context);

// 2. 检查权限
if (!sight_overlay.checkPermission()) {
    sight_overlay.requestPermission();
    return;
}

// 3. 显示悬浮窗（包含 type 参数）
sight_overlay.show("cross", "#FF0000", 30.0, 3.0, true, 0.8);

// 4. 更新配置（切换准星类型）
sight_overlay.update("tactical", "#00FF00", 25.0, 2.0, false, 1.0);

// 5. 隐藏悬浮窗
sight_overlay.hide();

// 6. 释放资源
sight_overlay.release();
```

### 不同准星类型示例

```java
// 战术准星
sight_overlay.show("tactical", "#FF0000", 30.0, 3.0, true, 0.8);

// 圆形准星
sight_overlay.show("circle", "#00FFFF", 25.0, 2.0, true, 0.9);

// 方括号准星
sight_overlay.show("bracket", "#FFFF00", 28.0, 2.5, false, 1.0);

// V形准星
sight_overlay.show("chevron", "#FF00FF", 32.0, 2.0, true, 0.85);

// 单点准星
sight_overlay.show("dot", "#FFFFFF", 20.0, 1.0, false, 1.0);
```

## 与前端渲染的同步

Java 悬浮窗实现需要与前端 `SightRenderer.vue` 保持准星渲染逻辑一致。

| 文件 | 职责 | 准星类型支持 |
|------|------|--------------|
| [SightRenderer.vue](../src/components/SightRenderer.vue) | H5/小程序/App 内渲染 | ✅ 全部 6 种 |
| [sight_overlay.java](./com/sight/front/sight_overlay.java) | Android 悬浮窗渲染 | ✅ 全部 6 种 |
| [types/sight.ts](../src/types/sight.ts) | 类型定义与预设 | ✅ 全部 6 种 |

**添加新准星类型时需要同步的位置:**

1. **类型定义**: [src/types/sight.ts](../src/types/sight.ts) 中的 `SightType`
2. **前端绘制**: [SightRenderer.vue](../src/components/SightRenderer.vue) 中的 `draw*()` 方法和 SVG 模板
3. **悬浮窗绘制**: [sight_overlay.java](java/com/sight/front/sight_overlay.java) 中的 `draw*()` 方法
4. **预设配置**: [types/sight.ts](../src/types/sight.ts) 中的 `PRESET_SIGHTS`

## 权限配置

在 `AndroidManifest.xml` 中添加：

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

## 调试

查看日志:

```bash
adb logcat | grep SightOverlay
```

常见日志标签:
- `SightOverlay`: 主要操作日志
- 错误级别: 权限缺失、初始化失败等

## 常见问题

**Q: 悬浮窗不显示?**

A: 检查以下几点：
1. 是否授予悬浮窗权限
2. 是否调用 `init()` 初始化
3. 是否有异常日志输出

**Q: 闪退或 SecurityException?**

A: 确保在 `AndroidManifest.xml` 中声明了 `SYSTEM_ALERT_WINDOW` 权限。

**Q: 准星位置偏移?**

A: 检查 `params.gravity = Gravity.CENTER` 设置，确保布局居中。

## 版本兼容性

| Android 版本 | 最低支持 | 特殊处理 |
|--------------|----------|----------|
| Android 8.0+ (API 26+) | ✅ | 使用 `TYPE_APPLICATION_OVERLAY` |
| Android 6.0-7.1 (API 23-25) | ✅ | 使用 `TYPE_PHONE` + 运行时权限 |
| Android 5.x 及以下 | ⚠️ | 仅使用旧权限模型 |
