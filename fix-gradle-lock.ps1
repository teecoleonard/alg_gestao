# Script PowerShell para resolver arquivo bloqueado no Gradle
Write-Host "🔧 Corrigindo arquivo bloqueado no Gradle..." -ForegroundColor Cyan

# 1. Matar TODOS os processos relacionados
Write-Host "`n📛 Encerrando processos relacionados..." -ForegroundColor Yellow

$processos = @("java", "gradle", "studio64", "studio", "adb", "kotlinc")
foreach ($proc in $processos) {
    try {
        Get-Process -Name $proc -ErrorAction SilentlyContinue | Stop-Process -Force
        Write-Host "  ✅ $proc encerrado" -ForegroundColor Green
    } catch {
        Write-Host "  ⏭️  $proc não encontrado" -ForegroundColor Gray
    }
}

# 2. Aguardar um pouco
Write-Host "`n⏳ Aguardando 3 segundos..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# 3. Tentar remover a pasta build com força máxima
Write-Host "`n🗑️  Removendo pasta build..." -ForegroundColor Yellow
$buildPath = "app\build"

if (Test-Path $buildPath) {
    try {
        # Remover atributos read-only recursivamente
        Get-ChildItem -Path $buildPath -Recurse | ForEach-Object {
            $_.Attributes = 'Normal'
        }
        
        # Remover a pasta
        Remove-Item -Path $buildPath -Recurse -Force -ErrorAction Stop
        Write-Host "  ✅ Pasta build removida com sucesso!" -ForegroundColor Green
    } catch {
        Write-Host "  ⚠️  Erro ao remover pasta: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "`n  🔍 Tentando identificar o processo que está usando o arquivo..." -ForegroundColor Yellow
        
        # Tentar usar handle.exe se disponível (SysInternals)
        if (Get-Command handle -ErrorAction SilentlyContinue) {
            handle "R.jar"
        }
        
        Write-Host "`n  💡 SOLUÇÃO: Feche o Android Studio e execute novamente!" -ForegroundColor Cyan
        exit 1
    }
} else {
    Write-Host "  ℹ️  Pasta build não existe" -ForegroundColor Gray
}

# 4. Limpar Gradle
Write-Host "`n🧹 Limpando Gradle..." -ForegroundColor Yellow
try {
    .\gradlew --stop
    Write-Host "  ✅ Gradle daemon parado" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  Erro ao parar Gradle: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Executar clean
Write-Host "`n🔄 Executando gradlew clean..." -ForegroundColor Yellow
.\gradlew clean

# 6. Compilar
Write-Host "`n🔨 Compilando APK..." -ForegroundColor Yellow
.\gradlew assembleDebug

Write-Host "`n✅ Script finalizado!" -ForegroundColor Green





