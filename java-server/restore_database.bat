@echo off
REM ===============================================================
REM 数据库恢复脚本
REM 功能：从备份文件恢复 java_2_48 数据库
REM ===============================================================

REM 设置变量
set DB_NAME=java_2_48
set DB_USER=root
set BACKUP_DIR=.\backups

REM 检查备份目录是否存在
if not exist "%BACKUP_DIR%" (
    echo ❌ 备份目录不存在: %BACKUP_DIR%
    echo 请先执行备份脚本或手动创建目录
    pause
    exit /b
)

REM 列出所有备份文件
echo ===============================================================
echo 可用的备份文件:
echo ===============================================================
dir /b "%BACKUP_DIR%\*.sql"
echo ===============================================================

REM 提示用户输入要恢复的文件名
echo.
set /p BACKUP_FILE="请输入要恢复的备份文件名（例如：backup_java_2_48_20240520_143000.sql）: "

REM 检查文件是否存在
if not exist "%BACKUP_DIR%\%BACKUP_FILE%" (
    echo ❌ 文件不存在: %BACKUP_DIR%\%BACKUP_FILE%
    pause
    exit /b
)

REM 确认恢复
echo.
echo ⚠️  警告：此操作将覆盖当前数据库 %DB_NAME% 的所有数据！
echo 备份文件: %BACKUP_DIR%\%BACKUP_FILE%
echo.
set /p CONFIRM="确认要恢复吗？(y/n): "

if /i not "%CONFIRM%"=="y" (
    echo 已取消恢复操作
    pause
    exit /b
)

REM 执行恢复命令
echo.
echo 正在恢复数据库...
echo 请输入 MySQL root 密码（输入后按回车）:

mysql -u %DB_USER% -p %DB_NAME% < "%BACKUP_DIR%\%BACKUP_FILE%"

REM 检查恢复是否成功
if %errorlevel% equ 0 (
    echo.
    echo ===============================================================
    echo ✅ 恢复成功！
    echo 数据库已从 %BACKUP_FILE% 恢复
    echo ===============================================================
) else (
    echo.
    echo ===============================================================
    echo ❌ 恢复失败！
    echo 请检查：
    echo 1. MySQL 服务是否已启动
    echo 2. 用户名和密码是否正确
    echo 3. mysql 命令是否在 PATH 环境变量中
    echo ===============================================================
)

REM 等待用户按键
echo.
pause
