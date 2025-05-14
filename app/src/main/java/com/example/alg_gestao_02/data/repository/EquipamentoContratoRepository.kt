package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

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
            
            // Fazer a chamada à API e obter a resposta bruta primeiro
            val response = apiService.getEquipamentosContrato(contratoId)
            
            if (response.isSuccessful) {
                val equipamentosApi = response.body()
                
                if (equipamentosApi != null) {
                    LogUtils.debug("EquipamentoContratoRepository", 
                        "Equipamentos recebidos: ${equipamentosApi.size}")
                    
                    val equipamentosProcessados = processarEquipamentosAninhados(equipamentosApi)
                    Resource.Success(equipamentosProcessados)
                } else {
                    LogUtils.debug("EquipamentoContratoRepository", "Resposta vazia de equipamentos")
                    Resource.Success(emptyList())
                }
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
    
    /**
     * Processa os objetos EquipamentoContrato para extrair dados do equipamento aninhado
     */
    private suspend fun processarEquipamentosAninhados(equipamentos: List<EquipamentoContrato>): List<EquipamentoContrato> {
        return equipamentos.map { equipamentoContrato ->
            // Verificar se o nome já está disponível
            if (equipamentoContrato.equipamentoNome != null) {
                // Se o nome já está definido, manter como está
                LogUtils.debug("EquipamentoContratoRepository", 
                    "Equipamento já tem nome definido - ID: ${equipamentoContrato.id}, " +
                    "Nome: ${equipamentoContrato.equipamentoNome}")
                equipamentoContrato
            } else if (equipamentoContrato.equipamento != null) {
                // Se o objeto equipamento está disponível, usar seu nome
                LogUtils.debug("EquipamentoContratoRepository", 
                    "Processando equipamento com objeto aninhado presente - ID: ${equipamentoContrato.id}, " +
                    "Nome original: ${equipamentoContrato.equipamento.nomeEquip}")
                
                equipamentoContrato.copy(
                    equipamentoNome = equipamentoContrato.equipamento.nomeEquip
                )
            } else {
                // Se não tem nome nem objeto equipamento, buscar pelo ID
                LogUtils.debug("EquipamentoContratoRepository", 
                    "Equipamento sem nome e sem objeto aninhado - ID: ${equipamentoContrato.id}, " +
                    "Buscando detalhes para equipamento_id: ${equipamentoContrato.equipamentoId}")
                
                val equipamentoDetalhes = obterDetalhesEquipamento(equipamentoContrato.equipamentoId)
                
                if (equipamentoDetalhes != null) {
                    LogUtils.debug("EquipamentoContratoRepository", 
                        "Detalhes do equipamento obtidos com sucesso - ID: ${equipamentoContrato.equipamentoId}, " +
                        "Nome: ${equipamentoDetalhes.nomeEquip}")
                    
                    // Atualizar o objeto com o nome correto
                    equipamentoContrato.copy(
                        equipamentoNome = equipamentoDetalhes.nomeEquip
                    )
                } else {
                    LogUtils.warning("EquipamentoContratoRepository", 
                        "Não foi possível obter detalhes do equipamento - ID: ${equipamentoContrato.equipamentoId}")
                    // Manter como está se não conseguir obter os detalhes
                    equipamentoContrato
                }
            }
        }
    }
    
    /**
     * Método para obter detalhes do equipamento quando não estiverem disponíveis na resposta principal
     */
    private suspend fun obterDetalhesEquipamento(equipamentoId: Int): Equipamento? {
        return try {
            val response = apiService.getEquipamentoById(equipamentoId)
            if (response.isSuccessful) {
                response.body()
            } else {
                LogUtils.warning("EquipamentoContratoRepository", 
                    "Não foi possível obter detalhes do equipamento ID: $equipamentoId")
                null
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoContratoRepository", 
                "Erro ao obter detalhes do equipamento ID: $equipamentoId", e)
            null
        }
    }
}
