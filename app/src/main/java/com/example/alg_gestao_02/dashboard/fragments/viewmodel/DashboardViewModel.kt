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
        LogUtils.info("DashboardViewModel", "🚀 ========== INICIALIZANDO DASHBOARD VIEWMODEL ==========")
        LogUtils.debug("DashboardViewModel", "📋 Repository: ${repository.javaClass.simpleName}")
        LogUtils.debug("DashboardViewModel", "📊 LiveData configurado")
        LogUtils.debug("DashboardViewModel", "🔄 Iniciando carregamento automático...")
        loadDashboardStats()
    }
    
    /**
     * Carrega as estatísticas do dashboard
     */
    private fun loadDashboardStats() {
        LogUtils.info("DashboardViewModel", "📥 ========== CARREGANDO ESTATÍSTICAS DO DASHBOARD ==========")
        LogUtils.debug("DashboardViewModel", "⏳ Definindo estado como Loading...")
        
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                LogUtils.info("DashboardViewModel", "🔄 Chamando repository para buscar dados...")
                val startTime = System.currentTimeMillis()
                
                val stats = repository.getDashboardStats()
                
                val loadTime = System.currentTimeMillis() - startTime
                LogUtils.info("DashboardViewModel", "⏱️ Tempo total de carregamento: ${loadTime}ms")
                
                LogUtils.info("DashboardViewModel", "✅ Dados recebidos com sucesso!")
                LogUtils.debug("DashboardViewModel", "📊 Estatísticas carregadas:")
                LogUtils.debug("DashboardViewModel", "   📋 Contratos: ${stats.contratos}")
                LogUtils.debug("DashboardViewModel", "   👥 Clientes: ${stats.clientes}")
                LogUtils.debug("DashboardViewModel", "   ⚙️ Equipamentos: ${stats.equipamentos}")
                LogUtils.debug("DashboardViewModel", "   📦 Devoluções: ${stats.devolucoes}")
                
                // Log das estatísticas expandidas
                if (stats.contratosEstaSemana > 0 || stats.clientesHoje > 0 || 
                    stats.equipamentosDisponiveis > 0 || stats.devolucoesPendentes > 0) {
                    LogUtils.info("DashboardViewModel", "📈 Estatísticas expandidas disponíveis:")
                    LogUtils.debug("DashboardViewModel", "   📋 Contratos esta semana: ${stats.contratosEstaSemana}")
                    LogUtils.debug("DashboardViewModel", "   👥 Clientes hoje: ${stats.clientesHoje}")
                    LogUtils.debug("DashboardViewModel", "   ⚙️ Equipamentos disponíveis: ${stats.equipamentosDisponiveis}")
                    LogUtils.debug("DashboardViewModel", "   📦 Devoluções pendentes: ${stats.devolucoesPendentes}")
                } else {
                    LogUtils.warning("DashboardViewModel", "⚠️ Estatísticas expandidas não disponíveis ou zeradas")
                }
                
                // Log das atividades recentes
                if (stats.atividadesRecentes.isNotEmpty()) {
                    LogUtils.info("DashboardViewModel", "🎯 ${stats.atividadesRecentes.size} atividades recentes recebidas")
                } else {
                    LogUtils.warning("DashboardViewModel", "⚠️ Nenhuma atividade recente recebida")
                }
                
                LogUtils.debug("DashboardViewModel", "📤 Atualizando LiveData com novos dados...")
                _dashboardStats.value = stats
                _uiState.value = UiState.Success(stats)
                
                LogUtils.info("DashboardViewModel", "✅ ========== CARREGAMENTO CONCLUÍDO COM SUCESSO ==========")
                
            } catch (e: Exception) {
                LogUtils.error("DashboardViewModel", "❌ ========== ERRO NO CARREGAMENTO ==========")
                LogUtils.error("DashboardViewModel", "🔍 Tipo do erro: ${e.javaClass.simpleName}")
                LogUtils.error("DashboardViewModel", "📝 Mensagem: ${e.message}")
                LogUtils.error("DashboardViewModel", "📚 Stack trace completo:", e)
                
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> {
                        LogUtils.error("DashboardViewModel", "🌐 Problema de conectividade de rede")
                        "Sem conexão com a internet. Verifique sua conexão."
                    }
                    is java.net.ConnectException -> {
                        LogUtils.error("DashboardViewModel", "🔌 Servidor indisponível")
                        "Servidor indisponível. Tente novamente em alguns instantes."
                    }
                    is java.net.SocketTimeoutException -> {
                        LogUtils.error("DashboardViewModel", "⏰ Timeout na requisição")
                        "Tempo limite excedido. Verifique sua conexão."
                    }
                    else -> {
                        LogUtils.error("DashboardViewModel", "❓ Erro genérico ou desconhecido")
                        "Erro ao carregar estatísticas: ${e.message ?: "Erro desconhecido"}"
                    }
                }
                
                LogUtils.debug("DashboardViewModel", "📤 Atualizando estado para Error com mensagem: '$errorMessage'")
                _uiState.value = UiState.Error(errorMessage)
                
                LogUtils.error("DashboardViewModel", "❌ ========== CARREGAMENTO FALHOU ==========")
            }
        }
    }
    
    /**
     * Carrega ou recarrega os dados do dashboard.
     */
    fun refreshDashboard() {
        LogUtils.info("DashboardViewModel", "🔄 ========== REFRESH SOLICITADO PELO USUÁRIO ==========")
        LogUtils.debug("DashboardViewModel", "🔄 Usuário fez pull-to-refresh ou clicou em atualizar")
        LogUtils.debug("DashboardViewModel", "📥 Iniciando novo carregamento...")
        loadDashboardStats()
    }
} 