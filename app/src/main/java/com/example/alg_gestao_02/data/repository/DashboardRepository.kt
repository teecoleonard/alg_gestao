package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.DashboardStats
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Repositório para gerenciar dados de estatísticas do dashboard
 */
class DashboardRepository {

    private val apiService = ApiClient.apiService

    /**
     * Busca estatísticas do dashboard
     * @return DashboardStats com contadores de contratos, clientes, equipamentos e devoluções
     */
    suspend fun getDashboardStats(): DashboardStats {
        LogUtils.debug("DashboardRepository", "Buscando estatísticas do dashboard")
        
        try {
            val response = apiService.getDashboardStats()
            
            if (response.isSuccessful) {
                val stats = response.body()
                if (stats != null) {
                    LogUtils.debug("DashboardRepository", "Estatísticas obtidas com sucesso: contratos=${stats.contratos}, clientes=${stats.clientes}, equipamentos=${stats.equipamentos}, devoluções=${stats.devolucoes}")
                    return stats
                } else {
                    LogUtils.error("DashboardRepository", "Resposta da API é nula")
                    throw Exception("Resposta da API é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "Erro na API: ${response.code()} - ${response.message()}")
                throw Exception("Erro ao buscar estatísticas: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("DashboardRepository", "Erro ao buscar estatísticas do dashboard: ${e.message}")
            throw e
        }
    }
} 