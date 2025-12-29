package com.sight.front;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.DashPathEffect;
import android.graphics.Path;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 准星类型常量 - 与 TypeScript SightType 对齐
 */
class SightType {
    public static final String CROSS = "cross";        // 十字准星
    public static final String DOT = "dot";            // 单点准星
    public static final String TACTICAL = "tactical";  // 战术准星
    public static final String CIRCLE = "circle";      // 圆形准星
    public static final String BRACKET = "bracket";    // 方括号准星
    public static final String CHEVRON = "chevron";    // V形准星
    public static final String QUADRANT = "quadrant";  // 象限准星
    public static final String CUSTOM = "custom";      // 自定义准星

    /**
     * 获取所有支持的准星类型
     */
    public static String[] getSupportedTypes() {
        return new String[]{CROSS, DOT, TACTICAL, CIRCLE, BRACKET, CHEVRON, QUADRANT};
    }

    /**
     * 检查类型是否支持
     */
    public static boolean isSupported(String type) {
        for (String supported : getSupportedTypes()) {
            if (supported.equals(type)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * 默认配置常量 - 与 TypeScript DEFAULT_SIGHT_CONFIG 对齐
 */
class SightDefaults {
    public static final String DEFAULT_TYPE = SightType.CROSS;
    public static final String DEFAULT_COLOR = "#00FF00";
    public static final double DEFAULT_SIZE = 20.0;
    public static final double DEFAULT_THICKNESS = 1.0;
    public static final boolean DEFAULT_SHOW_DOT = true;
    public static final double DEFAULT_OPACITY = 0.8;

    /**
     * 创建默认配置
     */
    public static SightOverlayConfig createDefaultConfig() {
        return new SightOverlayConfig(
                DEFAULT_TYPE,
                DEFAULT_COLOR,
                DEFAULT_SIZE,
                DEFAULT_THICKNESS,
                DEFAULT_SHOW_DOT,
                DEFAULT_OPACITY
        );
    }

    /**
     * 获取默认配置的 JSON 表示
     */
    public static JSONObject getDefaultConfigJson() {
        try {
            JSONObject config = new JSONObject();
            config.put("type", DEFAULT_TYPE);
            config.put("color", DEFAULT_COLOR);
            config.put("size", DEFAULT_SIZE);
            config.put("thickness", DEFAULT_THICKNESS);
            config.put("showDot", DEFAULT_SHOW_DOT);
            config.put("opacity", DEFAULT_OPACITY);
            return config;
        } catch (JSONException e) {
            Log.e("SightOverlay", "创建默认配置JSON失败: " + e.getMessage());
            return new JSONObject();
        }
    }
}

/**
 * 悬浮窗配置类
 */
class SightOverlayConfig {
    public String type = "cross";
    public String color = "#00FF00";
    public double size = 20.0;
    public double thickness = 2.0;
    public boolean showDot = true;
    public double opacity = 1.0;

    public SightOverlayConfig() {
    }

    public SightOverlayConfig(String type, String color, double size, double thickness, boolean showDot, double opacity) {
        this.type = type;
        this.color = color;
        this.size = size;
        this.thickness = thickness;
        this.showDot = showDot;
        this.opacity = opacity;
    }

    /**
     * 转换为 JSON 对象
     */
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("color", color);
            json.put("size", size);
            json.put("thickness", thickness);
            json.put("showDot", showDot);
            json.put("opacity", opacity);
            return json;
        } catch (JSONException e) {
            Log.e("SightOverlay", "配置转JSON失败: " + e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * 从 JSON 对象创建配置
     */
    public static SightOverlayConfig fromJson(JSONObject json) {
        SightOverlayConfig config = new SightOverlayConfig();
        try {
            if (json.has("type")) config.type = json.getString("type");
            if (json.has("color")) config.color = json.getString("color");
            if (json.has("size")) config.size = json.getDouble("size");
            if (json.has("thickness")) config.thickness = json.getDouble("thickness");
            if (json.has("showDot")) config.showDot = json.getBoolean("showDot");
            if (json.has("opacity")) config.opacity = json.getDouble("opacity");
        } catch (JSONException e) {
            Log.e("SightOverlay", "JSON解析失败: " + e.getMessage());
        }
        return config;
    }
}

/**
 * 悬浮窗视图 - 自定义绘制准星
 */
class OverlayView extends View {
    private Paint paint;
    private Paint dotPaint;
    private SightOverlayConfig config;

    public OverlayView(Context context) {
        super(context);
        initPaints();
    }

    private void initPaints() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setStyle(Paint.Style.FILL);
    }

    public void setConfig(SightOverlayConfig config) {
        this.config = config;
        invalidate();
    }

    public SightOverlayConfig getConfig() {
        return this.config;
    }

    public boolean isFullScreen() {
        return config != null && "quadrant".equals(config.type);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (config == null) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2;
        float centerY = height / 2;

        // 转换颜色
        int colorInt = Color.parseColor(config.color);
        int alpha = (int) (config.opacity * 255);

        paint.setColor(colorInt);
        paint.setAlpha(alpha);

        dotPaint.setColor(colorInt);
        dotPaint.setAlpha(alpha);

        // 计算实际大小（dp 转 px）
        float density = getResources().getDisplayMetrics().density;
        float sizePx = (float) config.size * density;
        float dotSizePx = 4 * density;
        float gap = Math.max(sizePx * 0.2f, 5 * density);

        paint.setStrokeWidth((float) config.thickness * density);

        // 根据类型绘制不同准星
        switch (config.type) {
            case "cross":
                drawCross(canvas, centerX, centerY, sizePx, gap);
                break;
            case "dot":
                drawDotOnly(canvas, centerX, centerY, dotSizePx);
                break;
            case "tactical":
                drawTactical(canvas, centerX, centerY, sizePx);
                break;
            case "circle":
                drawCircle(canvas, centerX, centerY, sizePx);
                break;
            case "bracket":
                drawBracket(canvas, centerX, centerY, sizePx);
                break;
            case "chevron":
                drawChevron(canvas, centerX, centerY, sizePx);
                break;
            case "quadrant":
                drawQuadrant(canvas, centerX, centerY, sizePx);
                break;
            default:
                drawCross(canvas, centerX, centerY, sizePx, gap);
                break;
        }

        // 绘制中心点（dot类型除外）
        if (config.showDot && !"dot".equals(config.type)) {
            canvas.drawCircle(centerX, centerY, dotSizePx, dotPaint);
        }
    }

    /**
     * 绘制十字准星
     */
    private void drawCross(Canvas canvas, float centerX, float centerY, float sizePx, float gap) {
        // 上
        canvas.drawLine(centerX, centerY - sizePx, centerX, centerY - gap, paint);
        // 下
        canvas.drawLine(centerX, centerY + gap, centerX, centerY + sizePx, paint);
        // 左
        canvas.drawLine(centerX - sizePx, centerY, centerX - gap, centerY, paint);
        // 右
        canvas.drawLine(centerX + gap, centerY, centerX + sizePx, centerY, paint);
    }

    /**
     * 绘制单点准星
     */
    private void drawDotOnly(Canvas canvas, float centerX, float centerY, float dotSizePx) {
        canvas.drawCircle(centerX, centerY, dotSizePx * 2, dotPaint);
    }

    /**
     * 绘制战术准星
     */
    private void drawTactical(Canvas canvas, float centerX, float centerY, float sizePx) {
        float innerRadius = sizePx * 0.3f;

        // 内圆
        canvas.drawCircle(centerX, centerY, innerRadius, paint);

        // 外围短线
        // 左
        canvas.drawLine(centerX - sizePx, centerY, centerX - sizePx * 0.5f, centerY, paint);
        // 右
        canvas.drawLine(centerX + sizePx * 0.5f, centerY, centerX + sizePx, centerY, paint);
        // 上
        canvas.drawLine(centerX, centerY - sizePx, centerX, centerY - sizePx * 0.5f, paint);
        // 下
        canvas.drawLine(centerX, centerY + sizePx * 0.5f, centerX, centerY + sizePx, paint);
    }

    /**
     * 绘制圆形准星
     */
    private void drawCircle(Canvas canvas, float centerX, float centerY, float sizePx) {
        // 设置虚线效果
        float dashWidth = sizePx * 0.5f;
        float gapWidth = sizePx * 0.3f;
        DashPathEffect dashEffect = new DashPathEffect(new float[]{dashWidth, gapWidth}, 0);
        paint.setPathEffect(dashEffect);

        canvas.drawCircle(centerX, centerY, sizePx, paint);

        // 重置虚线效果
        paint.setPathEffect(null);
    }

    /**
     * 绘制方括号准星
     */
    private void drawBracket(Canvas canvas, float centerX, float centerY, float sizePx) {
        float offset = sizePx * 0.6f;
        float density = getResources().getDisplayMetrics().density;
        float lineLength = sizePx - offset;

        // 左上
        canvas.drawLine(centerX - sizePx, centerY - offset, centerX - sizePx, centerY - sizePx, paint);
        canvas.drawLine(centerX - sizePx, centerY - sizePx, centerX - offset, centerY - sizePx, paint);
        // 右上
        canvas.drawLine(centerX + offset, centerY - sizePx, centerX + sizePx, centerY - sizePx, paint);
        canvas.drawLine(centerX + sizePx, centerY - sizePx, centerX + sizePx, centerY - offset, paint);
        // 左下
        canvas.drawLine(centerX - sizePx, centerY + offset, centerX - sizePx, centerY + sizePx, paint);
        canvas.drawLine(centerX - sizePx, centerY + sizePx, centerX - offset, centerY + sizePx, paint);
        // 右下
        canvas.drawLine(centerX + offset, centerY + sizePx, centerX + sizePx, centerY + sizePx, paint);
        canvas.drawLine(centerX + sizePx, centerY + sizePx, centerX + sizePx, centerY + offset, paint);
    }

    /**
     * 绘制V形准星
     */
    private void drawChevron(Canvas canvas, float centerX, float centerY, float sizePx) {
        Path path = new Path();
        // V形
        path.moveTo(centerX - sizePx * 0.6f, centerY - sizePx * 0.3f);
        path.lineTo(centerX, centerY + sizePx * 0.5f);
        path.lineTo(centerX + sizePx * 0.6f, centerY - sizePx * 0.3f);

        // 上方短线
        path.moveTo(centerX, centerY - sizePx);
        path.lineTo(centerX, centerY - sizePx * 0.3f);

        canvas.drawPath(path, paint);
    }

    /**
     * 绘制象限准星
     */
    private void drawQuadrant(Canvas canvas, float centerX, float centerY, float sizePx) {
        float width = getWidth();
        float height = getHeight();

        // 象限准星：从中心向四个方向延伸到屏幕边缘
        // 右侧水平线 - 从中心延伸到右边缘
        canvas.drawLine(centerX, centerY, width, centerY, paint);
        // 上方垂直线 - 从中心延伸到顶部边缘
        canvas.drawLine(centerX, centerY, centerX, 0, paint);
        // 左侧水平线 - 从中心延伸到左边缘
        canvas.drawLine(centerX, centerY, 0, centerY, paint);
        // 下方垂直线 - 从中心延伸到底部边缘
        canvas.drawLine(centerX, centerY, centerX, height, paint);
    }
}

/**
 * 悬浮窗管理器模块
 */
public class sight_overlay {

    private static sight_overlay instance;
    private Context mContext;
    private WindowManager mWindowManager;
    private OverlayView mOverlayView;
    private String currentType = "cross"; // 保存当前准星类型
    private int screenWidth = 0;
    private int screenHeight = 0;
    private int screenOrientation = Configuration.ORIENTATION_PORTRAIT; // 屏幕方向
    private int rotation = 0; // 屏幕旋转角度

    private sight_overlay() {
    }

    public static sight_overlay getInstance() {
        if (instance == null) {
            synchronized (sight_overlay.class) {
                if (instance == null) {
                    instance = new sight_overlay();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化悬浮窗管理器
     */
    public static void init(Context context) {
        sight_overlay module = getInstance();
        if (module.mContext == null) {
            module.mContext = context.getApplicationContext();
            module.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            module.updateScreenSize();
            Log.i("SightOverlay", "初始化成功");
        }
    }

    /**
     * 获取屏幕旋转角度
     */
    private void updateScreenRotation() {
        if (mWindowManager != null) {
            android.view.Display display = mWindowManager.getDefaultDisplay();
            if (display != null) {
                rotation = display.getRotation();
                String rotationStr;
                switch (rotation) {
                    case android.view.Surface.ROTATION_0:
                        rotationStr = "0° (竖屏)";
                        break;
                    case android.view.Surface.ROTATION_90:
                        rotationStr = "90° (横屏)";
                        break;
                    case android.view.Surface.ROTATION_180:
                        rotationStr = "180° (倒竖屏)";
                        break;
                    case android.view.Surface.ROTATION_270:
                        rotationStr = "270° (倒横屏)";
                        break;
                    default:
                        rotationStr = "未知";
                        break;
                }
                Log.i("SightOverlay", "屏幕旋转: " + rotationStr);
            }
        }
    }

    /**
     * 获取屏幕方向（横屏/竖屏）
     */
    private void updateScreenOrientation() {
        if (mContext != null) {
            screenOrientation = mContext.getResources().getConfiguration().orientation;
            String orientationStr = (screenOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    ? "横屏" : "竖屏";
            Log.i("SightOverlay", "屏幕方向: " + orientationStr);
        }
    }

    /**
     * 更新屏幕尺寸（考虑旋转）
     */
    private void updateScreenSize() {
        if (mWindowManager != null) {
            // 先获取当前旋转角度和方向
            updateScreenRotation();
            updateScreenOrientation();

            android.view.Display display = mWindowManager.getDefaultDisplay();
            android.graphics.Point screenSize = new android.graphics.Point();
            display.getSize(screenSize);

            // 横屏时需要交换宽高，确保 width > height
            if (isLandscape()) {
                screenWidth = Math.max(screenSize.x, screenSize.y);
                screenHeight = Math.min(screenSize.x, screenSize.y);
            } else {
                screenWidth = screenSize.x;
                screenHeight = screenSize.y;
            }

            String orientationStr = (screenOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    ? "横屏" : "竖屏";
            Log.i("SightOverlay", "屏幕尺寸: " + screenWidth + "x" + screenHeight + " (" + orientationStr + ")");
        }
    }

    /**
     * 判断是否为横屏
     */
    private boolean isLandscape() {
        return screenOrientation == Configuration.ORIENTATION_LANDSCAPE || rotation == android.view.Surface.ROTATION_90 || rotation == android.view.Surface.ROTATION_270;
    }

    /**
     * 显示悬浮窗
     */
    public static void show(String type, String color, double size, double thickness, boolean showDot, double opacity) {
        getInstance().showInternal(type, color, size, thickness, showDot, opacity);
    }

    /**
     * 隐藏悬浮窗
     */
    public static void hide() {
        getInstance().hideInternal();
    }

    /**
     * 更新悬浮窗配置
     */
    public static void update(String type, String color, double size, double thickness, boolean showDot, double opacity) {
        getInstance().updateInternal(type, color, size, thickness, showDot, opacity);
    }

    /**
     * 检查悬浮窗权限
     */
    public static boolean checkPermission() {
        return getInstance().checkPermissionInternal();
    }

    /**
     * 请求悬浮窗权限
     */
    public static void requestPermission() {
        getInstance().requestPermissionInternal();
    }

    /**
     * 获取支持的准星类型列表
     * @return JSON 字符串，包含所有支持的准星类型
     */
    public static String getSupportedTypes() {
        try {
            JSONArray types = new JSONArray();
            for (String type : SightType.getSupportedTypes()) {
                types.put(type);
            }
            JSONObject result = new JSONObject();
            result.put("types", types);
            return result.toString();
        } catch (JSONException e) {
            Log.e("SightOverlay", "获取支持类型失败: " + e.getMessage());
            return "{\"types\":[]}";
        }
    }

    /**
     * 获取默认配置
     * @return JSON 字符串，包含默认配置
     */
    public static String getDefaultConfig() {
        return SightDefaults.getDefaultConfigJson().toString();
    }

    /**
     * 获取可配置参数的元信息
     * @return JSON 字符串，描述每个可配置参数的类型和取值范围
     */
    public static String getConfigMetadata() {
        try {
            JSONObject metadata = new JSONObject();

            // type 参数
            JSONObject typeInfo = new JSONObject();
            typeInfo.put("type", "string");
            typeInfo.put("description", "准星类型");
            typeInfo.put("enum", new JSONArray(SightType.getSupportedTypes()));
            typeInfo.put("default", SightDefaults.DEFAULT_TYPE);
            metadata.put("type", typeInfo);

            // color 参数
            JSONObject colorInfo = new JSONObject();
            colorInfo.put("type", "string");
            colorInfo.put("description", "准星颜色（十六进制格式）");
            colorInfo.put("pattern", "^#[0-9A-Fa-f]{6}$");
            colorInfo.put("default", SightDefaults.DEFAULT_COLOR);
            colorInfo.put("examples", new JSONArray(new String[]{"#00FF00", "#FF0000", "#00FFFF", "#FFFF00", "#FF00FF"}));
            metadata.put("color", colorInfo);

            // size 参数
            JSONObject sizeInfo = new JSONObject();
            sizeInfo.put("type", "number");
            sizeInfo.put("description", "准星大小（像素）");
            sizeInfo.put("min", 5);
            sizeInfo.put("max", 100);
            sizeInfo.put("default", SightDefaults.DEFAULT_SIZE);
            metadata.put("size", sizeInfo);

            // thickness 参数
            JSONObject thicknessInfo = new JSONObject();
            thicknessInfo.put("type", "number");
            thicknessInfo.put("description", "准星粗细（像素）");
            thicknessInfo.put("min", 1);
            thicknessInfo.put("max", 10);
            thicknessInfo.put("default", SightDefaults.DEFAULT_THICKNESS);
            metadata.put("thickness", thicknessInfo);

            // showDot 参数
            JSONObject showDotInfo = new JSONObject();
            showDotInfo.put("type", "boolean");
            showDotInfo.put("description", "是否显示中心点");
            showDotInfo.put("default", SightDefaults.DEFAULT_SHOW_DOT);
            metadata.put("showDot", showDotInfo);

            // opacity 参数
            JSONObject opacityInfo = new JSONObject();
            opacityInfo.put("type", "number");
            opacityInfo.put("description", "准星透明度（0-1）");
            opacityInfo.put("min", 0.1);
            opacityInfo.put("max", 1.0);
            opacityInfo.put("default", SightDefaults.DEFAULT_OPACITY);
            metadata.put("opacity", opacityInfo);

            return metadata.toString();
        } catch (JSONException e) {
            Log.e("SightOverlay", "获取配置元信息失败: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * 释放资源
     */
    public static void release() {
        getInstance().releaseInternal();
    }

    /**
     * 创建布局参数
     */
    private WindowManager.LayoutParams createLayoutParams() {
        int type;
        if (Build.VERSION.SDK_INT >= 26) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_FULLSCREEN;

        // 象限准星需要全屏，其他准星使用 200x200
        int width = 200;
        int height = 200;
        if ("quadrant".equals(currentType)) {
            width = this.screenWidth;
            height = this.screenHeight;
            Log.i("SightOverlay", "象限准星使用全屏尺寸: " + width + "x" + height);
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width, // width
                height, // height
                type,
                flags,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;

        return params;
    }

    /**
     * 显示悬浮窗（内部实现）
     */
    private void showInternal(String type, String color, double size, double thickness, boolean showDot, double opacity) {
        if (mContext == null) {
            Log.e("SightOverlay", "悬浮窗未初始化，请先调用 init");
            return;
        }

        // 检查权限
        if (!checkPermissionInternal()) {
            Log.e("SightOverlay", "没有悬浮窗权限");
            return;
        }

        try {
            // 先获取屏幕方向和尺寸
            updateScreenSize();

            // 保存当前类型
            this.currentType = type;

            // 隐藏已存在的悬浮窗
            hideInternal();

            SightOverlayConfig config = new SightOverlayConfig(type, color, size, thickness, showDot, opacity);

            // 创建新视图
            mOverlayView = new OverlayView(mContext);
            mOverlayView.setConfig(config);

            // 添加到窗口（此时 createLayoutParams 会使用最新获取的屏幕尺寸）
            WindowManager.LayoutParams params = createLayoutParams();
            mWindowManager.addView(mOverlayView, params);

            Log.i("SightOverlay", "悬浮窗显示成功, 类型: " + type + ", 尺寸: " + params.width + "x" + params.height);
        } catch (Exception e) {
            Log.e("SightOverlay", "显示悬浮窗失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 隐藏悬浮窗（内部实现）
     */
    private void hideInternal() {
        try {
            if (mOverlayView != null && mWindowManager != null) {
                mWindowManager.removeView(mOverlayView);
                mOverlayView = null;
            }
        } catch (Exception e) {
            Log.e("SightOverlay", "隐藏悬浮窗失败: " + e.getMessage());
        }
    }

    /**
     * 更新悬浮窗配置（内部实现）
     */
    private void updateInternal(String type, String color, double size, double thickness, boolean showDot, double opacity) {
        // 检查是否需要切换全屏模式（象限准星需要全屏）
        boolean wasFullScreen = "quadrant".equals(currentType);
        boolean isFullScreen = "quadrant".equals(type);

        // 如果全屏模式改变，需要重新创建悬浮窗
        if (wasFullScreen != isFullScreen) {
            showInternal(type, color, size, thickness, showDot, opacity);
            return;
        }

        SightOverlayConfig config = new SightOverlayConfig(type, color, size, thickness, showDot, opacity);
        if (mOverlayView != null) {
            mOverlayView.setConfig(config);
        }
    }

    /**
     * 检查悬浮窗权限（内部实现）
     */
    private boolean checkPermissionInternal() {
        if (mContext == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.canDrawOverlays(mContext);
        }
        return true;
    }

    /**
     * 请求悬浮窗权限（内部实现）
     */
    private void requestPermissionInternal() {
        if (mContext == null) {
            Log.e("SightOverlay", "悬浮窗未初始化");
            return;
        }

        if (checkPermissionInternal()) {
            Log.i("SightOverlay", "权限已授予，无需请求");
            return;
        }

        // Android 6.0+ 需要跳转到设置页面
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                String packageName = mContext.getPackageName();
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + packageName)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                Log.i("SightOverlay", "已发起悬浮窗权限请求");
            } catch (Exception e) {
                Log.e("SightOverlay", "请求悬浮窗权限失败: " + e.getMessage());
                // 如果直接跳转失败，尝试打开应用的详细设置页面作为备用方案
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (Exception e2) {
                    Log.e("SightOverlay", "打开应用详情设置也失败了: " + e2.getMessage());
                }
            }
        }
    }

    /**
     * 释放资源（内部实现）
     */
    private void releaseInternal() {
        hideInternal();
        mContext = null;
        mWindowManager = null;
    }
}
