package com.example.alg_gestao_02.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilitário para gerenciar downloads e compartilhamento de PDFs
 * compatível com todas as versões do Android, incluindo Scoped Storage
 */
object PdfUtils {
    
    /**
     * Salva um PDF na pasta Downloads do dispositivo
     * Funciona em todas as versões do Android (com ou sem Scoped Storage)
     */
    fun salvarPdfNaPastaDownloads(
        context: Context,
        pdfBytes: ByteArray,
        nomeBase: String
    ): Result<String> {
        return try {
            LogUtils.debug("PdfUtils", "🔥 INÍCIO - salvarPdfNaPastaDownloads")
            LogUtils.debug("PdfUtils", "📦 Tamanho dos bytes: ${pdfBytes.size}")
            LogUtils.debug("PdfUtils", "📝 Nome base: '$nomeBase'")
            LogUtils.debug("PdfUtils", "📱 Android SDK: ${Build.VERSION.SDK_INT}")
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${nomeBase}_$timestamp.pdf"
            LogUtils.debug("PdfUtils", "📄 Nome do arquivo: '$fileName'")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                LogUtils.debug("PdfUtils", "🚀 Usando MediaStore API (Android 10+)")
                salvarComMediaStore(context, pdfBytes, fileName)
            } else {
                LogUtils.debug("PdfUtils", "🚀 Usando storage tradicional (Android 9-)")
                salvarComStorageTradicional(pdfBytes, fileName)
            }
        } catch (e: Exception) {
            LogUtils.error("PdfUtils", "💥 EXCEÇÃO em salvarPdfNaPastaDownloads", e)
            Result.failure(e)
        }
    }
    
    /**
     * Cria um arquivo temporário para compartilhamento
     */
    fun criarArquivoTemporario(
        context: Context,
        pdfBytes: ByteArray,
        nomeBase: String
    ): Result<File> {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${nomeBase}_$timestamp.pdf"
            val tempFile = File(context.cacheDir, fileName)
            
            FileOutputStream(tempFile).use { it.write(pdfBytes) }
            
            LogUtils.debug("PdfUtils", "Arquivo temporário criado: ${tempFile.absolutePath}")
            Result.success(tempFile)
        } catch (e: Exception) {
            LogUtils.error("PdfUtils", "Erro ao criar arquivo temporário", e)
            Result.failure(e)
        }
    }
    
    /**
     * Compartilha um PDF usando FileProvider
     */
    fun compartilharPdf(
        context: Context,
        arquivo: File,
        titulo: String = "Compartilhar PDF",
        textoAdicional: String = ""
    ): Result<Unit> {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                arquivo
            )
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                if (textoAdicional.isNotEmpty()) {
                    putExtra(Intent.EXTRA_TEXT, textoAdicional)
                    putExtra(Intent.EXTRA_SUBJECT, titulo)
                }
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            
            // Verificar se há apps disponíveis
            val packageManager = context.packageManager
            if (shareIntent.resolveActivity(packageManager) != null) {
                context.startActivity(Intent.createChooser(shareIntent, titulo))
                LogUtils.debug("PdfUtils", "Compartilhamento iniciado com sucesso")
                Result.success(Unit)
            } else {
                val error = Exception("Nenhum app disponível para compartilhar PDFs")
                LogUtils.warning("PdfUtils", "Nenhum app encontrado para compartilhar")
                Result.failure(error)
            }
        } catch (e: Exception) {
            LogUtils.error("PdfUtils", "Erro ao compartilhar PDF", e)
            Result.failure(e)
        }
    }
    
    /**
     * Salva PDF usando MediaStore API (Android 10+)
     */
    private fun salvarComMediaStore(
        context: Context,
        pdfBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return try {
            LogUtils.debug("PdfUtils", "🔧 Iniciando MediaStore - fileName: $fileName")
            
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            LogUtils.debug("PdfUtils", "📋 ContentValues criado")
            
            val resolver = context.contentResolver
            LogUtils.debug("PdfUtils", "🔗 ContentResolver obtido")
            
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            LogUtils.debug("PdfUtils", "📍 URI criado: $uri")
            
            uri?.let { fileUri ->
                LogUtils.debug("PdfUtils", "✅ URI válido, abrindo OutputStream...")
                resolver.openOutputStream(fileUri)?.use { outputStream ->
                    LogUtils.debug("PdfUtils", "📝 Escrevendo ${pdfBytes.size} bytes...")
                    outputStream.write(pdfBytes)
                    LogUtils.debug("PdfUtils", "✅ Bytes escritos com sucesso")
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    LogUtils.debug("PdfUtils", "🔄 Atualizando IS_PENDING para 0...")
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(fileUri, values, null, null)
                    LogUtils.debug("PdfUtils", "✅ IS_PENDING atualizado")
                }
                
                LogUtils.debug("PdfUtils", "🎉 PDF salvo com MediaStore: $fileName")
                Result.success("Downloads/$fileName")
            } ?: run {
                LogUtils.error("PdfUtils", "❌ URI é null - falha ao criar arquivo")
                val error = Exception("Não foi possível criar arquivo na pasta Downloads")
                Result.failure(error)
            }
        } catch (e: Exception) {
            LogUtils.error("PdfUtils", "💥 EXCEÇÃO em salvarComMediaStore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Salva PDF usando storage tradicional (Android 9 e inferior)
     */
    private fun salvarComStorageTradicional(
        pdfBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            
            FileOutputStream(file).use { it.write(pdfBytes) }
            
            LogUtils.debug("PdfUtils", "PDF salvo tradicionalmente: ${file.absolutePath}")
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            LogUtils.error("PdfUtils", "Erro ao salvar tradicionalmente", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se a pasta Downloads existe e é acessível
     */
    fun verificarAcessoPastaDownloads(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: MediaStore sempre funciona
                true
            } else {
                // Android 9 e inferior: verificar se a pasta existe
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.exists() || downloadsDir.mkdirs()
            }
        } catch (e: Exception) {
            LogUtils.error("PdfUtils", "Erro ao verificar acesso à pasta Downloads", e)
            false
        }
    }
} 