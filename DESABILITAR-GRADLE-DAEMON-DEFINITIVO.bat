@echo off
echo =========================================
echo DESABILITANDO GRADLE DAEMON GLOBALMENTE
echo =========================================
echo.
echo Esse script vai FORÇAR o Gradle a NUNCA
echo usar o daemon em segundo plano.
echo.
pause

echo [1/3] Criando gradle.properties global...
if not exist "%USERPROFILE%\.gradle" mkdir "%USERPROFILE%\.gradle"

(
echo # CONFIGURACAO GLOBAL - DESABILITA DAEMON
echo org.gradle.daemon=false
echo org.gradle.parallel=false
echo org.gradle.configureondemand=false
echo org.gradle.caching=false
) > "%USERPROFILE%\.gradle\gradle.properties"

echo    ✓ Arquivo criado em %USERPROFILE%\.gradle\gradle.properties

echo.
echo [2/3] Matando todos os processos Gradle...
taskkill /F /IM java.exe /T 2>nul
call gradlew --stop 2>nul
echo    ✓ Processos encerrados

echo.
echo [3/3] Limpando cache do Gradle...
if exist "app\build" rmdir /S /Q "app\build" 2>nul
if exist ".gradle" rmdir /S /Q ".gradle" 2>nul
echo    ✓ Cache limpo

echo.
echo =========================================
echo PRONTO! O Gradle Daemon foi DESABILITADO.
echo.
echo Agora os builds vao ser um pouco mais
echo lentos, mas NAO VAO MAIS TRAVAR arquivos!
echo =========================================
echo.
pause






