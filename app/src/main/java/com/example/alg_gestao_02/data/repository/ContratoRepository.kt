package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.data.models.EquipamentoJson
import com.example.alg_gestao_02.data.models.EquipamentoContratoData
import com.example.alg_gestao_02.data.models.ContratoResponse
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
                response.body()?.let { contrato ->
                    // Log do JSON recebido para debug
                    LogUtils.debug("ContratoRepository", "JSON recebido: ${response.raw()}")

                    // --- NOVO: garantir que a lista de equipamentos seja populada ---
                    val equipamentosOriginais = contrato.equipamentos
                    val equipamentosAlternativos = contrato.equipamentoContratos
                    val equipamentosParaProcessar = if (!equipamentosOriginais.isNullOrEmpty()) {
                        equipamentosOriginais
                    } else if (!equipamentosAlternativos.isNullOrEmpty()) {
                        equipamentosAlternativos
                    } else {
                        emptyList()
                    }

                    val equipamentosProcessados = if (equipamentosParaProcessar.isNotEmpty()) {
                        try {
                            val equipamentosJson = equipamentosParaProcessar.map { equip ->
                                EquipamentoJson(
                                    id = equip.equipamentoId,
                                    nomeEquip = equip.equipamentoNome ?: equip.equipamento?.nomeEquip,
                                    precoDiaria = equip.valorUnitario.toString(),
                                    precoSemanal = equip.equipamento?.precoSemanal?.toString() ?: (equip.valorUnitario * 7).toString(),
                                    precoQuinzenal = equip.equipamento?.precoQuinzenal?.toString() ?: (equip.valorUnitario * 15).toString(),
                                    precoMensal = equip.equipamento?.precoMensal?.toString() ?: (equip.valorUnitario * 30).toString(),
                                    codigoEquip = equip.equipamento?.codigoEquip ?: equip.equipamentoNome?.take(4) ?: "",
                                    quantidadeDisp = equip.quantidadeEquip, // usa a quantidade do contrato
                                    valorPatrimonio = equip.equipamento?.valorPatrimonio ?: equip.valorTotal,
                                    equipamentoContrato = EquipamentoContratoData(
                                        id = equip.id,
                                        contratoId = equip.contratoId,
                                        equipamentoId = equip.equipamentoId,
                                        quantidadeEquip = equip.quantidadeEquip, // usa a quantidade do contrato
                                        valorUnitario = equip.valorUnitario.toString(),
                                        valorTotal = equip.valorTotal.toString(),
                                        valorFrete = equip.valorFrete.toString()
                                    )
                                )
                            }
                            contrato.processarEquipamentosJson(equipamentosJson)
                        } catch (e: Exception) {
                            LogUtils.error("ContratoRepository", "Erro ao processar equipamentos", e)
                            emptyList()
                        }
                    } else {
                        LogUtils.debug("ContratoRepository", "Contrato sem equipamentos ou equipamentos nulos")
                        emptyList()
                    }

                    // Criar uma nova cópia do contrato com os equipamentos processados
                    val contratoProcessado = contrato.copy(equipamentos = equipamentosProcessados)
                    Resource.Success(contratoProcessado)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao buscar contrato: ${response.code()}")
                Resource.Error("Erro ao buscar contrato: ${response.message()}")
            }
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
     * Busca contratos por cliente
     */
    suspend fun getContratosByClienteId(clienteId: Int): List<Contrato> {
        return when (val result = getContratosByCliente(clienteId)) {
            is Resource.Success -> result.data
            is Resource.Error -> {
                LogUtils.error("ContratoRepository", "Erro ao buscar contratos do cliente: ${result.message}")
                emptyList()
            }
            is Resource.Loading -> {
                LogUtils.debug("ContratoRepository", "Carregando contratos do cliente...")
                emptyList()
            }
            else -> emptyList()
        }
    }
    
    /**
     * Cria um novo contrato
     */
    suspend fun createContrato(contrato: Contrato): Resource<Contrato> {
        return try {
            LogUtils.debug("ContratoRepository", "Criando contrato: ${contrato.contratoNum}")
            
            // Verificar se o cliente existe
            if (contrato.clienteId <= 0) {
                return Resource.Error("Cliente inválido")
            }
            
            // Validar IDs dos equipamentos
            val equipamentosValidos = contrato.equipamentos.mapNotNull { equipamento ->
                if (equipamento.id <= 0) {
                    LogUtils.warning("ContratoRepository", "Equipamento com ID inválido: ${equipamento.id}")
                    null
                } else {
                    equipamento
                }
            }
            
            if (equipamentosValidos.size != contrato.equipamentos.size) {
                LogUtils.error("ContratoRepository", 
                    "Alguns equipamentos foram removidos devido a IDs inválidos")
            }
            
            // Criar contrato com equipamentos validados
            val contratoParaSalvar = contrato.copy(equipamentos = equipamentosValidos)
            val response = apiService.createContrato(contratoParaSalvar)
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    // Log do JSON recebido para debug
                    LogUtils.debug("ContratoRepository", "JSON recebido: ${response.raw()}")
                    
                    // Extrair contrato da resposta
                    val contratoCriado = responseBody.contrato
                    
                    // Validar ID retornado
                    if (contratoCriado.id <= 0) {
                        LogUtils.error("ContratoRepository", "ID inválido retornado: ${contratoCriado.id}")
                        return Resource.Error("ID de contrato inválido retornado do servidor")
                    }
                    
                    LogUtils.debug("ContratoRepository", "Contrato criado com ID: ${contratoCriado.id}")
                    Resource.Success(contratoCriado)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao criar contrato: ${response.code()}")
                val errorMessage = if (response.errorBody() != null) {
                    "Erro ao criar contrato: ${response.errorBody()?.string() ?: "Erro desconhecido"}"
                } else {
                    "Erro ao criar contrato: ${response.message()}"
                }
                Resource.Error(errorMessage)
            }
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
            LogUtils.debug("ContratoRepository", "Atualizando contrato: $id")
            
            // Verificar se o ID é válido
            if (id <= 0) {
                return Resource.Error("ID de contrato inválido")
            }
            
            // Verificar se o cliente existe
            if (contrato.clienteId <= 0) {
                return Resource.Error("Cliente inválido")
            }
            
            // Validar IDs dos equipamentos
            val equipamentosValidos = contrato.equipamentos.mapNotNull { equipamento ->
                if (equipamento.id <= 0) {
                    LogUtils.warning("ContratoRepository", "Equipamento com ID inválido: ${equipamento.id}")
                    null
                } else if (equipamento.contratoId != id) {
                    LogUtils.warning("ContratoRepository", 
                        "Equipamento ${equipamento.id} com contratoId incorreto: ${equipamento.contratoId} (esperado: $id)")
                    null
                } else {
                    equipamento
                }
            }
            
            if (equipamentosValidos.size != contrato.equipamentos.size) {
                LogUtils.error("ContratoRepository", 
                    "Alguns equipamentos foram removidos devido a IDs inválidos")
            }
            
            // Atualizar contrato com equipamentos validados
            val contratoParaAtualizar = contrato.copy(equipamentos = equipamentosValidos)
            val response = apiService.updateContrato(id, contratoParaAtualizar)
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    // Log do JSON recebido para debug
                    LogUtils.debug("ContratoRepository", "JSON recebido: ${response.raw()}")
                    
                    // Extrair contrato da resposta
                    val contratoAtualizado = responseBody.contrato
                    
                    // Validar ID retornado
                    if (contratoAtualizado.id != id) {
                        LogUtils.error("ContratoRepository", 
                            "ID inconsistente retornado: ${contratoAtualizado.id} (esperado: $id)")
                        return Resource.Error("ID de contrato inconsistente retornado do servidor")
                    }
                    
                    // Retornar contrato com equipamentos processados
                    Resource.Success(contratoAtualizado)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("ContratoRepository", "Falha ao atualizar contrato: ${response.code()}")
                Resource.Error("Erro ao atualizar contrato: ${response.message()}")
            }
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
            val numerico = """\d+""".toRegex().find(it.contratoNum ?: "")?.value
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
