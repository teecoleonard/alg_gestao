package com.example.alg_gestao_02.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Classe utilitária para analisar logs de resposta JSON
 */
object LogAnalyzer {

    /**
     * Analisa uma resposta JSON e extrai o ID do contrato, se presente
     */
    fun extractContractIdFromResponse(jsonResponse: String): Int {
        try {
            // Usar JsonParser para extrair o objeto 'contrato' da resposta
            val jsonElement = JsonParser().parse(jsonResponse)
            if (jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject
                
                // Verificar se existe um objeto "contrato" na resposta
                if (jsonObject.has("contrato") && jsonObject.get("contrato").isJsonObject) {
                    val contratoObj = jsonObject.getAsJsonObject("contrato")
                    
                    // Extrair o ID do contrato
                    if (contratoObj.has("id")) {
                        val idElement = contratoObj.get("id")
                        if (!idElement.isJsonNull) {
                            return idElement.asInt
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.error("LogAnalyzer", "Erro ao analisar resposta JSON", e)
        }
        
        return 0 // Retornar 0 se não conseguir extrair o ID
    }
    
    /**
     * Imprime informações detalhadas sobre uma resposta JSON para depuração
     */
    fun logJsonResponseDetails(tag: String, jsonResponse: String) {
        try {
            val jsonElement = JsonParser().parse(jsonResponse)
            if (jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject
                
                val keys = jsonObject.keySet()
                LogUtils.debug(tag, "JSON Response keys: $keys")
                
                if (jsonObject.has("contrato")) {
                    val contratoObj = jsonObject.getAsJsonObject("contrato")
                    val contratoKeys = contratoObj.keySet()
                    LogUtils.debug(tag, "Contrato object keys: $contratoKeys")
                    
                    if (contratoObj.has("id")) {
                        LogUtils.debug(tag, "Contrato ID: ${contratoObj.get("id")}")
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.error(tag, "Erro ao analisar detalhes da resposta JSON", e)
        }
    }
} 