@echo off
chcp 65001 > nul
set /p msg="請輸入 Commit 訊息: "

if "%msg%"=="" (
    echo 錯誤: 訊息不能為空！
    pause
    exit /b
)

echo 正在處理 Git 指令...
git add .
git commit -m "%msg%"
git push

echo.
echo 完成！
pause
