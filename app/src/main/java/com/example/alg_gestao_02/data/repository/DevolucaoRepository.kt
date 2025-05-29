package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.data.api.ApiService
import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para gerenciar as operações de API relacionadas a devoluções.
 */
class DevolucaoRepository(private val apiService: ApiService = ApiClient.apiService) {

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
            LogUtils.info("DevolucaoRepository", "=== INICIANDO PROCESSAMENTO DEVOLUÇÃO ===")
            LogUtils.debug("DevolucaoRepository", "Processando devolução ID: $id, " +
                "quantidade: $quantidadeDevolvida, status: $statusItemDevolucao")
            LogUtils.debug("DevolucaoRepository", "Data efetiva: $dataDevolucaoEfetiva")
            LogUtils.debug("DevolucaoRepository", "Observação: $observacaoItemDevolucao")
            
            // Criar objeto de request
            val requestData = ApiService.DevolucaoUpdateRequest(
                quantidadeDevolvida = quantidadeDevolvida,
                statusItemDevolucao = statusItemDevolucao,
                dataDevolucaoEfetiva = dataDevolucaoEfetiva,
                observacaoItemDevolucao = observacaoItemDevolucao
            )
            
            LogUtils.debug("DevolucaoRepository", "Dados da requisição preparados: $requestData")
            LogUtils.info("DevolucaoRepository", "Enviando requisição PUT para API...")
            
            val response = apiService.updateDevolucao(id, requestData)
            
            LogUtils.debug("DevolucaoRepository", "Resposta recebida da API: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                LogUtils.info("DevolucaoRepository", "Resposta da API foi bem-sucedida")
                val responseBody = response.body()
                LogUtils.debug("DevolucaoRepository", "Response body existe: ${responseBody != null}")
                
                val devolucaoAtualizada = responseBody?.devolucao
                if (devolucaoAtualizada != null) {
                    LogUtils.info("DevolucaoRepository", 
                        "✅ Devolução processada com sucesso: ID=${devolucaoAtualizada.id}, Status=${devolucaoAtualizada.statusItemDevolucao}")
                    LogUtils.debug("DevolucaoRepository", "=== PROCESSAMENTO CONCLUÍDO COM SUCESSO ===")
                    Resource.Success(devolucaoAtualizada)
                } else {
                    val errorMessage = "Resposta vazia ao processar devolução - response.body().devolucao é null"
                    LogUtils.error("DevolucaoRepository", "❌ $errorMessage")
                    LogUtils.error("DevolucaoRepository", "Response body completo: $responseBody")
                    LogUtils.debug("DevolucaoRepository", "=== PROCESSAMENTO FALHOU (RESPOSTA VAZIA) ===")
                    Resource.Error(errorMessage)
                }
            } else {
                val errorMessage = "Erro ao processar devolução: HTTP ${response.code()} - ${response.message()}"
                LogUtils.error("DevolucaoRepository", "❌ $errorMessage")
                try {
                    val errorBody = response.errorBody()?.string()
                    LogUtils.error("DevolucaoRepository", "Corpo do erro: $errorBody")
                } catch (e: Exception) {
                    LogUtils.error("DevolucaoRepository", "Erro ao ler corpo da resposta de erro: ${e.message}")
                }
                LogUtils.debug("DevolucaoRepository", "=== PROCESSAMENTO FALHOU (ERRO HTTP) ===")
                Resource.Error(errorMessage)
            }
        } catch (e: java.net.SocketTimeoutException) {
            val errorMessage = "Timeout ao processar devolução - servidor não respondeu em 30s"
            LogUtils.error("DevolucaoRepository", "❌ $errorMessage", e)
            LogUtils.debug("DevolucaoRepository", "=== PROCESSAMENTO FALHOU (TIMEOUT) ===")
            Resource.Error(errorMessage)
        } catch (e: java.net.ConnectException) {
            val errorMessage = "Erro de conexão ao processar devolução - servidor inacessível"
            LogUtils.error("DevolucaoRepository", "❌ $errorMessage", e)
            LogUtils.debug("DevolucaoRepository", "=== PROCESSAMENTO FALHOU (CONEXÃO) ===")
            Resource.Error(errorMessage)
        } catch (e: retrofit2.HttpException) {
            val errorMessage = "Erro HTTP ao processar devolução: ${e.code()} - ${e.message()}"
            LogUtils.error("DevolucaoRepository", "❌ $errorMessage", e)
            try {
                val errorBody = e.response()?.errorBody()?.string()
                LogUtils.error("DevolucaoRepository", "Detalhes do erro HTTP: $errorBody")
            } catch (ex: Exception) {
                LogUtils.error("DevolucaoRepository", "Erro ao ler detalhes do erro HTTP: ${ex.message}")
            }
            LogUtils.debug("DevolucaoRepository", "=== PROCESSAMENTO FALHOU (HTTP EXCEPTION) ===")
            Resource.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Erro inesperado ao processar devolução: ${e.message}"
            LogUtils.error("DevolucaoRepository", "❌ $errorMessage", e)
            LogUtils.error("DevolucaoRepository", "Tipo da exceção: ${e.javaClass.simpleName}")
            LogUtils.debug("DevolucaoRepository", "=== PROCESSAMENTO FALHOU (ERRO INESPERADO) ===")
            Resource.Error(errorMessage)
        }
    }

    /**
     * Busca devoluções para um contrato específico (retorna lista diretamente)
     */
    suspend fun getDevolucoesByContratoIdList(contratoId: Int): List<Devolucao> {
        return try {
            LogUtils.debug("DevolucaoRepository", "Buscando devoluções para contrato ID: $contratoId")
            val response = apiService.getDevolucoesByContratoId(contratoId)
            
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                LogUtils.warning("DevolucaoRepository", "Falha ao buscar devoluções: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            LogUtils.error("DevolucaoRepository", "Erro ao buscar devoluções", e)
            emptyList()
        }
    }
}
