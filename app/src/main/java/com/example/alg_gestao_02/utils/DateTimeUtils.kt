package com.example.alg_gestao_02.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility para tratamento de datas com TimeZone de Brasília
 */
object DateTimeUtils {
    private const val BRASILIA_TIMEZONE = "America/Sao_Paulo"

    /**
     * Cria um SimpleDateFormat com TimeZone do Brasil
     */
    fun createBRDateFormat(pattern: String): SimpleDateFormat {
        return SimpleDateFormat(pattern, Locale("pt", "BR")).apply {
            timeZone = TimeZone.getTimeZone(BRASILIA_TIMEZONE)
        }
    }

    /**
     * Converte uma string de data ISO para formato brasileiro
     * @param isoDate Data no formato ISO 8601 (ex: "2024-01-15T10:30:00Z")
     * @param outputPattern Padrão de saída (ex: "dd/MM/yyyy HH:mm")
     */
    fun formatarDataISO(isoDate: String?, outputPattern: String = "dd/MM/yyyy"): String {
        if (isoDate.isNullOrEmpty()) return ""

        return try {
            var date: Date? = null
            
            // Se tem Z (UTC marker), parsear como UTC primeiro
            if (isoDate.contains("Z")) {
                // Parsear com timezone UTC
                val patterns = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'"
                )
                
                for (pattern in patterns) {
                    try {
                        val formatter = SimpleDateFormat(pattern, Locale("pt", "BR")).apply {
                            timeZone = TimeZone.getTimeZone("UTC")  // ✅ IMPORTANTE: Parse como UTC
                        }
                        date = formatter.parse(isoDate)
                        if (date != null) break
                    } catch (e: Exception) {
                        continue
                    }
                }
            } else {
                // Se não tem Z, parsear com timezone local BR
                val patterns = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
                )
                
                for (pattern in patterns) {
                    try {
                        val formatter = createBRDateFormat(pattern)
                        date = formatter.parse(isoDate)
                        if (date != null) break
                    } catch (e: Exception) {
                        continue
                    }
                }
            }

            if (date == null) {
                LogUtils.warning("DateTimeUtils", "Não conseguiu parsear data: $isoDate")
                return isoDate
            }

            // Formatter de saída com horário de Brasília ✅
            val outputFormatter = createBRDateFormat(outputPattern)
            outputFormatter.format(date)
        } catch (e: Exception) {
            LogUtils.error("DateTimeUtils", "Erro ao formatar data: ${e.message}", e)
            isoDate
        }
    }

    /**
     * Retorna a data/hora atual no fuso horário de Brasília
     */
    fun getDataHoraAtualBR(): String {
        val formatter = createBRDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return formatter.format(Date())
    }

    /**
     * Converte Date para String ISO com TimeZone BR
     */
    fun dateToISO(date: Date?): String {
        if (date == null) return ""
        val formatter = createBRDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return formatter.format(date)
    }

    /**
     * Normaliza uma data para o TimeZone de Brasília
     */
    fun normalizarDataBR(date: Date): Date {
        val formatter = createBRDateFormat("yyyy-MM-dd' 'HH:mm:ss")
        val formatted = formatter.format(date)
        return SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault()).parse(formatted) ?: date
    }
}
