package com.example.alg_gestao_02.ui.client.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.data.repository.ClienteRepository
import com.example.alg_gestao_02.data.repository.ContratoRepository
import com.example.alg_gestao_02.data.repository.DevolucaoRepository
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel para o fragment de detalhes do cliente
 */
class ClientDetailsViewModel(
    private val clienteRepository: ClienteRepository,
    private val contratoRepository: ContratoRepository,
    private val devolucaoRepository: DevolucaoRepository
) : ViewModel() {

    private val _cliente = MutableLiveData<Cliente>()
    val cliente: LiveData<Cliente> = _cliente

    private val _contratos = MutableLiveData<List<Contrato>>()
    val contratos: LiveData<List<Contrato>> = _contratos

    private val _devolucoes = MutableLiveData<List<Devolucao>>()
    val devolucoes: LiveData<List<Devolucao>> = _devolucoes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Carrega os dados do cliente, seus contratos e devoluções
     */
    fun carregarDetalhesCliente(clienteId: Int) {
        _isLoading.value = true
        _error.value = ""
        
        viewModelScope.launch {
            try {
                // Carrega os dados do cliente
                val clienteResult = try {
                    clienteRepository.getClienteById(clienteId)
                } catch (e: Exception) {
                    LogUtils.error("ClientDetailsViewModel", "Erro ao carregar cliente: ${e.message}")
                    _error.value = "Erro ao carregar dados do cliente: ${e.message}"
                    _isLoading.value = false
                    return@launch
                }
                _cliente.value = clienteResult

                // Carrega os contratos do cliente
                val contratosResult = try {
                    contratoRepository.getContratosByClienteId(clienteId)
                } catch (e: Exception) {
                    LogUtils.error("ClientDetailsViewModel", "Erro ao carregar contratos: ${e.message}")
                    _contratos.value = emptyList()
                    emptyList()
                }
                _contratos.value = contratosResult

                // Carrega as devoluções de todos os contratos do cliente
                val todasDevolucoes = mutableListOf<Devolucao>()
                contratosResult.forEach { contrato ->
                    try {
                        val devolucoesContrato = devolucaoRepository.getDevolucoesByContratoIdList(contrato.id)
                        todasDevolucoes.addAll(devolucoesContrato)
                    } catch (e: Exception) {
                        LogUtils.error("ClientDetailsViewModel", "Erro ao carregar devoluções para contrato ${contrato.id}: ${e.message}")
                    }
                }
                _devolucoes.value = todasDevolucoes

                _isLoading.value = false
            } catch (e: IOException) {
                LogUtils.error("ClientDetailsViewModel", "Erro de rede: ${e.message}")
                _error.value = "Erro de conexão. Verifique sua internet."
                _isLoading.value = false
            } catch (e: Exception) {
                LogUtils.error("ClientDetailsViewModel", "Erro ao carregar detalhes do cliente: ${e.message}")
                _error.value = "Erro ao carregar dados: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Retorna a contagem de devoluções por status
     */
    fun getStatusDevolucoesCount(): Triple<Int, Int, Int> {
        val devolucoesList = _devolucoes.value ?: emptyList()
        
        val pendentes = devolucoesList.count { it.isPendente() }
        val concluidas = devolucoesList.count { it.isProcessado() && it.statusItemDevolucao == "Devolvido" }
        val problemas = devolucoesList.count { 
            it.isProcessado() && (it.statusItemDevolucao == "Avariado" || it.statusItemDevolucao == "Faltante") 
        }
        
        return Triple(pendentes, concluidas, problemas)
    }
    
    /**
     * Retorna devoluções filtradas por status
     */
    fun getDevolucoesByStatus(status: String): List<Devolucao> {
        val devolucoesList = _devolucoes.value ?: emptyList()
        
        return when (status) {
            "Pendente" -> devolucoesList.filter { it.isPendente() }
            "Devolvido" -> devolucoesList.filter { it.isProcessado() && it.statusItemDevolucao == "Devolvido" }
            "Problemas" -> devolucoesList.filter { 
                it.isProcessado() && (it.statusItemDevolucao == "Avariado" || it.statusItemDevolucao == "Faltante") 
            }
            else -> emptyList()
        }
    }
}
