@echo off
setlocal

set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%backend"
set "FRONTEND_DIR=%ROOT_DIR%frontend"

if not exist "%BACKEND_DIR%\mvnw.cmd" (
  echo [ERROR] Backend wrapper not found: "%BACKEND_DIR%\mvnw.cmd"
  exit /b 1
)

if not exist "%FRONTEND_DIR%\package.json" (
  echo [ERROR] Frontend package.json not found: "%FRONTEND_DIR%\package.json"
  exit /b 1
)

echo Starting backend and frontend...

start "competition-backend" powershell -NoExit -Command "Set-Location -LiteralPath '%BACKEND_DIR%'; .\mvnw.cmd spring-boot:run"
start "competition-frontend" powershell -NoExit -Command "Set-Location -LiteralPath '%FRONTEND_DIR%'; if (-not (Test-Path -LiteralPath '.\node_modules')) { npm install }; npm run dev"

echo Done. Two terminal windows should be running:
echo - competition-backend
echo - competition-frontend

endlocal
