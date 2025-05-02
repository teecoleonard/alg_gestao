package com.example.alg_gestao_02.ui.summary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.data.repository.ProjectSummaryRepository
import com.example.alg_gestao_02.ui.state.UiState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel responsável por gerenciar os dados do Sumário do Projeto.
 * Implementa o padrão MVVM para separar a lógica de apresentação da interface do usuário.
 * Suporta caching offline através do Room Database.
 */
class ProjectSummaryViewModel(private val repository: ProjectSummaryRepository) : ViewModel() {
    private val _uiState = MutableLiveData<UiState<List<ProjectContractItem>>>()
    val uiState: LiveData<UiState<List<ProjectContractItem>>> = _uiState
    
    private var currentProjectId: String = ""
    
    init {
        // Iniciar sincronização de dados não sincronizados
        viewModelScope.launch {
            try {
                repository.syncUnsyncedData()
            } catch (e: Exception) {
                // Ignorar erros de sincronização inicial
            }
        }
    }
    
    /**
     * Carrega os contratos para o projeto especificado
     */
    fun loadContracts(projectId: String) {
        currentProjectId = projectId
        
        // Iniciar carregando dados iniciais, se necessário
        viewModelScope.launch {
            try {
                repository.loadInitialData(projectId)
            } catch (e: Exception) {
                // Ignorar erros ao carregar dados iniciais
            }
        }
        
        // Observar o fluxo de dados do repositório
        repository.getContractsByProject(projectId)
            .onStart { _uiState.value = UiState.loading() }
            .onEmpty { _uiState.value = UiState.empty() }
            .catch { e -> _uiState.value = UiState.error("Erro ao carregar contratos: ${e.message}") }
            .map { contracts -> 
                if (contracts.isEmpty()) UiState.empty() 
                else UiState.success(contracts) 
            }
            .asLiveData(viewModelScope.coroutineContext)
            .observeForever { state -> _uiState.value = state }
    }
    
    /**
     * Filtra os contratos por tipo (payment ou debt)
     */
    fun filterContractsByType(type: String) {
        if (currentProjectId.isEmpty()) return
        
        repository.getContractsByType(currentProjectId, type)
            .onStart { _uiState.value = UiState.loading() }
            .onEmpty { _uiState.value = UiState.empty() }
            .catch { e -> _uiState.value = UiState.error("Erro ao filtrar contratos: ${e.message}") }
            .map { contracts -> 
                if (contracts.isEmpty()) UiState.empty() 
                else UiState.success(contracts) 
            }
            .asLiveData(viewModelScope.coroutineContext)
            .observeForever { state -> _uiState.value = state }
    }
    
    /**
     * Busca contratos pelo termo especificado
     */
    fun searchContracts(query: String) {
        if (currentProjectId.isEmpty()) return
        
        if (query.isBlank()) {
            loadContracts(currentProjectId)
            return
        }
        
        repository.searchContracts(currentProjectId, query)
            .onStart { _uiState.value = UiState.loading() }
            .onEmpty { _uiState.value = UiState.empty() }
            .catch { e -> _uiState.value = UiState.error("Erro na busca: ${e.message}") }
            .map { contracts -> 
                if (contracts.isEmpty()) UiState.empty() 
                else UiState.success(contracts) 
            }
            .asLiveData(viewModelScope.coroutineContext)
            .observeForever { state -> _uiState.value = state }
    }
    
    /**
     * Adiciona um novo contrato
     */
    fun addContract(contract: ProjectContractItem) {
        viewModelScope.launch {
            try {
                val success = repository.addContract(contract)
                if (!success) {
                    _uiState.value = UiState.error("Erro ao adicionar contrato")
                }
                // Não precisa recarregar explicitamente, pois o Flow observará as mudanças no DB
            } catch (e: Exception) {
                _uiState.value = UiState.error("Erro ao adicionar contrato: ${e.message}")
            }
        }
    }
    
    /**
     * Atualiza um contrato existente
     */
    fun updateContract(contract: ProjectContractItem) {
        viewModelScope.launch {
            try {
                val success = repository.updateContract(contract)
                if (!success) {
                    _uiState.value = UiState.error("Erro ao atualizar contrato")
                }
                // Não precisa recarregar explicitamente, pois o Flow observará as mudanças no DB
            } catch (e: Exception) {
                _uiState.value = UiState.error("Erro ao atualizar contrato: ${e.message}")
            }
        }
    }
    
    /**
     * Remove um contrato
     */
    fun deleteContract(contractId: String) {
        viewModelScope.launch {
            try {
                val success = repository.deleteContract(contractId)
                if (!success) {
                    _uiState.value = UiState.error("Erro ao remover contrato")
                }
                // Não precisa recarregar explicitamente, pois o Flow observará as mudanças no DB
            } catch (e: Exception) {
                _uiState.value = UiState.error("Erro ao remover contrato: ${e.message}")
            }

        }

    }
    
    /**
     * Carrega todos os contratos de pagamento
     */
    fun loadPaymentContracts() {
        filterContractsByType("payment")
    }
    
    /**
     * Carrega todos os contratos de dívida
     */
    fun loadDebtContracts() {
        filterContractsByType("debt")
    }
    
    /**
     * Força uma sincronização com a API
     */
    fun syncWithApi() {
        viewModelScope.launch {
            try {
                repository.syncUnsyncedData()
            } catch (e: Exception) {
                _uiState.value = UiState.error("Erro na sincronização: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Limpar observadores para evitar vazamentos de memória
        repository.getContractsByProject(currentProjectId)
            .asLiveData(viewModelScope.coroutineContext)
            .removeObserver { }
    }
}

/**
 * Factory para criação do ProjectSummaryViewModel com o repository apropriado
 */
class ProjectSummaryViewModelFactory(private val repository: ProjectSummaryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectSummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectSummaryViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}