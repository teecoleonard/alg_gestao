package com.example.alg_gestao_02.ui.financial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.FinancialMetrics
import com.example.alg_gestao_02.data.models.ProgressMetrics
import com.example.alg_gestao_02.data.models.ReceitaClienteResponse
import com.example.alg_gestao_02.data.models.ResumoMensalCliente
import com.example.alg_gestao_02.data.models.ConfirmarPagamentoRequest
import com.example.alg_gestao_02.data.models.ConfirmarPagamentoResponse
import com.example.alg_gestao_02.data.models.GerarPdfResumoRequest
import com.example.alg_gestao_02.data.models.PdfResumoResponse
import com.example.alg_gestao_02.data.repository.DashboardRepository
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gerenciamento financeiro
 */
class FinancialViewModel(private val repository: DashboardRepository = DashboardRepository()) : ViewModel() {
    
    // LiveData para o estado da UI
    private val _uiState = MutableLiveData<UiState<Any>>()
    val uiState: LiveData<UiState<Any>> = _uiState
    
    // LiveData para métricas financeiras
    private val _financialMetrics = MutableLiveData<FinancialMetrics>()
    val financialMetrics: LiveData<FinancialMetrics> = _financialMetrics
    
    // LiveData para métricas de progresso
    private val _progressMetrics = MutableLiveData<ProgressMetrics>()
    val progressMetrics: LiveData<ProgressMetrics> = _progressMetrics
    
    // LiveData para receita por cliente
    private val _receitaPorCliente = MutableLiveData<ReceitaClienteResponse>()
    val receitaPorCliente: LiveData<ReceitaClienteResponse> = _receitaPorCliente
    
    // LiveData para resumo mensal de cliente específico
    private val _resumoMensalCliente = MutableLiveData<ResumoMensalCliente>()
    val resumoMensalCliente: LiveData<ResumoMensalCliente> = _resumoMensalCliente
    
    // LiveData para confirmação de pagamento
    private val _confirmacaoPagamento = MutableLiveData<ConfirmarPagamentoResponse>()
    val confirmacaoPagamento: LiveData<ConfirmarPagamentoResponse> = _confirmacaoPagamento
    
    // LiveData para geração de PDF
    private val _pdfGerado = MutableLiveData<PdfResumoResponse>()
    val pdfGerado: LiveData<PdfResumoResponse> = _pdfGerado
    
    // Meta personalizada (em produção, seria salva no servidor)
    private var metaPersonalizada: Double? = null
    
    init {
        LogUtils.info("FinancialViewModel", "🚀 ========== INICIALIZANDO FINANCIAL VIEWMODEL ==========")
        loadFinancialData()
    }
    
    /**
     * Carrega todos os dados financeiros
     */
    private fun loadFinancialData() {
        LogUtils.info("FinancialViewModel", "📥 ========== CARREGANDO DADOS FINANCEIROS ==========")
        
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                LogUtils.info("FinancialViewModel", "🔄 Carregando métricas financeiras...")
                val startTime = System.currentTimeMillis()
                
                // Carregar métricas financeiras
                val financialMetrics = try {
                    repository.getFinancialMetrics()
                } catch (e: Exception) {
                    LogUtils.warning("FinancialViewModel", "⚠️ Erro ao carregar métricas financeiras: ${e.message}")
                    null
                }
                
                // Carregar métricas de progresso
                val progressMetrics = try {
                    val originalProgress = repository.getProgressMetrics()
                    // Aplicar meta personalizada se existir
                    if (metaPersonalizada != null) {
                        originalProgress.copy(
                            receitaMeta = metaPersonalizada!!
                        )
                    } else {
                        originalProgress
                    }
                } catch (e: Exception) {
                    LogUtils.warning("FinancialViewModel", "⚠️ Erro ao carregar métricas de progresso: ${e.message}")
                    null
                }
                
                // Carregar receita por cliente
                val receitaCliente = try {
                    repository.getReceitaPorCliente()
                } catch (e: Exception) {
                    LogUtils.warning("FinancialViewModel", "⚠️ Erro ao carregar receita por cliente: ${e.message}")
                    null
                }
                
                val loadTime = System.currentTimeMillis() - startTime
                LogUtils.info("FinancialViewModel", "⏱️ Tempo total de carregamento: ${loadTime}ms")
                
                // Atualizar LiveData
                if (financialMetrics != null) {
                    LogUtils.info("FinancialViewModel", "💰 Métricas financeiras carregadas com sucesso!")
                    _financialMetrics.value = financialMetrics
                }
                
                if (progressMetrics != null) {
                    LogUtils.info("FinancialViewModel", "📊 Métricas de progresso carregadas com sucesso!")
                    _progressMetrics.value = progressMetrics
                }
                
                if (receitaCliente != null) {
                    LogUtils.info("FinancialViewModel", "👥 Receita por cliente carregada com sucesso!")
                    _receitaPorCliente.value = receitaCliente
                }
                
                _uiState.value = UiState.Success("Dados carregados com sucesso")
                LogUtils.info("FinancialViewModel", "✅ ========== CARREGAMENTO CONCLUÍDO ==========")
                
            } catch (e: Exception) {
                LogUtils.error("FinancialViewModel", "❌ ========== ERRO NO CARREGAMENTO ==========")
                LogUtils.error("FinancialViewModel", "📝 Mensagem: ${e.message}")
                LogUtils.error("FinancialViewModel", "📚 Stack trace:", e)
                
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Sem conexão com a internet"
                    is java.net.ConnectException -> "Servidor indisponível"
                    is java.net.SocketTimeoutException -> "Tempo limite excedido"
                    else -> "Erro ao carregar dados financeiros: ${e.message ?: "Erro desconhecido"}"
                }
                
                _uiState.value = UiState.Error(errorMessage)
            }
        }
    }
    
    /**
     * Atualiza os dados financeiros
     */
    fun refreshFinancialData() {
        LogUtils.info("FinancialViewModel", "🔄 ========== REFRESH SOLICITADO ==========")
        loadFinancialData()
    }
    
    /**
     * Define uma nova meta de receita
     */
    fun definirMeta(novaMeta: Double) {
        LogUtils.info("FinancialViewModel", "🎯 ========== DEFININDO NOVA META ==========")
        LogUtils.info("FinancialViewModel", "💰 Nova meta: R$ ${String.format("%.2f", novaMeta)}")
        
        metaPersonalizada = novaMeta
        
        // Atualizar métricas de progresso com nova meta
        val currentProgress = _progressMetrics.value
        if (currentProgress != null) {
            val updatedProgress = ProgressMetrics(
                contratosMeta = currentProgress.contratosMeta,
                contratosAtual = currentProgress.contratosAtual,
                receitaMeta = novaMeta,
                receitaAtual = currentProgress.receitaAtual,
                clientesMeta = currentProgress.clientesMeta,
                clientesAtual = currentProgress.clientesAtual,
                satisfacaoPercentual = currentProgress.satisfacaoPercentual
            )
            
            _progressMetrics.value = updatedProgress
            LogUtils.info("FinancialViewModel", "✅ Meta atualizada com sucesso!")
        }
        
        // TODO: Em produção, salvar a meta no servidor
        // repository.salvarMeta(novaMeta)
    }
    
    /**
     * Filtra dados por período
     */
    fun filtrarPorPeriodo(dataInicio: Date, dataFim: Date) {
        LogUtils.info("FinancialViewModel", "📅 ========== FILTRANDO POR PERÍODO ==========")
        LogUtils.info("FinancialViewModel", "📅 Período: ${dataInicio} até ${dataFim}")
        
        // TODO: Implementar filtro por período na API
        // Por enquanto, simular carregamento
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                // Simular delay de carregamento
                kotlinx.coroutines.delay(1000)
                
                // TODO: Chamar API com parâmetros de data
                // val filteredData = repository.getFinancialMetricsByPeriod(dataInicio, dataFim)
                
                // Por enquanto, recarregar dados normais
                loadFinancialData()
                
                LogUtils.info("FinancialViewModel", "✅ Filtro aplicado com sucesso!")
                
            } catch (e: Exception) {
                LogUtils.error("FinancialViewModel", "❌ Erro ao aplicar filtro: ${e.message}")
                _uiState.value = UiState.Error("Erro ao aplicar filtro: ${e.message}")
            }
        }
    }
    
    /**
     * Remove filtro de período
     */
    fun limparFiltro() {
        LogUtils.info("FinancialViewModel", "🗑️ ========== LIMPANDO FILTRO ==========")
        
        // Recarregar dados sem filtro
        loadFinancialData()
        
        LogUtils.info("FinancialViewModel", "✅ Filtro removido com sucesso!")
    }
    
    /**
     * Obtém resumo financeiro formatado
     */
    fun getResumoFinanceiro(): String {
        val financial = _financialMetrics.value
        val progress = _progressMetrics.value
        
        return if (financial != null && progress != null) {
            """
            💰 RESUMO FINANCEIRO
            ====================
            
            Valor Total Ativo: R$ ${String.format("%.2f", financial.valorTotalAtivo)}
            Receita Mensal: R$ ${String.format("%.2f", financial.receitaMensal)}
            Ticket Médio: R$ ${String.format("%.2f", financial.ticketMedio)}
            
            Meta de Receita: R$ ${String.format("%.2f", progress.receitaMeta)}
            Progresso: ${progress.receitaPercentual}%
            
            ${if (progress.receitaPercentual >= 100) "🎉 Meta atingida!" else "💪 Continue assim!"}
            """.trimIndent()
        } else {
            "Dados financeiros não disponíveis"
        }
    }
    
    /**
     * Verifica se a meta foi atingida
     */
    fun isMetaAtingida(): Boolean {
        return _progressMetrics.value?.receitaPercentual?.let { it >= 100 } ?: false
    }
    
    /**
     * Calcula valor restante para atingir a meta
     */
    fun getValorRestanteMeta(): Double {
        val progress = _progressMetrics.value
        return if (progress != null) {
            maxOf(0.0, progress.receitaMeta - progress.receitaAtual)
        } else {
            0.0
        }
    }
    
    /**
     * Busca resumo mensal detalhado de um cliente
     */
    fun buscarResumoMensalCliente(clienteId: Int, mesReferencia: String? = null) {
        LogUtils.info("FinancialViewModel", "📊 ========== BUSCANDO RESUMO MENSAL DO CLIENTE ==========")
        LogUtils.info("FinancialViewModel", "👤 Cliente ID: $clienteId")
        
        val mes = mesReferencia ?: SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        LogUtils.info("FinancialViewModel", "📅 Mês: $mes")
        
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                val resumo = repository.getResumoMensalCliente(clienteId, mes)
                
                LogUtils.info("FinancialViewModel", "✅ Resumo mensal carregado com sucesso!")
                _resumoMensalCliente.value = resumo
                _uiState.value = UiState.Success("Resumo carregado com sucesso")
                
            } catch (e: Exception) {
                LogUtils.error("FinancialViewModel", "❌ Erro ao buscar resumo mensal: ${e.message}")
                _uiState.value = UiState.Error("Erro ao carregar resumo: ${e.message}")
            }
        }
    }

    /**
     * Confirma pagamento de um cliente
     */
    fun confirmarPagamento(
        clienteId: Int,
        mesReferencia: String,
        valorPago: Double,
        observacoes: String? = null
    ) {
        LogUtils.info("FinancialViewModel", "💳 ========== CONFIRMANDO PAGAMENTO ==========")
        LogUtils.info("FinancialViewModel", "👤 Cliente ID: $clienteId")
        LogUtils.info("FinancialViewModel", "💰 Valor: R$ ${String.format("%.2f", valorPago)}")
        
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                val request = ConfirmarPagamentoRequest(
                    clienteId = clienteId,
                    mesReferencia = mesReferencia,
                    valorPago = valorPago,
                    dataPagamento = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    observacoes = observacoes
                )
                
                val response = repository.confirmarPagamento(request)
                
                LogUtils.info("FinancialViewModel", "✅ Pagamento confirmado com sucesso!")
                _confirmacaoPagamento.value = response
                
                // Atualizar resumo do cliente se disponível
                if (response.resumoAtualizado != null) {
                    _resumoMensalCliente.value = response.resumoAtualizado
                }
                
                _uiState.value = UiState.Success("Pagamento confirmado")
                
            } catch (e: Exception) {
                LogUtils.error("FinancialViewModel", "❌ Erro ao confirmar pagamento: ${e.message}")
                _uiState.value = UiState.Error("Erro ao confirmar pagamento: ${e.message}")
            }
        }
    }

    /**
     * Gera PDF de resumo mensal
     */
    fun gerarPdfResumoMensal(
        mesReferencia: String? = null,
        clienteIds: List<Int>? = null,
        tipoRelatorio: String = "COMPLETO"
    ) {
        LogUtils.info("FinancialViewModel", "📄 ========== GERANDO PDF RESUMO MENSAL ==========")
        
        val mes = mesReferencia ?: SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        LogUtils.info("FinancialViewModel", "📅 Mês: $mes")
        LogUtils.info("FinancialViewModel", "👥 Clientes: ${clienteIds?.size ?: "todos"}")
        
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                val request = GerarPdfResumoRequest(
                    mesReferencia = mes,
                    clienteIds = clienteIds,
                    incluirDetalhes = true,
                    tipoRelatorio = tipoRelatorio
                )
                
                val response = repository.gerarPdfResumoMensal(request)
                
                LogUtils.info("FinancialViewModel", "✅ PDF gerado com sucesso!")
                LogUtils.info("FinancialViewModel", "📄 Arquivo: ${response.nomeArquivo}")
                
                _pdfGerado.value = response
                _uiState.value = UiState.Success("PDF gerado com sucesso")
                
            } catch (e: Exception) {
                LogUtils.error("FinancialViewModel", "❌ Erro ao gerar PDF: ${e.message}")
                _uiState.value = UiState.Error("Erro ao gerar PDF: ${e.message}")
            }
        }
    }

    /**
     * Limpa o resumo mensal atual
     */
    fun limparResumoMensal() {
        _resumoMensalCliente.value = null
    }
} 