package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource

/**
 * Repositório para gerenciar operações com Equipamentos
 */
class EquipamentoRepository {
    private val apiService = ApiClient.apiService
    
    /**
     * Busca todos os equipamentos
     */
    suspend fun getEquipamentos(): Resource<List<Equipamento>> {
        return try {
            LogUtils.debug("EquipamentoRepository", "Buscando todos os equipamentos")
            val response = apiService.getEquipamentos()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("EquipamentoRepository", "Falha ao buscar equipamentos: ${response.code()}")
                Resource.Error("Erro ao buscar equipamentos: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoRepository", "Erro ao buscar equipamentos", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Busca equipamentos disponíveis
     */
    suspend fun getEquipamentosDisponiveis(): Resource<List<Equipamento>> {
        return try {
            LogUtils.debug("EquipamentoRepository", "Buscando equipamentos disponíveis")
            val response = apiService.getEquipamentosDisponiveis()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("EquipamentoRepository", "Falha ao buscar equipamentos disponíveis: ${response.code()}")
                Resource.Error("Erro ao buscar equipamentos disponíveis: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoRepository", "Erro ao buscar equipamentos disponíveis", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Busca um equipamento pelo ID
     */
    suspend fun getEquipamentoById(id: Int): Resource<Equipamento> {
        return try {
            LogUtils.debug("EquipamentoRepository", "Buscando equipamento com ID: $id")
            val response = apiService.getEquipamentoById(id)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("EquipamentoRepository", "Falha ao buscar equipamento: ${response.code()}")
                Resource.Error("Erro ao buscar equipamento: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoRepository", "Erro ao buscar equipamento", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Cria um novo equipamento
     */
    suspend fun createEquipamento(equipamento: Equipamento): Resource<Equipamento> {
        return try {
            LogUtils.debug("EquipamentoRepository", "Criando novo equipamento: ${equipamento.nomeEquip}")
            val response = apiService.createEquipamento(equipamento)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("EquipamentoRepository", "Falha ao criar equipamento: ${response.code()}")
                Resource.Error("Erro ao criar equipamento: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoRepository", "Erro ao criar equipamento", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Atualiza um equipamento existente
     */
    suspend fun updateEquipamento(id: Int, equipamento: Equipamento): Resource<Equipamento> {
        return try {
            LogUtils.debug("EquipamentoRepository", "Atualizando equipamento com ID: $id")
            val response = apiService.updateEquipamento(id, equipamento)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("EquipamentoRepository", "Falha ao atualizar equipamento: ${response.code()}")
                Resource.Error("Erro ao atualizar equipamento: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoRepository", "Erro ao atualizar equipamento", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Exclui um equipamento
     */
    suspend fun deleteEquipamento(id: Int): Resource<Boolean> {
        return try {
            LogUtils.debug("EquipamentoRepository", "Excluindo equipamento com ID: $id")
            val response = apiService.deleteEquipamento(id)
            
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                LogUtils.warning("EquipamentoRepository", "Falha ao excluir equipamento: ${response.code()}")
                Resource.Error("Erro ao excluir equipamento: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoRepository", "Erro ao excluir equipamento", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
} 