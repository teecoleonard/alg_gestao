package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.DashboardStats
import com.example.alg_gestao_02.data.models.FinancialMetrics
import com.example.alg_gestao_02.data.models.ProgressMetrics
import com.example.alg_gestao_02.data.models.TaskMetrics
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import kotlinx.coroutines.delay

/**
 * Repositório para gerenciar dados de estatísticas do dashboard
 */
class DashboardRepository {

    private val apiService = ApiClient.apiService

    /**
     * Busca estatísticas do dashboard
     * @return DashboardStats com contadores de contratos, clientes, equipamentos e devoluções
     */
    suspend fun getDashboardStats(): DashboardStats {
        LogUtils.info("DashboardRepository", "🔄 ========== INICIANDO BUSCA DE ESTATÍSTICAS ==========")
        
        val startTime = System.currentTimeMillis()
        
        try {
            // Log de debug sobre a URL que será chamada
            LogUtils.debug("DashboardRepository", "📡 URL Base: ${ApiClient.getBaseUrl()}")
            LogUtils.debug("DashboardRepository", "📡 Endpoint completo: ${ApiClient.getBaseUrl()}api/dashboard/stats")
            
            // Log de headers e autenticação
            LogUtils.debug("DashboardRepository", "🔐 Verificando autenticação...")
            
            LogUtils.info("DashboardRepository", "📞 Fazendo requisição para API...")
            val response = apiService.getDashboardStats()
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            LogUtils.debug("DashboardRepository", "📋 Status da resposta: ${response.code()} - ${response.message()}")
            LogUtils.debug("DashboardRepository", "📊 Headers da resposta: ${response.headers()}")
            
            if (response.isSuccessful) {
                val stats = response.body()
                if (stats != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== ESTATÍSTICAS OBTIDAS COM SUCESSO ==========")
                    LogUtils.info("DashboardRepository", "📊 DADOS BÁSICOS:")
                    LogUtils.info("DashboardRepository", "   📋 Contratos: ${stats.contratos}")
                    LogUtils.info("DashboardRepository", "   👥 Clientes: ${stats.clientes}")
                    LogUtils.info("DashboardRepository", "   ⚙️ Equipamentos: ${stats.equipamentos}")
                    LogUtils.info("DashboardRepository", "   📦 Devoluções: ${stats.devolucoes}")
                    
                    // Logs das novas estatísticas expandidas
                    LogUtils.info("DashboardRepository", "📈 ESTATÍSTICAS DETALHADAS:")
                    LogUtils.info("DashboardRepository", "   📋 Contratos esta semana: ${stats.contratosEstaSemana}")
                    LogUtils.info("DashboardRepository", "   👥 Clientes hoje: ${stats.clientesHoje}")
                    LogUtils.info("DashboardRepository", "   ⚙️ Equipamentos disponíveis: ${stats.equipamentosDisponiveis}")
                    LogUtils.info("DashboardRepository", "   📦 Devoluções pendentes: ${stats.devolucoesPendentes}")
                    
                    // Log das atividades recentes
                    if (stats.atividadesRecentes.isNotEmpty()) {
                        LogUtils.info("DashboardRepository", "🎯 ATIVIDADES RECENTES (${stats.atividadesRecentes.size} itens):")
                        stats.atividadesRecentes.forEachIndexed { index, atividade ->
                            LogUtils.info("DashboardRepository", "   ${index + 1}. [${atividade.tipo.uppercase()}] ${atividade.titulo}")
                            LogUtils.debug("DashboardRepository", "      Descrição: ${atividade.descricao}")
                            LogUtils.debug("DashboardRepository", "      Tempo: ${atividade.tempoRelativo}")
                        }
                    } else {
                        LogUtils.warning("DashboardRepository", "⚠️ Nenhuma atividade recente encontrada")
                    }
                    
                    LogUtils.info("DashboardRepository", "✅ ========== PROCESSAMENTO CONCLUÍDO ==========")
                    return stats
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta da API é nula!")
                    LogUtils.error("DashboardRepository", "📋 Response body: null")
                    LogUtils.error("DashboardRepository", "📋 Response code: ${response.code()}")
                    LogUtils.error("DashboardRepository", "📋 Response headers: ${response.headers()}")
                    throw Exception("Resposta da API é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ ========== ERRO NA API ==========")
                LogUtils.error("DashboardRepository", "📋 Status Code: ${response.code()}")
                LogUtils.error("DashboardRepository", "📋 Status Message: ${response.message()}")
                LogUtils.error("DashboardRepository", "📋 Response Headers: ${response.headers()}")
                
                // Tentar ler o corpo da resposta de erro
                try {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        LogUtils.error("DashboardRepository", "📋 Error Body: $errorBody")
                    }
                } catch (e: Exception) {
                    LogUtils.error("DashboardRepository", "📋 Erro ao ler error body: ${e.message}")
                }
                
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO BUSCAR ESTATÍSTICAS ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "🔍 Tipo do erro: ${e.javaClass.simpleName}")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            LogUtils.error("DashboardRepository", "📚 Stack trace:", e)
            
            // Log de debug adicional para problemas de rede
            when (e) {
                is java.net.UnknownHostException -> {
                    LogUtils.error("DashboardRepository", "🌐 PROBLEMA DE REDE: Host não encontrado")
                    LogUtils.error("DashboardRepository", "💡 Verifique sua conexão com a internet")
                }
                is java.net.ConnectException -> {
                    LogUtils.error("DashboardRepository", "🔌 PROBLEMA DE CONEXÃO: Não foi possível conectar ao servidor")
                    LogUtils.error("DashboardRepository", "💡 Verifique se a API está rodando em: ${ApiClient.BASE_URL}")
                }
                is java.net.SocketTimeoutException -> {
                    LogUtils.error("DashboardRepository", "⏰ TIMEOUT: A requisição demorou muito para responder")
                    LogUtils.error("DashboardRepository", "💡 Verifique a performance da API")
                }
            }
            
            throw e
        }
    }

    /**
     * Busca métricas financeiras do dashboard
     * @return FinancialMetrics com dados financeiros do sistema
     */
    suspend fun getFinancialMetrics(): FinancialMetrics {
        LogUtils.info("DashboardRepository", "💰 ========== INICIANDO BUSCA DE MÉTRICAS FINANCEIRAS ==========")
        
        val startTime = System.currentTimeMillis()
        
        try {
            LogUtils.debug("DashboardRepository", "📡 Endpoint: ${ApiClient.getBaseUrl()}api/dashboard/financial-metrics")
            
            LogUtils.info("DashboardRepository", "📞 Fazendo requisição para métricas financeiras...")
            val response = apiService.getFinancialMetrics()
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            if (response.isSuccessful) {
                val metrics = response.body()
                if (metrics != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== MÉTRICAS FINANCEIRAS OBTIDAS ==========")
                    LogUtils.info("DashboardRepository", "💰 Valor Total Ativo: R$ ${String.format("%.2f", metrics.valorTotalAtivo)}")
                    LogUtils.info("DashboardRepository", "📈 Receita Mensal: R$ ${String.format("%.2f", metrics.receitaMensal)}")
                    LogUtils.info("DashboardRepository", "🎯 Ticket Médio: R$ ${String.format("%.2f", metrics.ticketMedio)}")
                    
                    return metrics
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta das métricas financeiras é nula!")
                    throw Exception("Resposta das métricas financeiras é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ Erro HTTP ${response.code()}: ${response.message()}")
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO BUSCAR MÉTRICAS FINANCEIRAS ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            
            throw e
        }
    }

    /**
     * Busca métricas de progresso/metas do dashboard
     * @return ProgressMetrics com dados de metas e progresso
     */
    suspend fun getProgressMetrics(): ProgressMetrics {
        LogUtils.info("DashboardRepository", "📊 ========== INICIANDO BUSCA DE MÉTRICAS DE PROGRESSO ==========")
        
        val startTime = System.currentTimeMillis()
        
        try {
            LogUtils.debug("DashboardRepository", "📡 Endpoint: ${ApiClient.getBaseUrl()}api/dashboard/progress-metrics")
            
            LogUtils.info("DashboardRepository", "📞 Fazendo requisição para métricas de progresso...")
            val response = apiService.getProgressMetrics()
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            if (response.isSuccessful) {
                val metrics = response.body()
                if (metrics != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== MÉTRICAS DE PROGRESSO OBTIDAS ==========")
                    LogUtils.info("DashboardRepository", "📊 Contratos: ${metrics.contratosAtual}/${metrics.contratosMeta} (${metrics.contratosPercentual}%)")
                    LogUtils.info("DashboardRepository", "💰 Receita: R$ ${String.format("%.2f", metrics.receitaAtual)}/R$ ${String.format("%.2f", metrics.receitaMeta)} (${metrics.receitaPercentual}%)")
                    LogUtils.info("DashboardRepository", "👥 Clientes: ${metrics.clientesAtual}/${metrics.clientesMeta} (${metrics.clientesPercentual}%)")
                    LogUtils.info("DashboardRepository", "😊 Satisfação: ${metrics.satisfacaoPercentual}%")
                    
                    return metrics
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta das métricas de progresso é nula!")
                    throw Exception("Resposta das métricas de progresso é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ Erro HTTP ${response.code()}: ${response.message()}")
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO BUSCAR MÉTRICAS DE PROGRESSO ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            
            throw e
        }
    }

    /**
     * Busca métricas de tarefas pendentes do dashboard
     * @return TaskMetrics com dados de tarefas pendentes
     */
    suspend fun getTaskMetrics(): TaskMetrics {
        LogUtils.info("DashboardRepository", "📋 ========== INICIANDO BUSCA DE TAREFAS PENDENTES ==========")
        
        val startTime = System.currentTimeMillis()
        
        try {
            LogUtils.debug("DashboardRepository", "📡 Endpoint: ${ApiClient.getBaseUrl()}api/dashboard/task-metrics")
            
            LogUtils.info("DashboardRepository", "📞 Fazendo requisição para tarefas pendentes...")
            val response = apiService.getTaskMetrics()
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            if (response.isSuccessful) {
                val metrics = response.body()
                if (metrics != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== TAREFAS PENDENTES OBTIDAS ==========")
                    LogUtils.info("DashboardRepository", "📋 Contratos aguardando assinatura: ${metrics.contratosAguardandoAssinatura}")
                    LogUtils.info("DashboardRepository", "📦 Devoluções em atraso: ${metrics.devolucoesEmAtraso}")
                    LogUtils.info("DashboardRepository", "⚙️ Equipamentos para manutenção: ${metrics.equipamentosManutencao}")
                    LogUtils.info("DashboardRepository", "📊 Total de tarefas: ${metrics.totalTarefas}")
                    
                    return metrics
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta das tarefas pendentes é nula!")
                    throw Exception("Resposta das tarefas pendentes é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ Erro HTTP ${response.code()}: ${response.message()}")
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO BUSCAR TAREFAS PENDENTES ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            
            throw e
        }
    }
} 