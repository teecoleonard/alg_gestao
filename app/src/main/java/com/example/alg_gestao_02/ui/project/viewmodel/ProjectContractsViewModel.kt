package com.example.alg_gestao_02.ui.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.data.repository.ContractRepository
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar contratos de um projeto específico.
 * Segue o padrão MVVM para separar a lógica de negócios da UI.
 */
class ProjectContractsViewModel : ViewModel() {

    // Estados da UI que o fragment irá observar
    sealed class UiState {
        object Loading : UiState()
        data class Success(val contracts: List<ProjectContractItem>) : UiState()
        object Empty : UiState()
        data class Error(val message: String) : UiState()
    }

    private val repository = ContractRepository()
    
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    
    private var currentProjectId: String? = null
    private var currentStatus: String? = null
    
    /**
     * Carrega contratos para um projeto específico
     */
    fun loadContractsForProject(projectId: String) {
        currentProjectId = projectId
        currentStatus = null
        _uiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                val contracts = repository.getContractsByProject(projectId)
                
                if (contracts.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(contracts)
                }
            } catch (e: Exception) {
                LogUtils.error("ProjectContractsViewModel", "Erro ao carregar contratos: ${e.message}")
                _uiState.value = UiState.Error("Falha ao carregar contratos: ${e.message}")
            }
        }
    }
    
    /**
     * Filtra contratos por status
     */
    fun filterByStatus(status: String?) {
        if (currentProjectId == null) {
            LogUtils.error("ProjectContractsViewModel", "Tentativa de filtrar sem um projeto definido")
            _uiState.value = UiState.Error("Nenhum projeto selecionado")
            return
        }
        
        _uiState.value = UiState.Loading
        currentStatus = status
        
        viewModelScope.launch {
            try {
                val contracts = if (status == null) {
                    repository.getContractsByProject(currentProjectId!!)
                } else {
                    repository.getContractsByProjectAndStatus(currentProjectId!!, status)
                }
                
                if (contracts.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(contracts)
                }
            } catch (e: Exception) {
                LogUtils.error("ProjectContractsViewModel", "Erro ao filtrar contratos: ${e.message}")
                _uiState.value = UiState.Error("Falha ao filtrar contratos: ${e.message}")
            }
        }
    }
    
    /**
     * Atualiza dados (por exemplo, ao fazer swipe refresh)
     */
    fun refresh() {
        if (currentProjectId == null) {
            return
        }
        
        _uiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                val contracts = if (currentStatus == null) {
                    repository.getContractsByProject(currentProjectId!!)
                } else {
                    repository.getContractsByProjectAndStatus(currentProjectId!!, currentStatus!!)
                }
                
                if (contracts.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(contracts)
                }
            } catch (e: Exception) {
                LogUtils.error("ProjectContractsViewModel", "Erro ao atualizar contratos: ${e.message}")
                _uiState.value = UiState.Error("Falha ao atualizar contratos: ${e.message}")
            }
        }
    }
} 