@echo off
echo ==========================================
echo       Leyi Snack Shop - Start Script
echo ==========================================

:: 1. Start Backend
echo [1/3] Starting Backend...
start "Leyi Backend" cmd /c "set JAVA_HOME=D:\program tool\java\jdk&& set PATH=%JAVA_HOME%\bin;%PATH%&& java -jar backend\target\leyi-snack-1.0.0.jar"

:: 2. Start Admin Frontend
echo [2/3] Starting Admin Frontend...
start "Leyi Admin" cmd /c "cd frontend-admin && npm run dev"

:: 3. Start Customer Frontend
echo [3/3] Starting Customer Frontend...
start "Leyi Customer" cmd /c "cd frontend-customer && npm run dev"

echo.
echo All services started in background windows.
echo Waiting 15 seconds for services to initialize...
echo.

:: Wait for services
timeout /t 15 >nul

:: 4. Open Browser
echo [4/4] Opening Browser...
start http://localhost:3000/login
start http://localhost:3001/login

echo.
echo ==========================================
echo          Done! Please check browser.
echo      (Do NOT close the popped up windows)
echo ==========================================
pause