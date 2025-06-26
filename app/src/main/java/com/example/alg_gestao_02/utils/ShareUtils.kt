package com.example.alg_gestao_02.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utilitário para compartilhamento de arquivos
 */
object ShareUtils {
    
    /**
     * Compartilha um arquivo PDF
     */
    fun sharePdfFile(context: Context, file: File) {
        try {
            LogUtils.info("ShareUtils", "🚀 ========== INICIANDO COMPARTILHAMENTO ==========")
            LogUtils.info("ShareUtils", "📄 Arquivo: ${file.absolutePath}")
            LogUtils.info("ShareUtils", "📏 Tamanho: ${file.length()} bytes")
            
            // Criar URI usando FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            LogUtils.info("ShareUtils", "🔗 URI gerada: $uri")
            
            // Criar intent de compartilhamento
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório Financeiro - ALG Gestão")
                putExtra(Intent.EXTRA_TEXT, "Relatório financeiro gerado pelo ALG Gestão")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Criar chooser
            val chooserIntent = Intent.createChooser(shareIntent, "Compartilhar Relatório")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            LogUtils.info("ShareUtils", "📱 Abrindo seletor de compartilhamento...")
            context.startActivity(chooserIntent)
            
            LogUtils.info("ShareUtils", "✅ Compartilhamento iniciado com sucesso!")
            
        } catch (e: Exception) {
            LogUtils.error("ShareUtils", "❌ Erro ao compartilhar arquivo: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Abre um arquivo PDF no visualizador padrão
     */
    fun openPdfFile(context: Context, file: File) {
        try {
            LogUtils.info("ShareUtils", "🚀 ========== ABRINDO ARQUIVO PDF ==========")
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Verificar se há um app para abrir PDF
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                LogUtils.info("ShareUtils", "✅ Arquivo PDF aberto com sucesso!")
            } else {
                LogUtils.warning("ShareUtils", "⚠️ Nenhum app encontrado para abrir PDF")
                // Fallback: tentar compartilhar
                sharePdfFile(context, file)
            }
            
        } catch (e: Exception) {
            LogUtils.error("ShareUtils", "❌ Erro ao abrir arquivo: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Verifica se um arquivo existe e é válido
     */
    fun isFileValid(file: File?): Boolean {
        return file != null && file.exists() && file.length() > 0
    }
    
    /**
     * Formata o tamanho do arquivo para exibição
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes bytes"
        }
    }
} 