@echo off
REM ===============================================================
REM 数据库备份脚本
REM 功能：备份 java_2_48 数据库
REM ===============================================================

REM 设置变量
set DB_NAME=java_2_48
set DB_USER=root
set BACKUP_DIR=.\backups

REM 创建备份目录
if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
    echo 已创建备份目录: %BACKUP_DIR%
)

REM 生成备份文件名（包含时间戳）
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_FILE=%BACKUP_DIR%\backup_%DB_NAME%_%TIMESTAMP%.sql

REM 提示输入密码
echo 正在备份数据库 %DB_NAME%...
echo 请输入 MySQL root 密码（输入后按回车）:

REM 执行备份命令
mysqldump -u %DB_USER% -p %DB_NAME% > "%BACKUP_FILE%"

REM 检查备份是否成功
if %errorlevel% equ 0 (
    echo.
    echo ===============================================================
    echo ✅ 备份成功！
    echo 备份文件: %BACKUP_FILE%
    echo ===============================================================
) else (
    echo.
    echo ===============================================================
    echo ❌ 备份失败！
    echo 请检查：
    echo 1. MySQL 服务是否已启动
    echo 2. 用户名和密码是否正确
    echo 3. mysqldump 命令是否在 PATH 环境变量中
    echo ===============================================================
)

REM 等待用户按键
echo.
pause
