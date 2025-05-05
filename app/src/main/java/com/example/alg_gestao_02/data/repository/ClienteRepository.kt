package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.CancellationException

/**
 * Repositório para gerenciar operações com Clientes
 */
class ClienteRepository {
    private val apiService = ApiClient.apiService
    
    /**
     * Busca todos os clientes
     */
    suspend fun getClientes(): Resource<List<Cliente>> {
        return try {
            LogUtils.debug("ClienteRepository", "Buscando todos os clientes")
            val response = apiService.getClientes()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ClienteRepository", "Falha ao buscar clientes: ${response.code()}")
                Resource.Error("Erro ao buscar clientes: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ClienteRepository", "Operação de busca de clientes cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ClienteRepository", "Erro ao buscar clientes", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Busca um cliente pelo ID
     */
    suspend fun getClienteById(id: Int): Resource<Cliente> {
        return try {
            LogUtils.debug("ClienteRepository", "Buscando cliente com ID: $id")
            val response = apiService.getClienteById(id)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ClienteRepository", "Falha ao buscar cliente: ${response.code()}")
                Resource.Error("Erro ao buscar cliente: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ClienteRepository", "Operação de busca de cliente cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ClienteRepository", "Erro ao buscar cliente", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Cria um novo cliente
     */
    suspend fun createCliente(cliente: Cliente): Resource<Cliente> {
        return try {
            LogUtils.debug("ClienteRepository", "Criando novo cliente: ${cliente.contratante}")
            val response = apiService.createCliente(cliente)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ClienteRepository", "Falha ao criar cliente: ${response.code()}")
                Resource.Error("Erro ao criar cliente: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ClienteRepository", "Operação de criação de cliente cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ClienteRepository", "Erro ao criar cliente", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Atualiza um cliente existente
     */
    suspend fun updateCliente(id: Int, cliente: Cliente): Resource<Cliente> {
        return try {
            LogUtils.debug("ClienteRepository", "Atualizando cliente com ID: $id")
            val response = apiService.updateCliente(id, cliente)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ClienteRepository", "Falha ao atualizar cliente: ${response.code()}")
                Resource.Error("Erro ao atualizar cliente: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ClienteRepository", "Operação de atualização de cliente cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ClienteRepository", "Erro ao atualizar cliente", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Exclui um cliente
     */
    suspend fun deleteCliente(id: Int): Resource<Boolean> {
        return try {
            LogUtils.debug("ClienteRepository", "Excluindo cliente com ID: $id")
            val response = apiService.deleteCliente(id)
            
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                LogUtils.warning("ClienteRepository", "Falha ao excluir cliente: ${response.code()}")
                Resource.Error("Erro ao excluir cliente: ${response.message()}")
            }
        } catch (e: CancellationException) {
            // Tratamento específico para cancelamento de job
            LogUtils.debug("ClienteRepository", "Operação de exclusão de cliente cancelada")
            Resource.Error("Operação cancelada")
        } catch (e: Exception) {
            LogUtils.error("ClienteRepository", "Erro ao excluir cliente", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
} 