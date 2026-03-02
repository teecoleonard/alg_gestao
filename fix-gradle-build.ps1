# Script para corrigir erro de build do Gradle
# Erro: "O arquivo já está sendo usado por outro processo"

Write-Host "=== Corrigindo erro de build do Gradle ===" -ForegroundColor Cyan
Write-Host ""

# 1. Matar processos Java que podem estar travando arquivos
Write-Host "1. Encerrando processos Java..." -ForegroundColor Yellow
Get-Process | Where-Object { $_.ProcessName -like "*java*" } | ForEach-Object {
    Write-Host "   Matando processo: $($_.ProcessName) (PID: $($_.Id))" -ForegroundColor Gray
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}

# Esperar um pouco para os processos liberarem os arquivos
Start-Sleep -Seconds 2

# 2. Matar processo do Gradle Daemon
Write-Host "2. Encerrando Gradle Daemon..." -ForegroundColor Yellow
try {
    & .\gradlew --stop
    Write-Host "   Gradle Daemon encerrado com sucesso" -ForegroundColor Green
} catch {
    Write-Host "   Aviso: Não foi possível encerrar o Gradle Daemon" -ForegroundColor Gray
}

Start-Sleep -Seconds 2

# 3. Tentar deletar a pasta build manualmente
Write-Host "3. Removendo pasta build..." -ForegroundColor Yellow
$buildPath = ".\app\build"
if (Test-Path $buildPath) {
    try {
        Remove-Item -Recurse -Force $buildPath -ErrorAction Stop
        Write-Host "   Pasta build removida com sucesso" -ForegroundColor Green
    } catch {
        Write-Host "   Aviso: Não foi possível remover completamente a pasta build" -ForegroundColor Gray
        Write-Host "   Tentando remover arquivos específicos..." -ForegroundColor Gray
        
        # Tentar remover o arquivo problemático específico
        $problematicFile = ".\app\build\intermediates\compile_and_runtime_not_namespaced_r_class_jar\debug\processDebugResources\R.jar"
        if (Test-Path $problematicFile) {
            try {
                Remove-Item -Force $problematicFile -ErrorAction Stop
                Write-Host "   Arquivo R.jar removido" -ForegroundColor Green
            } catch {
                Write-Host "   Erro ao remover R.jar: $_" -ForegroundColor Red
            }
        }
    }
} else {
    Write-Host "   Pasta build não existe" -ForegroundColor Gray
}

# 4. Limpar cache do Gradle
Write-Host "4. Limpando cache do Gradle..." -ForegroundColor Yellow
try {
    & .\gradlew clean
    Write-Host "   Cache limpo com sucesso" -ForegroundColor Green
} catch {
    Write-Host "   Aviso: Erro ao executar gradlew clean" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== Correção concluída ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Green
Write-Host "1. Feche o Android Studio se estiver aberto" -ForegroundColor White
Write-Host "2. Execute: .\gradlew clean" -ForegroundColor White
Write-Host "3. Execute: .\gradlew build" -ForegroundColor White
Write-Host "4. Se o erro persistir, reinicie o computador" -ForegroundColor White
Write-Host ""






