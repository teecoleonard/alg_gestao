package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.data.api.ApiService
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para gerenciar as operações de API relacionadas a devoluções.
 */
class DevolucaoRepository(private val apiService: ApiService) {

    /**
     * Obtém a lista de devoluções com base nos filtros fornecidos
     */
    suspend fun getDevolucoes(
        status: String? = null,
        clienteId: Int? = null,
        contratoId: Int? = null,
        devNum: String? = null,
        dataPrevistaInicio: String? = null,
        dataPrevistaFim: String? = null
    ): Resource<List<Devolucao>> = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("DevolucaoRepository", "Buscando devoluções com filtros: " +
                    "status=$status, clienteId=$clienteId, contratoId=$contratoId, devNum=$devNum")
            
            val response = apiService.getDevolucoes(
                status = status,
                clienteId = clienteId,
                contratoId = contratoId,
                devNum = devNum,
                dataPrevistaInicio = dataPrevistaInicio,
                dataPrevistaFim = dataPrevistaFim
            )
            
            if (response.isSuccessful) {
                val devolucoes = response.body() ?: emptyList()
                LogUtils.debug("DevolucaoRepository", "Devoluções encontradas: ${devolucoes.size}")
                Resource.Success(devolucoes)
            } else {
                val errorMessage = "Erro ao carregar devoluções: ${response.message()}"
                LogUtils.error("DevolucaoRepository", errorMessage)
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Erro ao carregar devoluções: ${e.message}"
            LogUtils.error("DevolucaoRepository", errorMessage, e)
            Resource.Error(errorMessage)
        }
    }

    /**
     * Obtém os detalhes de uma devolução específica pelo ID
     */
    suspend fun getDevolucaoById(id: Int): Resource<Devolucao> = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("DevolucaoRepository", "Buscando devolução com ID: $id")
            
            val response = apiService.getDevolucaoById(id)
            
            if (response.isSuccessful) {
                val devolucao = response.body()
                if (devolucao != null) {
                    LogUtils.debug("DevolucaoRepository", "Devolução encontrada: ID=${devolucao.id}")
                    Resource.Success(devolucao)
                } else {
                    val errorMessage = "Devolução não encontrada"
                    LogUtils.error("DevolucaoRepository", errorMessage)
                    Resource.Error(errorMessage)
                }
            } else {
                val errorMessage = "Erro ao carregar devolução: ${response.message()}"
                LogUtils.error("DevolucaoRepository", errorMessage)
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Erro ao carregar devolução: ${e.message}"
            LogUtils.error("DevolucaoRepository", errorMessage, e)
            Resource.Error(errorMessage)
        }
    }

    /**
     * Obtém as devoluções associadas a um contrato específico
     */
    suspend fun getDevolucoesByContratoId(contratoId: Int): Resource<List<Devolucao>> = 
        withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("DevolucaoRepository", "Buscando devoluções do contrato ID: $contratoId")
            
            val response = apiService.getDevolucoesByContratoId(contratoId)
            
            if (response.isSuccessful) {
                val devolucoes = response.body() ?: emptyList()
                LogUtils.debug("DevolucaoRepository", 
                    "Devoluções do contrato $contratoId encontradas: ${devolucoes.size}")
                Resource.Success(devolucoes)
            } else {
                val errorMessage = "Erro ao carregar devoluções do contrato: ${response.message()}"
                LogUtils.error("DevolucaoRepository", errorMessage)
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Erro ao carregar devoluções do contrato: ${e.message}"
            LogUtils.error("DevolucaoRepository", errorMessage, e)
            Resource.Error(errorMessage)
        }
    }

    /**
     * Obtém as devoluções agrupadas por um número de devolução específico
     */
    suspend fun getDevolucoesByDevNum(devNum: String): Resource<List<Devolucao>> = 
        withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("DevolucaoRepository", "Buscando devoluções por dev_num: $devNum")
            
            val response = apiService.getDevolucoesByDevNum(devNum)
            
            if (response.isSuccessful) {
                val devolucoes = response.body() ?: emptyList()
                LogUtils.debug("DevolucaoRepository", 
                    "Devoluções com dev_num $devNum encontradas: ${devolucoes.size}")
                Resource.Success(devolucoes)
            } else {
                val errorMessage = "Erro ao carregar devoluções por dev_num: ${response.message()}"
                LogUtils.error("DevolucaoRepository", errorMessage)
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Erro ao carregar devoluções por dev_num: ${e.message}"
            LogUtils.error("DevolucaoRepository", errorMessage, e)
            Resource.Error(errorMessage)
        }
    }

    /**
     * Atualiza/processa um item de devolução existente
     */
    suspend fun processarDevolucao(
        id: Int,
        quantidadeDevolvida: Int? = null,
        statusItemDevolucao: String? = null,
        dataDevolucaoEfetiva: String? = null,
        observacaoItemDevolucao: String? = null
    ): Resource<Devolucao> = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("DevolucaoRepository", "Processando devolução ID: $id, " +
                "quantidade: $quantidadeDevolvida, status: $statusItemDevolucao")
            
            // Criar objeto de request
            val requestData = ApiService.DevolucaoUpdateRequest(
                quantidadeDevolvida = quantidadeDevolvida,
                statusItemDevolucao = statusItemDevolucao,
                dataDevolucaoEfetiva = dataDevolucaoEfetiva,
                observacaoItemDevolucao = observacaoItemDevolucao
            )
            
            val response = apiService.updateDevolucao(id, requestData)
            
            if (response.isSuccessful) {
                val devolucaoAtualizada = response.body()?.devolucao
                if (devolucaoAtualizada != null) {
                    LogUtils.debug("DevolucaoRepository", 
                        "Devolução processada com sucesso: ID=${devolucaoAtualizada.id}")
                    Resource.Success(devolucaoAtualizada)
                } else {
                    val errorMessage = "Resposta vazia ao processar devolução"
                    LogUtils.error("DevolucaoRepository", errorMessage)
                    Resource.Error(errorMessage)
                }
            } else {
                val errorMessage = "Erro ao processar devolução: ${response.message()}"
                LogUtils.error("DevolucaoRepository", errorMessage)
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Erro ao processar devolução: ${e.message}"
            LogUtils.error("DevolucaoRepository", errorMessage, e)
            Resource.Error(errorMessage)
        }
    }
}
