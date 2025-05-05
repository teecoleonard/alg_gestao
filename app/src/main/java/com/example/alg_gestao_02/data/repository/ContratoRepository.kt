package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositório para gerenciar operações com Contratos
 */
class ContratoRepository {
    private val apiService = ApiClient.apiService
    
    /**
     * Busca todos os contratos
     */
    suspend fun getContratos(): Resource<List<Contrato>> {
        return try {
            LogUtils.debug("ContratoRepository", "Buscando todos os contratos")
            val response = apiService.getContratos()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao buscar contratos: ${response.code()}")
                Resource.Error("Erro ao buscar contratos: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ContratoRepository", "Operação de busca de contratos cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ContratoRepository", "Erro ao buscar contratos", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Busca um contrato pelo ID
     */
    suspend fun getContratoById(id: Int): Resource<Contrato> {
        return try {
            LogUtils.debug("ContratoRepository", "Buscando contrato com ID: $id")
            val response = apiService.getContratoById(id)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao buscar contrato: ${response.code()}")
                Resource.Error("Erro ao buscar contrato: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ContratoRepository", "Operação de busca de contrato cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ContratoRepository", "Erro ao buscar contrato", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Busca contratos por cliente
     */
    suspend fun getContratosByCliente(clienteId: Int): Resource<List<Contrato>> {
        return try {
            LogUtils.debug("ContratoRepository", "Buscando contratos do cliente: $clienteId")
            val response = apiService.getContratosByCliente(clienteId)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao buscar contratos do cliente: ${response.code()}")
                Resource.Error("Erro ao buscar contratos do cliente: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ContratoRepository", "Operação de busca de contratos do cliente cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ContratoRepository", "Erro ao buscar contratos do cliente", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Cria um novo contrato
     */
    suspend fun createContrato(contrato: Contrato): Resource<Contrato> {
        return try {
            LogUtils.debug("ContratoRepository", "Criando novo contrato para cliente: ${contrato.clienteId}")
            val response = apiService.createContrato(contrato)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao criar contrato: ${response.code()}")
                Resource.Error("Erro ao criar contrato: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ContratoRepository", "Operação de criação de contrato cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ContratoRepository", "Erro ao criar contrato", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Atualiza um contrato existente
     */
    suspend fun updateContrato(id: Int, contrato: Contrato): Resource<Contrato> {
        return try {
            LogUtils.debug("ContratoRepository", "Atualizando contrato com ID: $id")
            val response = apiService.updateContrato(id, contrato)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao atualizar contrato: ${response.code()}")
                Resource.Error("Erro ao atualizar contrato: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ContratoRepository", "Operação de atualização de contrato cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ContratoRepository", "Erro ao atualizar contrato", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Exclui um contrato
     */
    suspend fun deleteContrato(id: Int): Resource<Boolean> {
        return try {
            LogUtils.debug("ContratoRepository", "Excluindo contrato com ID: $id")
            val response = apiService.deleteContrato(id)
            
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao excluir contrato: ${response.code()}")
                Resource.Error("Erro ao excluir contrato: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ContratoRepository", "Operação de exclusão de contrato cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ContratoRepository", "Erro ao excluir contrato", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Gera o próximo número de contrato para um cliente
     */
    suspend fun getNextContratoNum(clienteId: Int): String {
        val contratos = when (val result = getContratosByCliente(clienteId)) {
            is Resource.Success -> result.data
            else -> emptyList()
        }
        
        val contratoNums = contratos.map { 
            // Pega apenas os dígitos do número do contrato
            val numerico = """\d+""".toRegex().find(it.contratoNum)?.value
            numerico?.toIntOrNull() ?: 0
        }
        
        val proximoNum = if (contratoNums.isEmpty()) 1 else contratoNums.maxOrNull()!! + 1
        
        // Formata o número com 3 dígitos
        return String.format("%03d", proximoNum)
    }
    
    /**
     * Gera a data atual formatada para o padrão ISO 8601
     */
    fun getDataHoraAtual(): String {
        val formatoData = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return formatoData.format(Date())
    }
    
    /**
     * Gera a data de vencimento (atual + 30 dias) formatada
     */
    fun getDataVencimento(): String {
        val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 30) // 30 dias a partir de hoje
        return formatoData.format(calendar.time)
    }
} 