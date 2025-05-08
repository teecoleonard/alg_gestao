package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource

/**
 * Repositório para consultar equipamentos de contratos
 * Modificações devem ser feitas através do ContratoRepository
 */
class EquipamentoContratoRepository {
    private val apiService = ApiClient.apiService
    
    /**
     * Busca todos os equipamentos de um contrato
     */
    suspend fun getEquipamentosContrato(contratoId: Int): Resource<List<EquipamentoContrato>> {
        return try {
            LogUtils.debug("EquipamentoContratoRepository", "Buscando equipamentos do contrato: $contratoId")
            val response = apiService.getEquipamentosContrato(contratoId)
            
            if (response.isSuccessful) {
                response.body()?.let { equipamentos ->
                    LogUtils.debug("EquipamentoContratoRepository", 
                        "Equipamentos recebidos: ${equipamentos.size}")
                    Resource.Success(equipamentos)
                } ?: Resource.Success(emptyList())
            } else if (response.code() == 404) {
                LogUtils.info("EquipamentoContratoRepository", "Contrato não encontrado: $contratoId")
                Resource.Success(emptyList())
            } else {
                LogUtils.warning("EquipamentoContratoRepository", 
                    "Falha ao buscar equipamentos: ${response.code()}")
                Resource.Error("Erro ao buscar equipamentos: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoContratoRepository", "Erro ao buscar equipamentos", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
}
