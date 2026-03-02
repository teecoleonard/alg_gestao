@echo off
echo ========================================
echo   SOLUCAO DEFINITIVA - GRADLE LOCK
echo ========================================
echo.

echo [1/5] Parando todos os daemons Gradle...
call gradlew --stop
timeout /t 2 /nobreak >nul

echo.
echo [2/5] Matando processos Java...
taskkill /F /IM java.exe /T 2>nul
taskkill /F /IM javaw.exe /T 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [3/5] Removendo diretorio build...
if exist "app\build" (
    rmdir /S /Q "app\build" 2>nul
    echo Diretorio app\build removido
) else (
    echo Diretorio app\build nao existe
)

if exist ".gradle" (
    rmdir /S /Q ".gradle" 2>nul
    echo Diretorio .gradle removido
)

timeout /t 2 /nobreak >nul

echo.
echo [4/5] Limpando cache do Gradle...
call gradlew clean
timeout /t 2 /nobreak >nul

echo.
echo [5/5] Compilando APK...
call gradlew assembleDebug --no-daemon --no-parallel

echo.
echo ========================================
echo   PROCESSO CONCLUIDO!
echo ========================================
pause





