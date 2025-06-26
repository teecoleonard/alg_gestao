package com.example.alg_gestao_02.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.alg_gestao_02.data.models.FinancialMetrics
import com.example.alg_gestao_02.data.models.ProgressMetrics
import com.example.alg_gestao_02.utils.LogUtils
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Serviço para geração de relatórios PDF
 */
class ReportService(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    /**
     * Gera relatório financeiro em PDF
     */
    fun generateFinancialReport(
        financialMetrics: FinancialMetrics,
        progressMetrics: ProgressMetrics,
        dataInicio: Date? = null,
        dataFim: Date? = null
    ): File? {
        return try {
            LogUtils.info("ReportService", "🚀 ========== INICIANDO GERAÇÃO DE RELATÓRIO ==========")
            
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = document.startPage(pageInfo)
            
            val canvas = page.canvas
            val paint = Paint()
            
            // Desenhar conteúdo do relatório
            drawReportContent(canvas, paint, financialMetrics, progressMetrics, dataInicio, dataFim)
            
            document.finishPage(page)
            
            // Salvar arquivo
            val fileName = "relatorio_financeiro_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()
            
            LogUtils.info("ReportService", "✅ Relatório gerado com sucesso: ${file.absolutePath}")
            file
            
        } catch (e: Exception) {
            LogUtils.error("ReportService", "❌ Erro ao gerar relatório: ${e.message}", e)
            null
        }
    }
    
    /**
     * Desenha o conteúdo do relatório no canvas
     */
    private fun drawReportContent(
        canvas: Canvas,
        paint: Paint,
        financialMetrics: FinancialMetrics,
        progressMetrics: ProgressMetrics,
        dataInicio: Date?,
        dataFim: Date?
    ) {
        var yPosition = 80f
        val leftMargin = 50f
        val rightMargin = 545f
        
        // Header
        paint.textSize = 24f
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("📊 RELATÓRIO FINANCEIRO", leftMargin, yPosition, paint)
        yPosition += 40f
        
        // Data de geração
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("Gerado em: ${dateTimeFormat.format(Date())}", leftMargin, yPosition, paint)
        yPosition += 30f
        
        // Período
        val periodo = if (dataInicio != null && dataFim != null) {
            "Período: ${dateFormat.format(dataInicio)} a ${dateFormat.format(dataFim)}"
        } else {
            "Período: Todos os dados"
        }
        canvas.drawText(periodo, leftMargin, yPosition, paint)
        yPosition += 50f
        
        // Linha separadora
        paint.strokeWidth = 2f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
        yPosition += 30f
        
        // Seção: Métricas Principais
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("💰 MÉTRICAS PRINCIPAIS", leftMargin, yPosition, paint)
        yPosition += 40f
        
        paint.textSize = 14f
        paint.isFakeBoldText = false
        
        // Valor Total Ativo
        canvas.drawText("Valor Total Ativo:", leftMargin, yPosition, paint)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#2E7D32") // Verde
        canvas.drawText(currencyFormat.format(financialMetrics.valorTotalAtivo), leftMargin + 200f, yPosition, paint)
        yPosition += 25f
        
        // Receita Mensal
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText("Receita Mensal:", leftMargin, yPosition, paint)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#1976D2") // Azul
        canvas.drawText(currencyFormat.format(financialMetrics.receitaMensal), leftMargin + 200f, yPosition, paint)
        yPosition += 25f
        
        // Ticket Médio
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText("Ticket Médio:", leftMargin, yPosition, paint)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#F57C00") // Laranja
        canvas.drawText(currencyFormat.format(financialMetrics.ticketMedio), leftMargin + 200f, yPosition, paint)
        yPosition += 50f
        
        // Linha separadora
        paint.color = Color.GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
        yPosition += 30f
        
        // Seção: Metas e Progresso
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("🎯 METAS E PROGRESSO", leftMargin, yPosition, paint)
        yPosition += 40f
        
        paint.textSize = 14f
        paint.isFakeBoldText = false
        
        // Meta de Receita
        canvas.drawText("Meta de Receita:", leftMargin, yPosition, paint)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#9C27B0") // Roxo
        canvas.drawText(currencyFormat.format(progressMetrics.receitaMeta), leftMargin + 200f, yPosition, paint)
        yPosition += 25f
        
        // Receita Atual
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText("Receita Atual:", leftMargin, yPosition, paint)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#2E7D32") // Verde
        canvas.drawText(currencyFormat.format(progressMetrics.receitaAtual), leftMargin + 200f, yPosition, paint)
        yPosition += 25f
        
        // Progresso
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText("Progresso:", leftMargin, yPosition, paint)
        paint.isFakeBoldText = true
        val progressColor = when {
            progressMetrics.receitaPercentual >= 90 -> Color.parseColor("#2E7D32") // Verde
            progressMetrics.receitaPercentual >= 70 -> Color.parseColor("#F57C00") // Laranja
            else -> Color.parseColor("#D32F2F") // Vermelho
        }
        paint.color = progressColor
        canvas.drawText("${progressMetrics.receitaPercentual}%", leftMargin + 200f, yPosition, paint)
        yPosition += 25f
        
        // Valor Restante
        val valorRestante = progressMetrics.receitaMeta - progressMetrics.receitaAtual
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText("Restante para Meta:", leftMargin, yPosition, paint)
        paint.isFakeBoldText = true
        paint.color = if (valorRestante > 0) Color.parseColor("#D32F2F") else Color.parseColor("#2E7D32")
        canvas.drawText(currencyFormat.format(maxOf(0.0, valorRestante)), leftMargin + 200f, yPosition, paint)
        yPosition += 50f
        
        // Desenhar barra de progresso
        drawProgressBar(canvas, paint, leftMargin, yPosition, 400f, 20f, progressMetrics.receitaPercentual)
        yPosition += 50f
        
        // Linha separadora
        paint.color = Color.GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
        yPosition += 30f
        
        // Seção: Análise
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("📈 ANÁLISE", leftMargin, yPosition, paint)
        yPosition += 40f
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        
        // Status da meta
        val statusMeta = if (progressMetrics.receitaPercentual >= 100) {
            "✅ Meta atingida! Parabéns pela excelente performance."
        } else {
            "⚠️ Meta ainda não atingida. Faltam ${100 - progressMetrics.receitaPercentual}% para completar."
        }
        drawMultilineText(canvas, paint, statusMeta, leftMargin, yPosition, rightMargin - leftMargin)
        yPosition += 40f
        
        // Análise do ticket médio
        val ticketAnalise = if (financialMetrics.ticketMedio > 3000) {
            "💎 Ticket médio alto (${currencyFormat.format(financialMetrics.ticketMedio)}). Excelente valor por contrato!"
        } else {
            "📊 Ticket médio de ${currencyFormat.format(financialMetrics.ticketMedio)}. Considere estratégias para aumentar o valor médio."
        }
        drawMultilineText(canvas, paint, ticketAnalise, leftMargin, yPosition, rightMargin - leftMargin)
        yPosition += 40f
        
        // Tendência mensal
        val tendencia = if (financialMetrics.receitaMensal > financialMetrics.valorTotalAtivo * 0.1) {
            "📈 Crescimento acelerado! Receita mensal representa mais de 10% do valor total."
        } else {
            "📊 Crescimento estável. Receita mensal dentro dos padrões esperados."
        }
        drawMultilineText(canvas, paint, tendencia, leftMargin, yPosition, rightMargin - leftMargin)
        yPosition += 60f
        
        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Relatório gerado automaticamente pelo ALG Gestão", leftMargin, yPosition, paint)
        canvas.drawText("© 2025 ALG Gestão - Todos os direitos reservados", leftMargin, yPosition + 15f, paint)
        
        // Espaço final para melhor apresentação
        yPosition += 50f
    }
    
    /**
     * Desenha uma barra de progresso
     */
    private fun drawProgressBar(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        percentage: Int
    ) {
        // Fundo da barra
        paint.color = Color.parseColor("#E0E0E0")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(x, y, x + width, y + height, height / 2, height / 2, paint)
        
        // Progresso
        val progressWidth = (width * percentage) / 100f
        val progressColor = when {
            percentage >= 90 -> Color.parseColor("#2E7D32") // Verde
            percentage >= 70 -> Color.parseColor("#F57C00") // Laranja
            else -> Color.parseColor("#D32F2F") // Vermelho
        }
        paint.color = progressColor
        canvas.drawRoundRect(x, y, x + progressWidth, y + height, height / 2, height / 2, paint)
        
        // Texto do percentual
        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("$percentage%", x + progressWidth / 2, y + height / 2 + 3f, paint)
        paint.textAlign = Paint.Align.LEFT
    }
    
    /**
     * Desenha texto multilinha
     */
    private fun drawMultilineText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var currentY = y
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val textWidth = paint.measureText(testLine)
            
            if (textWidth <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += paint.textSize + 5f
                }
                currentLine = word
            }
        }
        
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
            currentY += paint.textSize + 5f
        }
        
        return currentY
    }
} 