@echo off
echo =========================================
echo MATANDO TODOS OS PROCESSOS DO GRADLE
echo =========================================
echo.

echo [1/4] Matando processos Java...
taskkill /F /IM java.exe /T 2>nul
if %errorlevel% equ 0 (
    echo    ✓ Processos Java encerrados
) else (
    echo    - Nenhum processo Java encontrado
)

echo.
echo [2/4] Matando processos Gradle...
taskkill /F /IM gradle.exe /T 2>nul
taskkill /F /IM gradlew.exe /T 2>nul

echo.
echo [3/4] Parando Gradle Daemon...
call gradlew --stop 2>nul

echo.
echo [4/4] Limpando pasta build...
if exist "app\build" (
    rmdir /S /Q "app\build" 2>nul
    echo    ✓ Pasta build removida
) else (
    echo    - Pasta build não existe
)

echo.
echo =========================================
echo PRONTO! Agora você pode tentar o build.
echo =========================================
echo.
pause






