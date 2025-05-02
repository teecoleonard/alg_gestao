package com.example.alg_gestao_02.ui.contract.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.data.repository.ContractRepository
import com.example.alg_gestao_02.ui.state.UiState
import kotlinx.coroutines.launch
import java.time.YearMonth

class ProjectContractsViewModel(private val repository: ContractRepository) : ViewModel() {
    private val _uiState = MutableLiveData<UiState<List<ProjectContractItem>>>()
    val uiState: LiveData<UiState<List<ProjectContractItem>>> = _uiState
    
    private var currentMonth = YearMonth.now()
    private var currentProjectId: String = ""
    
    fun loadContracts(projectId: String) {
        currentProjectId = projectId
        loadContractsByMonth()
    }
    
    fun loadPreviousMonth() {
        currentMonth = currentMonth.minusMonths(1)
        loadContractsByMonth()
    }
    
    fun loadNextMonth() {
        currentMonth = currentMonth.plusMonths(1)
        loadContractsByMonth()
    }
    
    fun getCurrentMonth(): YearMonth {
        return currentMonth
    }
    
    private fun loadContractsByMonth() {
        _uiState.value = UiState.Companion.loading()
        
        viewModelScope.launch {
            try {
                val contracts = repository.getAllContratos()
                
                // Filtrar contratos pelo projeto (simulando o comportamento até que a implementação real esteja disponível)
                val result = contracts.filter { contrato ->
                    contrato.projectId == currentProjectId
                }.map { contrato ->
                    ProjectContractItem(
                        id = contrato.id,
                        projectId = contrato.projectId,
                        name = contrato.title,
                        date = contrato.startDate,
                        type = "Serviço",
                        status = contrato.status,
                        value = contrato.value,
                        description = contrato.description ?: ""
                    )
                }
                
                if (result.isEmpty()) {
                    _uiState.value = UiState.Companion.empty()
                } else {
                    _uiState.value = UiState.Companion.success(result)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Companion.error(e.message ?: "Erro ao carregar contratos")
            }
        }
    }
    
    fun deleteContract(contractId: String) {
        viewModelScope.launch {
            try {
                repository.deleteContrato(contractId)
                // Recarregar os contratos após a exclusão
                loadContractsByMonth()
            } catch (e: Exception) {
                _uiState.value = UiState.Companion.error("Falha ao excluir contrato: ${e.message}")
            }
        }
    }
    
    fun loadAllContracts() {
        _uiState.value = UiState.Companion.loading()
        
        viewModelScope.launch {
            try {
                val contracts = repository.getAllContratos()
                
                // Filtrar contratos pelo projeto (simulando o comportamento até que a implementação real esteja disponível)
                val result = contracts.filter { contrato ->
                    contrato.projectId == currentProjectId
                }.map { contrato ->
                    ProjectContractItem(
                        id = contrato.id,
                        projectId = contrato.projectId,
                        name = contrato.title,
                        date = contrato.startDate,
                        type = "Serviço",
                        status = contrato.status,
                        value = contrato.value,
                        description = contrato.description ?: ""
                    )
                }
                
                if (result.isEmpty()) {
                    _uiState.value = UiState.Companion.empty()
                } else {
                    _uiState.value = UiState.Companion.success(result)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Companion.error(e.message ?: "Erro ao carregar todos os contratos")
            }
        }
    }
    
    fun addContract(contract: ProjectContractItem) {
        viewModelScope.launch {
            try {
                // Implementar ao conectar com API real
                loadContractsByMonth()
            } catch (e: Exception) {
                _uiState.value = UiState.Companion.error("Falha ao adicionar contrato: ${e.message}")
            }
        }
    }
    
    fun updateContract(contract: ProjectContractItem) {
        viewModelScope.launch {
            try {
                // Implementar ao conectar com API real
                loadContractsByMonth()
            } catch (e: Exception) {
                _uiState.value = UiState.Companion.error("Falha ao atualizar contrato: ${e.message}")
            }
        }
    }
}

class ProjectContractsViewModelFactory(private val repository: ContractRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectContractsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectContractsViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
} 