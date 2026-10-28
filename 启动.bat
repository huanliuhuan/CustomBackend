@echo off
title AdminBase Launcher
cd /d %~dp0

echo ==============================================
echo   AdminBase  -  one click start
echo ==============================================

echo [1/3] checking docker / mysql container ...
docker ps --format "{{.Names}}" 2>nul | findstr "mysql" >nul
if errorlevel 1 (
  echo.
  echo   [!] Docker is not running or mysql container is down.
  echo       Please start Docker Desktop first, then run again.
  echo.
  pause
  exit /b 1
)
echo       mysql container OK

docker exec mysql mysql -uroot -p123456 -e "use admin_base; select 1" >nul 2>nul
if errorlevel 1 (
  echo [1/3] first run - initializing database admin_base ...
  docker cp "%~dp0sql\admin_base.sql" mysql:/tmp/admin_base.sql
  docker exec mysql sh -c "mysql -uroot -p123456 --default-character-set=utf8mb4 -e 'source /tmp/admin_base.sql'"
) else (
  echo       database already exists, skip init
)

echo [2/3] starting backend spring boot on 8081 ...
start "AdminBase-backend-8081" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo [3/3] starting frontend vite on 5173 ...
start "AdminBase-frontend-5173" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo   keep the two windows open. browser will open in a few seconds:
echo       http://localhost:5173
echo       login  admin / admin123
echo.
timeout /t 8 >nul
start http://localhost:5173
