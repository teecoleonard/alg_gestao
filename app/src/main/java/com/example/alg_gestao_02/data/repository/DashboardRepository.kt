package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.DashboardStats
import com.example.alg_gestao_02.data.models.FinancialMetrics
import com.example.alg_gestao_02.data.models.ProgressMetrics
import com.example.alg_gestao_02.data.models.TaskMetrics
import com.example.alg_gestao_02.data.models.ReceitaClienteResponse
import com.example.alg_gestao_02.data.models.ResumoMensalCliente
import com.example.alg_gestao_02.data.models.ContratoResumo
import com.example.alg_gestao_02.data.models.DevolucaoResumo
import com.example.alg_gestao_02.data.models.ConfirmarPagamentoRequest
import com.example.alg_gestao_02.data.models.ConfirmarPagamentoResponse
import com.example.alg_gestao_02.data.models.GerarPdfResumoRequest
import com.example.alg_gestao_02.data.models.PdfResumoResponse
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

    /**
     * Busca receita mensal por cliente
     * @param mes Mês para filtrar (1-12), opcional
     * @param ano Ano para filtrar, opcional
     * @return ReceitaClienteResponse com lista de receita por cliente
     */
    suspend fun getReceitaPorCliente(mes: Int? = null, ano: Int? = null): ReceitaClienteResponse {
        LogUtils.info("DashboardRepository", "💰 ========== INICIANDO BUSCA DE RECEITA POR CLIENTE ==========")
        
        if (mes != null && ano != null) {
            LogUtils.info("DashboardRepository", "📅 Filtro aplicado: $mes/$ano")
        } else {
            LogUtils.info("DashboardRepository", "📅 Sem filtro de período (dados gerais)")
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            val endpoint = if (mes != null && ano != null) {
                "${ApiClient.getBaseUrl()}api/dashboard/receita-por-cliente?mes=$mes&ano=$ano"
            } else {
                "${ApiClient.getBaseUrl()}api/dashboard/receita-por-cliente"
            }
            
            LogUtils.debug("DashboardRepository", "📡 Endpoint: $endpoint")
            
            LogUtils.info("DashboardRepository", "📞 Fazendo requisição para receita por cliente...")
            val response = if (mes != null && ano != null) {
                apiService.getReceitaPorClienteComFiltro(mes, ano)
            } else {
                apiService.getReceitaPorCliente()
            }
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            if (response.isSuccessful) {
                val receitaResponse = response.body()
                if (receitaResponse != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== RECEITA POR CLIENTE OBTIDA ==========")
                    LogUtils.info("DashboardRepository", "👥 Total de clientes: ${receitaResponse.totalClientes}")
                    LogUtils.info("DashboardRepository", "💰 Total geral: R$ ${String.format("%.2f", receitaResponse.totalGeral)}")
                    
                    // Log dos top 5 clientes por receita
                    val topClientes = receitaResponse.clientes.take(5)
                    LogUtils.info("DashboardRepository", "🏆 TOP 5 CLIENTES POR RECEITA:")
                    topClientes.forEachIndexed { index, cliente ->
                        LogUtils.info("DashboardRepository", "   ${index + 1}. ${cliente.clienteNome}: ${cliente.getValorMensalFormatado()}")
                    }
                    
                    return receitaResponse
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta da receita por cliente é nula!")
                    throw Exception("Resposta da receita por cliente é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ Erro HTTP ${response.code()}: ${response.message()}")
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO BUSCAR RECEITA POR CLIENTE ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            
            throw e
        }
    }

    /**
     * Busca resumo mensal detalhado de um cliente específico
     * @param clienteId ID do cliente
     * @param mesReferencia Mês de referência (formato: yyyy-MM)
     * @return ResumoMensalCliente com dados detalhados
     */
    suspend fun getResumoMensalCliente(clienteId: Int, mesReferencia: String): ResumoMensalCliente {
        LogUtils.info("DashboardRepository", "📊 ========== INICIANDO BUSCA DE RESUMO MENSAL ==========")
        LogUtils.info("DashboardRepository", "👤 Cliente ID: $clienteId")
        LogUtils.info("DashboardRepository", "📅 Mês: $mesReferencia")
        
        val startTime = System.currentTimeMillis()
        
        try {
            LogUtils.debug("DashboardRepository", "📡 Endpoint: ${ApiClient.getBaseUrl()}api/dashboard/resumo-mensal-cliente/$clienteId?mes=$mesReferencia")
            
            val response = apiService.getResumoMensalCliente(clienteId, mesReferencia)
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            if (response.isSuccessful) {
                val resumo = response.body()
                if (resumo != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== RESUMO MENSAL OBTIDO ==========")
                    LogUtils.info("DashboardRepository", "👤 Cliente: ${resumo.clienteNome}")
                    LogUtils.info("DashboardRepository", "💰 Valor mensal: ${resumo.getValorMensalFormatado()}")
                    LogUtils.info("DashboardRepository", "💳 Status pagamento: ${resumo.statusPagamento}")
                    LogUtils.info("DashboardRepository", "📋 Contratos ativos: ${resumo.contratosAtivos}")
                    LogUtils.info("DashboardRepository", "📦 Devoluções no mês: ${resumo.devolucoesMes}")
                    
                    return resumo
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta do resumo mensal é nula!")
                    throw Exception("Resposta do resumo mensal é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ Erro HTTP ${response.code()}: ${response.message()}")
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO BUSCAR RESUMO MENSAL ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            
            // Fallback para dados simulados
            LogUtils.warning("DashboardRepository", "🔄 Usando dados simulados para resumo mensal...")
            return gerarResumoMensalSimulado(clienteId, mesReferencia)
        }
    }

    /**
     * Gera dados simulados para resumo mensal baseado no período selecionado
     * @param clienteId ID do cliente
     * @param mesReferencia Mês de referência (formato: yyyy-MM)
     * @return ResumoMensalCliente com dados simulados variando por período
     */
    private fun gerarResumoMensalSimulado(clienteId: Int, mesReferencia: String): ResumoMensalCliente {
        // Base de dados de clientes simulados
        val clientesSimulados = mapOf(
            1 to "João Silva & Cia",
            2 to "Maria Oliveira LTDA",
            3 to "Pedro Santos Eventos",
            4 to "Ana Costa Produções",
            5 to "Carlos Ferreira & Associados",
            6 to "Luciana Almeida ME",
            7 to "Roberto Nascimento",
            8 to "Fernanda Lima Eventos",
            9 to "José Carlos & Filhos",
            10 to "Patrícia Rodrigues"
        )
        
        val nomeCliente = clientesSimulados[clienteId] ?: "Cliente Simulado $clienteId"
        
        // Extrair ano e mês para gerar dados variáveis
        val (ano, mes) = try {
            val partes = mesReferencia.split("-")
            Pair(partes[0].toInt(), partes[1].toInt())
        } catch (e: Exception) {
            Pair(2024, 1)
        }
        
        // Gerar dados variáveis baseados no mês/ano/cliente
        val semente = (clienteId * 1000) + (ano * 12) + mes
        val random = java.util.Random(semente.toLong())
        
        // Valores que variam por período
        val valorBase = 1500.0 + (clienteId * 200.0)
        val variacao = (random.nextDouble() - 0.5) * 500.0 // -250 a +250
        val valorMensal = maxOf(500.0, valorBase + variacao)
        
        val contratosAtivos = 2 + (clienteId % 3) + (mes % 2)
        val contratosMes = if (mes in listOf(1, 3, 6, 9)) random.nextInt(2) else 0
        val devolucoesMes = if (mes in listOf(2, 5, 8, 11)) random.nextInt(2) else 0
        val valorDevolucoes = devolucoesMes * (200.0 + random.nextDouble() * 300.0)
        
        // Status varia por mês
        val status = when (mes % 4) {
            0 -> "PAGO"
            1 -> "PENDENTE" 
            2 -> if (random.nextBoolean()) "PAGO" else "PENDENTE"
            else -> "ATRASADO"
        }
        
        // Gerar data de vencimento
        val dataVencimento = String.format("%04d-%02d-%02d", ano, mes, 5 + random.nextInt(10))
        val dataPagamento = if (status == "PAGO") 
            String.format("%04d-%02d-%02d", ano, mes, 3 + random.nextInt(10)) else null
        
        // Contratos detalhados variando por período
        val contratosDetalhes = mutableListOf<ContratoResumo>()
        for (i in 1..contratosAtivos) {
            val contratoValor = valorMensal / contratosAtivos + (random.nextDouble() - 0.5) * 100
            contratosDetalhes.add(
                ContratoResumo(
                    contratoId = (clienteId * 100) + i,
                    contratoNum = String.format("%03d", (clienteId * 10) + i),
                    valorMensal = maxOf(100.0, contratoValor),
                    periodo = "${mes}/${ano}",
                    dataAssinatura = String.format("%04d-%02d-%02d", ano - random.nextInt(2), 
                        1 + random.nextInt(12), 1 + random.nextInt(28)),
                    status = if (i <= contratosMes) "NOVO" else "ATIVO"
                )
            )
        }
        
        // Devoluções detalhadas variando por período
        val devolucoesDetalhes = mutableListOf<DevolucaoResumo>()
        for (i in 1..devolucoesMes) {
            val valorMulta = 150.0 + random.nextDouble() * 200.0
            devolucoesDetalhes.add(
                DevolucaoResumo(
                    devolucaoId = (clienteId * 1000) + (mes * 10) + i,
                    numeroDevolucao = String.format("DEV%03d%02d%02d", clienteId, mes, i),
                    valorMulta = valorMulta,
                    dataDevolucao = String.format("%04d-%02d-%02d", ano, mes, 
                        10 + random.nextInt(15)),
                    status = "PROCESSADA",
                    equipamentoNome = listOf("Notebook Dell", "Projetor Epson", "Sound System", 
                        "Mesa Redonda", "Cadeiras Plásticas")[random.nextInt(5)]
                )
            )
        }
        
        val ticketMedio = if (contratosAtivos > 0) valorMensal / contratosAtivos else 0.0
        
        LogUtils.info("DashboardRepository", "🔄 Dados simulados gerados para $mesReferencia:")
        LogUtils.info("DashboardRepository", "   💰 Valor: R$ ${String.format("%.2f", valorMensal)}")
        LogUtils.info("DashboardRepository", "   📋 Contratos: $contratosAtivos ativos, $contratosMes novos")
        LogUtils.info("DashboardRepository", "   📦 Devoluções: $devolucoesMes no mês")
        LogUtils.info("DashboardRepository", "   💳 Status: $status")
        
        return ResumoMensalCliente(
            clienteId = clienteId,
            clienteNome = nomeCliente,
            mesReferencia = mesReferencia,
            valorMensal = valorMensal,
            totalContratos = contratosAtivos,
            contratosAtivos = contratosAtivos,
            contratosMes = contratosMes,
            devolucoesMes = devolucoesMes,
            valorDevolucoes = valorDevolucoes,
            valorTotalPagar = valorMensal + valorDevolucoes,
            statusPagamento = status,
            dataVencimento = dataVencimento,
            dataPagamento = dataPagamento,
            observacoes = if (devolucoesMes > 0) "Período com devoluções registradas" else null,
            ticketMedio = ticketMedio,
            contratosDetalhes = contratosDetalhes,
            devolucoesDetalhes = devolucoesDetalhes
        )
    }

    /**
     * Confirma pagamento de um cliente
     * @param request Dados da confirmação de pagamento
     * @return ConfirmarPagamentoResponse com resultado da operação
     */
    suspend fun confirmarPagamento(request: ConfirmarPagamentoRequest): ConfirmarPagamentoResponse {
        LogUtils.info("DashboardRepository", "💳 ========== CONFIRMANDO PAGAMENTO ==========")
        LogUtils.info("DashboardRepository", "👤 Cliente ID: ${request.clienteId}")
        LogUtils.info("DashboardRepository", "📅 Mês: ${request.mesReferencia}")
        LogUtils.info("DashboardRepository", "💰 Valor: R$ ${String.format("%.2f", request.valorPago)}")
        
        val startTime = System.currentTimeMillis()
        
        try {
            LogUtils.debug("DashboardRepository", "📡 Endpoint: ${ApiClient.getBaseUrl()}api/dashboard/confirmar-pagamento")
            
            val response = apiService.confirmarPagamento(request)
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            if (response.isSuccessful) {
                val confirmacao = response.body()
                if (confirmacao != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== PAGAMENTO CONFIRMADO ==========")
                    LogUtils.info("DashboardRepository", "✅ Sucesso: ${confirmacao.sucesso}")
                    LogUtils.info("DashboardRepository", "📝 Mensagem: ${confirmacao.mensagem}")
                    
                    return confirmacao
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta da confirmação é nula!")
                    throw Exception("Resposta da confirmação é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ Erro HTTP ${response.code()}: ${response.message()}")
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO CONFIRMAR PAGAMENTO ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            
            throw e
        }
    }

    /**
     * Gera PDF de resumo mensal
     * @param request Parâmetros para geração do PDF
     * @return PdfResumoResponse com informações do PDF gerado
     */
    suspend fun gerarPdfResumoMensal(request: GerarPdfResumoRequest): PdfResumoResponse {
        LogUtils.info("DashboardRepository", "📄 ========== GERANDO PDF RESUMO MENSAL ==========")
        LogUtils.info("DashboardRepository", "📅 Mês: ${request.mesReferencia}")
        LogUtils.info("DashboardRepository", "👥 Clientes: ${request.clienteIds?.size ?: "todos"}")
        LogUtils.info("DashboardRepository", "📋 Tipo: ${request.tipoRelatorio}")
        
        val startTime = System.currentTimeMillis()
        
        try {
            LogUtils.debug("DashboardRepository", "📡 Endpoint: ${ApiClient.getBaseUrl()}api/dashboard/gerar-pdf-resumo-mensal")
            
            val response = apiService.gerarPdfResumoMensal(request)
            
            val requestTime = System.currentTimeMillis() - startTime
            LogUtils.info("DashboardRepository", "⏱️ Tempo de resposta: ${requestTime}ms")
            
            if (response.isSuccessful) {
                val pdfResponse = response.body()
                if (pdfResponse != null) {
                    LogUtils.info("DashboardRepository", "✅ ========== PDF GERADO COM SUCESSO ==========")
                    LogUtils.info("DashboardRepository", "📄 Arquivo: ${pdfResponse.nomeArquivo}")
                    LogUtils.info("DashboardRepository", "💾 Tamanho: ${pdfResponse.tamanhoArquivo} bytes")
                    LogUtils.info("DashboardRepository", "🔗 URL: ${pdfResponse.urlDownload}")
                    
                    return pdfResponse
                } else {
                    LogUtils.error("DashboardRepository", "❌ ERRO: Resposta da geração de PDF é nula!")
                    throw Exception("Resposta da geração de PDF é nula")
                }
            } else {
                LogUtils.error("DashboardRepository", "❌ Erro HTTP ${response.code()}: ${response.message()}")
                throw Exception("Erro HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            LogUtils.error("DashboardRepository", "❌ ========== ERRO AO GERAR PDF ==========")
            LogUtils.error("DashboardRepository", "⏱️ Tempo total: ${totalTime}ms")
            LogUtils.error("DashboardRepository", "📝 Mensagem: ${e.message}")
            
            throw e
        }
    }
} 