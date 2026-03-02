@echo off
echo Matando processos Java...
taskkill /F /IM java.exe 2>nul

timeout /t 2 /nobreak >nul

echo Removendo pasta build...
if exist "app\build" rmdir /S /Q "app\build"

echo.
echo PRONTO! Tente o build novamente.






