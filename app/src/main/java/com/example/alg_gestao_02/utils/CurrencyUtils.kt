package com.example.alg_gestao_02.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Formata um valor para o formato de moeda brasileira
 */
fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(value)
}

/**
 * Tenta converter uma string de moeda para um valor Double
 */
fun parseCurrencyToDouble(currencyString: String): Double {
    // Remove símbolos de moeda e formatação
    val cleanString = currencyString
        .replace("[R$]".toRegex(), "")
        .replace("\\s".toRegex(), "")
        .replace("\\.", "")
        .replace(",", ".")
        .trim()
    
    return try {
        cleanString.toDouble()
    } catch (e: Exception) {
        0.0
    }
}