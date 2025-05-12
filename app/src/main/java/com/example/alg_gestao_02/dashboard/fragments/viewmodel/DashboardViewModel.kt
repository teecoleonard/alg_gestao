package com.example.alg_gestao_02.dashboard.fragments.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel para o Dashboard principal da aplicação
 * Gerencia o estado da UI e o carregamento de dados do Dashboard
 */
class DashboardViewModel : ViewModel() {
    
    // LiveData para o estado da UI
    private val _uiState = MutableLiveData<UiState<Boolean>>()
    val uiState: LiveData<UiState<Boolean>> = _uiState
    
    init {
        LogUtils.debug("DashboardViewModel", "Inicializando ViewModel do Dashboard")
    }
    
    /**
     * Carrega ou recarrega os dados do dashboard.
     * No momento apenas simula um carregamento, mas pode ser expandido para
     * buscar dados reais de diferentes fontes (resumos, estatísticas, etc.)
     */
    fun refreshDashboard() {
        LogUtils.debug("DashboardViewModel", "Atualizando dados do dashboard")
        
        // Emitir estado de carregamento
        _uiState.value = UiState.Loading()
        
        // Usando coroutines para operação assíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simular tempo de carregamento (2 segundos)
                delay(2000)
                
                // Simulação de sucesso após 2 segundos
                _uiState.postValue(UiState.Success(true))
                
                LogUtils.debug("DashboardViewModel", "Dashboard atualizado com sucesso")
            } catch (e: Exception) {
                LogUtils.error("DashboardViewModel", "Erro ao atualizar dashboard: ${e.message}")
                _uiState.postValue(UiState.Error("Erro ao atualizar o dashboard: ${e.message}"))
            }
        }
    }
} 