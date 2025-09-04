package com.example.alg_gestao_02.ui.devolucao.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.data.repository.DevolucaoRepository
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para gerenciar a funcionalidade de Devoluções
 */
class DevolucoesViewModel(
    private val repository: DevolucaoRepository
) : ViewModel() {

    // Estado da UI para listagem de devoluções
    private val _uiState = MutableLiveData<UiState<List<Devolucao>>>()
    val uiState: LiveData<UiState<List<Devolucao>>> = _uiState

    // Estado da UI para detalhes de um grupo de devoluções (por dev_num)
    private val _devolucaoGrupoState = MutableLiveData<UiState<List<Devolucao>>>()
    val devolucaoGrupoState: LiveData<UiState<List<Devolucao>>> = _devolucaoGrupoState

    // Estado da operação de processamento de devolução
    private val _processamentoState = MutableLiveData<UiState<Devolucao>?>()
    val processamentoState: LiveData<UiState<Devolucao>?> = _processamentoState

    // Gerenciador de erros
    val errorHandler = ErrorViewModel()

    // Lista completa de devoluções (para filtragem local)
    private var allDevolucoes: List<Devolucao> = emptyList()

    // Filtros
    private val _statusFiltro = MutableLiveData<String?>(null)
    val statusFiltro: LiveData<String?> = _statusFiltro

    private val _clienteIdFiltro = MutableLiveData<Int?>(null)
    val clienteIdFiltro: LiveData<Int?> = _clienteIdFiltro

    private val _contratoIdFiltro = MutableLiveData<Int?>(null)
    val contratoIdFiltro: LiveData<Int?> = _contratoIdFiltro

    private val _devNumFiltro = MutableLiveData<String?>(null)
    val devNumFiltro: LiveData<String?> = _devNumFiltro

    private val _searchTerm = MutableLiveData<String>("")
    val searchTerm: LiveData<String> = _searchTerm

    init {
        loadDevolucoes()
    }

    /**
     * Carrega a lista de devoluções com base nos filtros atuais
     */
    fun loadDevolucoes() {
        _uiState.value = UiState.loading()

        viewModelScope.launch {
            try {
                LogUtils.debug("DevolucoesViewModel", "Carregando devoluções")

                // Aplicar filtros
                val resultado = repository.getDevolucoes(
                    status = _statusFiltro.value,
                    clienteId = _clienteIdFiltro.value,
                    contratoId = _contratoIdFiltro.value,
                    devNum = _devNumFiltro.value
                )

                when (resultado) {
                    is Resource.Success -> {
                        val devolucoes = resultado.data
                        if (devolucoes.isNotEmpty()) {
                            LogUtils.info("DevolucoesViewModel", "Devoluções carregadas: ${devolucoes.size}")
                            // Ordenar por data mais recente (usando data efetiva se existir, senão data prevista)
                            allDevolucoes = sortDevolucoesByMostRecent(devolucoes)
                            applySearchFilter(_searchTerm.value ?: "")
                        } else {
                            LogUtils.info("DevolucoesViewModel", "Nenhuma devolução encontrada")
                            allDevolucoes = emptyList()
                            _uiState.value = UiState.Empty()
                        }
                    }
                    is Resource.Error -> {
                        LogUtils.error("DevolucoesViewModel", "Erro ao carregar devoluções: ${resultado.message}")
                        _uiState.value = UiState.Error(resultado.message ?: "Erro desconhecido")
                    }
                    is Resource.Loading -> {
                        // Já estamos exibindo o estado de carregamento
                        LogUtils.debug("DevolucoesViewModel", "Carregando devoluções...")
                    }
                }
            } catch (e: Exception) {
                LogUtils.error("DevolucoesViewModel", "Erro ao carregar devoluções", e)
                _uiState.value = UiState.Error("Erro ao carregar devoluções: ${e.message}")
            }
        }
    }

    /**
     * Aplica o termo de busca à lista de devoluções
     */
    private fun applySearchFilter(searchTerm: String) {
        if (searchTerm.isBlank()) {
            _uiState.value = UiState.Success(allDevolucoes)
            return
        }

        val termLower = searchTerm.lowercase()
        val filtered = allDevolucoes.filter { devolucao ->
            devolucao.resolverNomeCliente().lowercase().contains(termLower) ||
                    devolucao.devNum.lowercase().contains(termLower) ||
                    devolucao.resolverNomeEquipamento().lowercase().contains(termLower)
        }

        if (filtered.isNotEmpty()) {
            // Manter a ordenação por data mais recente na lista filtrada
            val sortedFiltered = sortDevolucoesByMostRecent(filtered)
            _uiState.value = UiState.Success(sortedFiltered)
        } else {
            _uiState.value = UiState.Empty()
        }
    }

    /**
     * Define o termo de busca
     */
    fun setSearchTerm(term: String) {
        _searchTerm.value = term
        applySearchFilter(term)
    }

    /**
     * Define o filtro de status
     */
    fun setStatusFiltro(status: String?) {
        _statusFiltro.value = status
        loadDevolucoes()
    }

    /**
     * Define o filtro de cliente
     */
    fun setClienteIdFiltro(clienteId: Int?) {
        _clienteIdFiltro.value = clienteId
        loadDevolucoes()
    }

    /**
     * Define o filtro de contrato
     */
    fun setContratoIdFiltro(contratoId: Int?) {
        _contratoIdFiltro.value = contratoId
        loadDevolucoes()
    }

    /**
     * Define o filtro de número de devolução
     */
    fun setDevNumFiltro(devNum: String?) {
        _devNumFiltro.value = devNum
        loadDevolucoes()
    }

    /**
     * Limpa todos os filtros
     */
    fun clearFiltros() {
        _statusFiltro.value = null
        _clienteIdFiltro.value = null
        _contratoIdFiltro.value = null
        _devNumFiltro.value = null
        _searchTerm.value = ""
        loadDevolucoes()
    }

    /**
     * Carrega as devoluções agrupadas por dev_num
     */
    fun carregarDevolucoesPorDevNum(devNum: String) {
        _devolucaoGrupoState.value = UiState.loading()

        viewModelScope.launch {
            try {
                LogUtils.debug("DevolucoesViewModel", "Carregando devoluções por dev_num: $devNum")

                when (val resultado = repository.getDevolucoesByDevNum(devNum)) {
                    is Resource.Success -> {
                        val devolucoes = resultado.data
                        if (devolucoes.isNotEmpty()) {
                            LogUtils.info("DevolucoesViewModel", "Devoluções carregadas: ${devolucoes.size}")
                            _devolucaoGrupoState.value = UiState.Success(devolucoes)
                        } else {
                            LogUtils.info("DevolucoesViewModel", "Nenhuma devolução encontrada para dev_num: $devNum")
                            _devolucaoGrupoState.value = UiState.Empty()
                        }
                    }
                    is Resource.Error -> {
                        LogUtils.error("DevolucoesViewModel", "Erro ao carregar devoluções: ${resultado.message}")
                        _devolucaoGrupoState.value = UiState.Error(resultado.message ?: "Erro desconhecido")
                    }
                    is Resource.Loading -> {
                        // Já estamos exibindo o estado de carregamento
                        LogUtils.debug("DevolucoesViewModel", "Carregando devoluções por dev_num...")
                    }
                }
            } catch (e: Exception) {
                LogUtils.error("DevolucoesViewModel", "Erro ao carregar devoluções", e)
                _devolucaoGrupoState.value = UiState.Error("Erro ao carregar devoluções: ${e.message}")
            }
        }
    }

    /**
     * Carrega as devoluções de um contrato específico
     */
    fun carregarDevolucoesPorContrato(contratoId: Int) {
        _devolucaoGrupoState.value = UiState.loading()

        viewModelScope.launch {
            try {
                LogUtils.debug("DevolucoesViewModel", "Carregando devoluções por contrato: $contratoId")

                when (val resultado = repository.getDevolucoesByContratoId(contratoId)) {
                    is Resource.Success -> {
                        val devolucoes = resultado.data
                        if (devolucoes.isNotEmpty()) {
                            LogUtils.info("DevolucoesViewModel", "Devoluções carregadas: ${devolucoes.size}")
                            _devolucaoGrupoState.value = UiState.Success(devolucoes)
                        } else {
                            LogUtils.info("DevolucoesViewModel", "Nenhuma devolução encontrada para contrato: $contratoId")
                            _devolucaoGrupoState.value = UiState.Empty()
                        }
                    }
                    is Resource.Error -> {
                        LogUtils.error("DevolucoesViewModel", "Erro ao carregar devoluções: ${resultado.message}")
                        _devolucaoGrupoState.value = UiState.Error(resultado.message ?: "Erro desconhecido")
                    }
                    is Resource.Loading -> {
                        // Já estamos exibindo o estado de carregamento
                        LogUtils.debug("DevolucoesViewModel", "Carregando devoluções por contrato...")
                    }
                }
            } catch (e: Exception) {
                LogUtils.error("DevolucoesViewModel", "Erro ao carregar devoluções", e)
                _devolucaoGrupoState.value = UiState.Error("Erro ao carregar devoluções: ${e.message}")
            }
        }
    }

    /**
     * Processa um item de devolução
     */
    fun processarDevolucao(
        devolucaoId: Int,
        quantidadeDevolvida: Int,
        statusItemDevolucao: String,
        observacaoItemDevolucao: String? = null
    ) {
        LogUtils.info("DevolucoesViewModel", "🔄 INICIANDO PROCESSAMENTO NO VIEWMODEL")
        LogUtils.debug("DevolucoesViewModel", "Parâmetros recebidos - ID: $devolucaoId, " +
                "quantidade: $quantidadeDevolvida, status: $statusItemDevolucao, observação: $observacaoItemDevolucao")
        
        _processamentoState.value = UiState.loading()
        LogUtils.debug("DevolucoesViewModel", "Estado alterado para Loading")

        viewModelScope.launch {
            try {
                LogUtils.debug("DevolucoesViewModel", "Processando devolução ID: $devolucaoId, " +
                        "quantidade: $quantidadeDevolvida, status: $statusItemDevolucao")

                // Formatar data atual como data efetiva de devolução
                val formatoData = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val dataEfetiva = formatoData.format(Date())
                LogUtils.debug("DevolucoesViewModel", "Data efetiva formatada: $dataEfetiva")

                LogUtils.info("DevolucoesViewModel", "Chamando repository.processarDevolucao...")
                when (val resultado = repository.processarDevolucao(
                    id = devolucaoId,
                    quantidadeDevolvida = quantidadeDevolvida,
                    statusItemDevolucao = statusItemDevolucao,
                    dataDevolucaoEfetiva = dataEfetiva,
                    observacaoItemDevolucao = observacaoItemDevolucao
                )) {
                    is Resource.Success -> {
                        val devolucaoAtualizada = resultado.data
                        LogUtils.info("DevolucoesViewModel", "✅ SUCESSO NO VIEWMODEL - Devolução processada: ID=${devolucaoAtualizada.id}")
                        LogUtils.debug("DevolucoesViewModel", "Status atualizado para: ${devolucaoAtualizada.statusItemDevolucao}")
                        _processamentoState.value = UiState.Success(devolucaoAtualizada)

                        // Recarregar o grupo de devoluções para atualizar a UI
                        val devNum = devolucaoAtualizada.devNum
                        LogUtils.debug("DevolucoesViewModel", "Recarregando devoluções para dev_num: $devNum")
                        carregarDevolucoesPorDevNum(devNum)
                    }
                    
                    is Resource.Error -> {
                        LogUtils.error("DevolucoesViewModel", "❌ ERRO NO VIEWMODEL - Falha ao processar devolução: ${resultado.message}")
                        _processamentoState.value = UiState.Error(resultado.message ?: "Erro desconhecido")
                    }
                    
                    is Resource.Loading -> {
                        // Já estamos exibindo o estado de carregamento
                        LogUtils.debug("DevolucoesViewModel", "Processando devolução...")
                    }
                }
            } catch (e: Exception) {
                LogUtils.error("DevolucoesViewModel", "❌ EXCEÇÃO NO VIEWMODEL - Erro ao processar devolução", e)
                LogUtils.error("DevolucoesViewModel", "Tipo da exceção: ${e.javaClass.simpleName}")
                LogUtils.error("DevolucoesViewModel", "Mensagem: ${e.message}")
                _processamentoState.value = UiState.Error("Erro ao processar devolução: ${e.message}")
            }
        }
    }

    /**
     * Limpa o estado de processamento
     */
    fun clearProcessamentoState() {
        _processamentoState.value = null
    }

    /**
     * Função helper para ordenar devoluções por data mais recente
     */
    private fun sortDevolucoesByMostRecent(devolucoes: List<Devolucao>): List<Devolucao> {
        return devolucoes.sortedByDescending { devolucao ->
            try {
                val dataParaOrdenar = if (!devolucao.dataDevolucaoEfetiva.isNullOrEmpty()) {
                    devolucao.dataDevolucaoEfetiva
                } else {
                    devolucao.dataDevolucaoPrevista ?: ""
                }
                // Converter data para timestamp para ordenação correta
                val formato = if (dataParaOrdenar.contains("T")) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                }
                formato.parse(dataParaOrdenar)?.time ?: 0L
            } catch (e: Exception) {
                LogUtils.warning("DevolucoesViewModel", "Erro ao ordenar por data: ${e.message}")
                0L // Se não conseguir parsear, coloca no final
            }
        }
    }
}

/**
 * Factory para criação do ViewModel
 */
class DevolucoesViewModelFactory(
    private val repository: DevolucaoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevolucoesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DevolucoesViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
} 