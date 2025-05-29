package com.example.alg_gestao_02.dashboard.fragments.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.DashboardStats
import com.example.alg_gestao_02.data.repository.DashboardRepository
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch

/**
 * ViewModel para o Dashboard principal da aplicação
 * Gerencia o estado da UI e o carregamento de dados do Dashboard
 */
class DashboardViewModel(private val repository: DashboardRepository = DashboardRepository()) : ViewModel() {
    
    // LiveData para o estado da UI
    private val _uiState = MutableLiveData<UiState<DashboardStats>>()
    val uiState: LiveData<UiState<DashboardStats>> = _uiState
    
    // LiveData para as estatísticas do dashboard
    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> = _dashboardStats
    
    init {
        LogUtils.debug("DashboardViewModel", "Inicializando ViewModel do Dashboard")
        loadDashboardStats()
    }
    
    /**
     * Carrega as estatísticas do dashboard
     */
    private fun loadDashboardStats() {
        LogUtils.debug("DashboardViewModel", "Carregando estatísticas do dashboard")
        
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                val stats = repository.getDashboardStats()
                _dashboardStats.value = stats
                _uiState.value = UiState.Success(stats)
                
                LogUtils.debug("DashboardViewModel", "Estatísticas carregadas com sucesso")
            } catch (e: Exception) {
                LogUtils.error("DashboardViewModel", "Erro ao carregar estatísticas: ${e.message}")
                _uiState.value = UiState.Error("Erro ao carregar estatísticas: ${e.message}")
            }
        }
    }
    
    /**
     * Carrega ou recarrega os dados do dashboard.
     */
    fun refreshDashboard() {
        LogUtils.debug("DashboardViewModel", "Atualizando dados do dashboard")
        loadDashboardStats()
    }
} 