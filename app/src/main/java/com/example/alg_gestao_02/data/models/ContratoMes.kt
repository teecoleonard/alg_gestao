package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Representa um mês/ano com seus contratos associados
 */
@Parcelize
data class ContratoMes(
    val ano: Int,
    val mes: Int,
    val contratos: List<Contrato> = emptyList()
) : Parcelable {
    
    /**
     * Retorna o nome do mês em português
     */
    fun getNomeMes(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, mes - 1) // Calendar usa 0-11 para meses
        val formato = SimpleDateFormat("MMMM", Locale("pt", "BR"))
        return formato.format(calendar.time).replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Retorna o nome do mês abreviado
     */
    fun getNomeMesAbreviado(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, mes - 1)
        val formato = SimpleDateFormat("MMM", Locale("pt", "BR"))
        return formato.format(calendar.time).replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Retorna a descrição completa do mês/ano
     */
    fun getDescricaoCompleta(): String {
        return "${getNomeMes()} de $ano"
    }
    
    /**
     * Retorna a descrição abreviada do mês/ano
     */
    fun getDescricaoAbreviada(): String {
        return "${getNomeMesAbreviado()}/$ano"
    }
    
    /**
     * Retorna a quantidade de contratos neste mês
     */
    fun getQuantidadeContratos(): Int {
        return contratos.size
    }
    
    /**
     * Retorna o valor total dos contratos deste mês
     */
    fun getValorTotal(): Double {
        return contratos.sumOf { it.getValorEfetivo() }
    }
    
    /**
     * Retorna uma chave única para ordenação (ano * 100 + mês)
     */
    fun getChaveOrdenacao(): Int {
        return ano * 100 + mes
    }
    
    /**
     * Verifica se este mês tem contratos
     */
    fun temContratos(): Boolean {
        return contratos.isNotEmpty()
    }
    
    companion object {
        /**
         * Cria um ContratoMes a partir de uma data
         */
        fun fromDate(date: Date): ContratoMes {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return ContratoMes(
                ano = calendar.get(Calendar.YEAR),
                mes = calendar.get(Calendar.MONTH) + 1 // Calendar usa 0-11
            )
        }
        
        /**
         * Cria um ContratoMes a partir de uma string de data no formato "yyyy-MM-dd"
         */
        fun fromDateString(dateString: String): ContratoMes? {
            return try {
                val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = formato.parse(dateString)
                if (date != null) fromDate(date) else null
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Cria um ContratoMes a partir de uma string de data no formato "yyyy-MM-dd'T'HH:mm:ss"
         */
        fun fromDateTimeString(dateTimeString: String): ContratoMes? {
            return try {
                val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = formato.parse(dateTimeString)
                if (date != null) fromDate(date) else null
            } catch (e: Exception) {
                null
            }
        }
    }
}

