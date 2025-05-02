package com.example.alg_gestao_02.ui.contract.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.contract.model.Contrato
import com.example.alg_gestao_02.data.repository.ContractRepository
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch

/**
 * ViewModel para o ContratosFragment.
 * Segue o padrão MVVM para separar a lógica de negócios da UI.
 */
class ContratosViewModel(private val repository: ContractRepository) : ViewModel() {

    // Adiciona ErrorViewModel para tratar erros de forma centralizada
    val errorHandler = ErrorViewModel()

    // LiveData privado para armazenar o estado da UI
    private val _contratosState = MutableLiveData<UiState<List<Contrato>>>()
    
    // LiveData público exposto para a UI
    val contratosState: LiveData<UiState<List<Contrato>>> = _contratosState
    
    // Filtro atual aplicado
    private var filtroAtual = FiltroContrato.TODOS
    
    // Texto de busca atual
    private var textoBusca = ""
    
    init {
        // Carrega os contratos ao inicializar o ViewModel
        loadContratos()
    }
    
    /**
     * Carrega a lista de contratos.
     */
    fun loadContratos() {
        LogUtils.debug("ContratosViewModel", "Carregando contratos com filtro: $filtroAtual")
        
        _contratosState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                var contratos: List<Contrato> = emptyList()
                
                // Se tiver texto de busca, priorizar a busca
                if (textoBusca.isNotEmpty()) {
                    contratos = repository.searchContratos(textoBusca)
                } else {
                    // Aplicar filtro por status
                    contratos = when (filtroAtual) {
                        FiltroContrato.TODOS -> repository.getAllContratos()
                        FiltroContrato.ATIVOS -> repository.getContratosByStatus("active")
                        FiltroContrato.PENDENTES -> repository.getContratosByStatus("pending")
                        FiltroContrato.CONCLUIDOS -> repository.getContratosByStatus("completed")
                        FiltroContrato.CANCELADOS -> repository.getContratosByStatus("cancelled")
                    }
                }
                
                if (contratos.isEmpty()) {
                    _contratosState.value = UiState.empty()
                } else {
                    _contratosState.value = UiState.success(contratos)
                }
                
            } catch (e: Exception) {
                // Primeiro, usar o sistema centralizado de tratamento de erros
                errorHandler.handleException(e, "ContratosViewModel", true)
                
                // Segundo, atualizar o estado da UI para refletir o erro
                _contratosState.value = UiState.error("Falha ao carregar contratos: ${e.message}")
            }
        }
    }
    
    /**
     * Define o filtro de tipo de contrato e recarrega os dados
     */
    fun setFiltroTipo(filtro: FiltroContrato) {
        filtroAtual = filtro
        loadContratos()
    }
    
    /**
     * Define o texto de busca e recarrega os dados
     */
    fun setTextoBusca(texto: String) {
        if (textoBusca != texto) {
            textoBusca = texto
            loadContratos()
        }
    }
    
    /**
     * Adiciona um novo contrato
     */
    fun adicionarContrato(novoContrato: Contrato, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addContrato(novoContrato)
                
                LogUtils.debug("ContratosViewModel", "Contrato ${novoContrato.contractNumber} adicionado com sucesso")
                
                // Recarregar dados após adicionar
                loadContratos()
                onSuccess()
            } catch (e: Exception) {
                LogUtils.error("ContratosViewModel", "Erro ao adicionar contrato: ${e.message}")
                errorHandler.handleException(e, "ContratosViewModel")
                onError(e.message ?: "Erro desconhecido ao adicionar contrato")
            }
        }
    }
    
    /**
     * Atualizar contrato existente
     */
    fun atualizarContrato(contrato: Contrato, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val sucesso = repository.updateContrato(contrato)
                if (sucesso) {
                    LogUtils.debug("ContratosViewModel", "Contrato ${contrato.contractNumber} atualizado com sucesso")
                    
                    // Recarregar dados após atualizar
                    loadContratos()
                    onSuccess()
                } else {
                    throw Exception("Contrato não encontrado")
                }
            } catch (e: Exception) {
                LogUtils.error("ContratosViewModel", "Erro ao atualizar contrato: ${e.message}")
                errorHandler.handleException(e, "ContratosViewModel")
                onError(e.message ?: "Erro desconhecido ao atualizar contrato")
            }
        }
    }
    
    /**
     * Excluir contrato
     */
    fun excluirContrato(contrato: Contrato, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val sucesso = repository.deleteContrato(contrato.id)
                if (sucesso) {
                    LogUtils.debug("ContratosViewModel", "Contrato ${contrato.contractNumber} excluído com sucesso")
                    
                    // Recarregar dados após excluir
                    loadContratos()
                    onSuccess()
                } else {
                    throw Exception("Contrato não encontrado")
                }
            } catch (e: Exception) {
                LogUtils.error("ContratosViewModel", "Erro ao excluir contrato: ${e.message}")
                errorHandler.handleException(e, "ContratosViewModel")
                onError(e.message ?: "Erro desconhecido ao excluir contrato")
            }
        }
    }
    
    /**
     * Enum para representar os possíveis filtros de contratos
     */
    enum class FiltroContrato {
        TODOS,
        ATIVOS,
        PENDENTES,
        CONCLUIDOS,
        CANCELADOS
    }
}

/**
 * Factory para criar instâncias do ContratosViewModel com o repositório correto
 */
class ContratosViewModelFactory(private val repository: ContractRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContratosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContratosViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
} 