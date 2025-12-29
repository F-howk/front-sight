@echo off
REM Logo PNG 生成脚本 (Windows)
REM 需要安装 ImageMagick: https://imagemagick.org/script/download.php

setlocal

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set SVG_FILE=%PROJECT_ROOT%\src\static\logo-icon.svg
set OUTPUT_DIR=%PROJECT_ROOT%\src\static\icons

REM 创建输出目录
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo.
echo ========================================
echo    Logo PNG 图标生成工具
echo ========================================
echo.
echo SVG 源文件: %SVG_FILE%
echo 输出目录: %OUTPUT_DIR%
echo.

REM 检查 convert 命令是否可用
where convert >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 错误: 未安装 ImageMagick
    echo.
    echo 请安装 ImageMagick:
    echo   1. 访问 https://imagemagick.org/script/download.php
    echo   2. 下载 Windows 版本
    echo   3. 安装时确保勾选 "Install legacy utilities (e.g. convert)"
    echo.
    pause
    exit /b 1
)

REM 检查 SVG 文件是否存在
if not exist "%SVG_FILE%" (
    echo ❌ 错误: 找不到 SVG 文件
    echo    %SVG_FILE%
    pause
    exit /b 1
)

REM 生成各个尺寸的 PNG
echo ⏳ 正在生成 PNG 图标...
echo.

set SIZES=512 192 144 96 72 48

for %%s in (%SIZES%) do (
    set OUTPUT_FILE=%OUTPUT_DIR%\logo-%%sx%%s.png
    echo   生成 %%sx%%s...
    convert -background none -density 300 -resize "%%sx%%s" "%SVG_FILE%" "!OUTPUT_FILE!"
    if %ERRORLEVEL% EQU 0 (
        echo   ✅ logo-%%sx%%s.png
    ) else (
        echo   ❌ 生成失败
    )
)

echo.
echo ========================================
echo 🎉 所有图标生成完成!
echo ========================================
echo.
echo 📁 输出目录: %OUTPUT_DIR%
dir /b "%OUTPUT_DIR%"
echo.
pause
