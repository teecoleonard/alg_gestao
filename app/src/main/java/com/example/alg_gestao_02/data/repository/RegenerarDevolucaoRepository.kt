package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para gerenciar regeneração de devoluções
 */
class RegenerarDevolucaoRepository {
    
    private val apiService = ApiClient.apiService
    
    /**
     * Verifica se um contrato precisa regenerar devoluções
     */
    suspend fun verificarNecessidadeRegeneracao(contratoId: Int): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                LogUtils.debug("RegenerarDevolucaoRepository", "🔍 Verificando necessidade de regeneração para contrato: $contratoId")
                
                val response = apiService.verificarNecessidadeRegeneracao(contratoId)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    val precisaRegenerar = responseBody["precisaRegenerar"] as? Boolean ?: false
                    
                    LogUtils.debug("RegenerarDevolucaoRepository", "✅ Verificação concluída: precisaRegenerar = $precisaRegenerar")
                    
                    Resource.Success(precisaRegenerar)
                } else {
                    val errorMsg = "Erro ao verificar necessidade de regeneração: ${response.code()}"
                    LogUtils.error("RegenerarDevolucaoRepository", errorMsg)
                    Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erro de conexão ao verificar regeneração: ${e.message}"
                LogUtils.error("RegenerarDevolucaoRepository", errorMsg, e)
                Resource.Error(errorMsg)
            }
        }
    }
    
    /**
     * Regenera as devoluções de um contrato
     */
    suspend fun regenerarDevolucoes(contratoId: Int): Resource<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                LogUtils.debug("RegenerarDevolucaoRepository", "🔄 Iniciando regeneração de devoluções para contrato: $contratoId")
                
                val response = apiService.regenerarDevolucoes(contratoId)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    val success = responseBody["success"] as? Boolean ?: false
                    
                    if (success) {
                        val data = responseBody["data"] as? Map<String, Any>
                        val novasDevolucoesCount = data?.get("novasDevolucoesS") as? Double ?: 0.0
                        val obsoletasCount = data?.get("devolucoesSObsoletas") as? Double ?: 0.0
                        
                        LogUtils.debug("RegenerarDevolucaoRepository", "✅ Regeneração concluída:")
                        LogUtils.debug("RegenerarDevolucaoRepository", "  - Devoluções obsoletas: ${obsoletasCount.toInt()}")
                        LogUtils.debug("RegenerarDevolucaoRepository", "  - Novas devoluções: ${novasDevolucoesCount.toInt()}")
                        
                        Resource.Success(responseBody)
                    } else {
                        val message = responseBody["message"]?.toString() ?: "Erro desconhecido"
                        LogUtils.error("RegenerarDevolucaoRepository", "❌ Falha na regeneração: $message")
                        Resource.Error(message)
                    }
                } else {
                    val errorMsg = "Erro ao regenerar devoluções: ${response.code()}"
                    LogUtils.error("RegenerarDevolucaoRepository", errorMsg)
                    Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erro de conexão ao regenerar devoluções: ${e.message}"
                LogUtils.error("RegenerarDevolucaoRepository", errorMsg, e)
                Resource.Error(errorMsg)
            }
        }
    }
}
