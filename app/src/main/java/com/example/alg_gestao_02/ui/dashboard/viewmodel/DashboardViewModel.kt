package com.example.alg_gestao_02.ui.dashboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.dashboard.model.Project
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.dashboard.repository.ProjectRepository
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel para o DashboardFragment.
 * Segue o padrão MVVM para separar a lógica de negócios da UI.
 */
class DashboardViewModel(private val repository: ProjectRepository) : ViewModel() {

    // Adiciona ErrorViewModel para tratar erros de forma centralizada
    val errorHandler = ErrorViewModel()

    // LiveData privado para armazenar o estado da UI
    private val _uiState = MutableLiveData<UiState<List<Project>>>()
    
    // LiveData público exposto para a UI
    val uiState: LiveData<UiState<List<Project>>> = _uiState
    
    private var currentFilter: String? = null
    
    init {
        // Carrega os projetos ao inicializar o ViewModel
        loadProjects()
    }
    
    /**
     * Carrega a lista de projetos.
     * No futuro será substituído por chamadas reais à API.
     */
    fun loadProjects() {
        _uiState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                val result = repository.getAllProjects()
                
                if (result.isEmpty()) {
                    _uiState.value = UiState.empty()
                } else {
                    _uiState.value = UiState.success(result)
                }
            } catch (e: Exception) {
                // Usa o sistema centralizado de tratamento de erros
                errorHandler.handleException(e, "DashboardViewModel", true)
                
                // Ainda atualiza o estado da UI para manter a compatibilidade
                _uiState.value = UiState.error(e.message ?: "Erro ao carregar projetos")
            }
        }
    }
    
    /**
     * Atualiza os projetos (usado pelo pull-to-refresh)
     */
    fun refreshProjects() {
        loadProjects()
    }
    
    /**
     * Filtra projetos por categoria (em desenvolvimento)
     */
    fun filterProjects(category: String?) {
        currentFilter = category
        _uiState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                // Simular delay de rede
                delay(500)
                
                val allProjects = repository.getAllProjects()
                
                val filteredProjects = if (category == null) {
                    allProjects
                } else {
                    allProjects.filter { it.category == category }
                }
                
                if (filteredProjects.isEmpty()) {
                    _uiState.value = UiState.empty()
                } else {
                    _uiState.value = UiState.success(filteredProjects)
                }
            } catch (e: Exception) {
                // Usa o sistema centralizado de tratamento de erros
                errorHandler.handleException(e, "DashboardViewModel", true)
                
                // Ainda atualiza o estado da UI para manter a compatibilidade
                _uiState.value = UiState.error("Erro ao filtrar projetos: ${e.message}")
            }
        }
    }
    
    /**
     * Carrega informações detalhadas de um projeto
     */
    fun getProjectById(projectId: String, onResult: (Project?) -> Unit) {
        viewModelScope.launch {
            try {
                val project = repository.getProjectById(projectId)
                onResult(project)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
    
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                val success = repository.deleteProject(projectId)
                if (success) {
                    refreshProjects()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.error("Falha ao excluir projeto: ${e.message}")
            }
        }
    }
}

/**
 * Factory para criar instâncias do DashboardViewModel com o repositório correto
 */
class DashboardViewModelFactory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
} 