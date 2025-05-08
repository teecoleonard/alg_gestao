package com.example.alg_gestao_02.ui.contrato.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.data.repository.ClienteRepository
import com.example.alg_gestao_02.data.repository.ContratoRepository
import com.example.alg_gestao_02.data.repository.EquipamentoContratoRepository
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar a tela de listagem e CRUD de contratos
 */
class ContratosViewModel(
    private val repository: ContratoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoContratoRepository: EquipamentoContratoRepository // Mantido apenas para leitura
) : ViewModel() {

    // Estado da UI para listagem de contratos
    private val _uiState = MutableLiveData<UiState<List<Contrato>>>()
    val uiState: LiveData<UiState<List<Contrato>>> = _uiState
    
    // Estado da operação de criação/edição/exclusão
    private val _operationState = MutableLiveData<UiState<Contrato>?>()
    val operationState: LiveData<UiState<Contrato>?> = _operationState
    
    // Lista de clientes para seleção
    private val _clientesState = MutableLiveData<UiState<List<Cliente>>>()
    val clientesState: LiveData<UiState<List<Cliente>>> = _clientesState
    
    // ViewModel para tratamento de erros
    val errorHandler = ErrorViewModel()
    
    // Cliente selecionado para novo contrato
    private val _selectedCliente = MutableLiveData<Cliente?>()
    val selectedCliente: LiveData<Cliente?> = _selectedCliente
    
    // Lista completa de contratos (para filtragem local)
    private var allContratos: List<Contrato> = emptyList()
    
    // Termo de busca atual
    private val _searchTerm = MutableLiveData<String>("")
    val searchTerm: LiveData<String> = _searchTerm
    
    // LiveData para equipamentos de contrato
    private val _equipamentosContratoState = MutableLiveData<UiState<List<EquipamentoContrato>>>()
    val equipamentosContratoState: LiveData<UiState<List<EquipamentoContrato>>> = _equipamentosContratoState
    
    // LiveData para o contrato selecionado com todos os detalhes (incluindo equipamentos)
    private val _contratoDetalhado = MutableLiveData<UiState<Contrato?>>()
    val contratoDetalhado: LiveData<UiState<Contrato?>> = _contratoDetalhado
    
    init {
        loadContratos()
        loadClientes()
    }
    
    /**
     * Carrega a lista de contratos
     */
    fun loadContratos() {
        _uiState.value = UiState.loading()
        
        viewModelScope.launch {
            LogUtils.debug("ContratosViewModel", "Carregando contratos")
            
            when (val result = repository.getContratos()) {
                is Resource.Success -> {
                    val contratos = result.data
                    if (contratos.isNotEmpty()) {
                        LogUtils.info("ContratosViewModel", "Contratos carregados com sucesso: ${contratos.size}")
                        allContratos = contratos
                        applySearchFilter(_searchTerm.value ?: "")
                    } else {
                        LogUtils.info("ContratosViewModel", "Nenhum contrato encontrado")
                        allContratos = emptyList()
                        _uiState.value = UiState.Empty()
                    }
                }
                
                is Resource.Error -> {
                    if (result.message == "Operação cancelada") {
                        // Não exibir erro para operações canceladas normalmente
                        LogUtils.debug("ContratosViewModel", "Carregamento de contratos cancelado")
                    } else {
                        LogUtils.error("ContratosViewModel", "Erro ao carregar contratos: ${result.message}")
                        _uiState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ContratosViewModel", "Estado desconhecido ao carregar contratos")
                    _uiState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
    
    /**
     * Carrega a lista de clientes para seleção na criação de contrato
     */
    fun loadClientes() {
        _clientesState.value = UiState.loading()
        
        viewModelScope.launch {
            LogUtils.debug("ContratosViewModel", "Carregando clientes para seleção")
            
            when (val result = clienteRepository.getClientes()) {
                is Resource.Success -> {
                    val clientes = result.data
                    if (clientes.isNotEmpty()) {
                        LogUtils.info("ContratosViewModel", "Clientes carregados com sucesso: ${clientes.size}")
                        _clientesState.value = UiState.Success(clientes)
                    } else {
                        LogUtils.info("ContratosViewModel", "Nenhum cliente encontrado")
                        _clientesState.value = UiState.Empty()
                    }
                }
                
                is Resource.Error -> {
                    LogUtils.error("ContratosViewModel", "Erro ao carregar clientes: ${result.message}")
                    _clientesState.value = UiState.Error(result.message)
                }
                
                else -> {
                    LogUtils.error("ContratosViewModel", "Estado desconhecido ao carregar clientes")
                    _clientesState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
    
    /**
     * Seleciona um cliente para criação de contrato
     */
    fun selectCliente(cliente: Cliente) {
        _selectedCliente.value = cliente
    }
    
    /**
     * Limpa o cliente selecionado
     */
    fun clearSelectedCliente() {
        _selectedCliente.value = null
    }
    
    /**
     * Cria um novo contrato
     */
    fun criarContrato(contrato: Contrato, equipamentos: List<EquipamentoContrato> = emptyList()) {
        _operationState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                LogUtils.debug("ContratosViewModel", "Criando contrato: ${contrato.contratoNum}")
                
                // Gerar ID temporário se for um novo contrato (id == 0)
                val contratoComId = if (contrato.id == 0) {
                    val tempId = Contrato.generateTempId()
                    LogUtils.debug("ContratosViewModel", "Gerado ID temporário: $tempId")
                    contrato.copy(id = tempId)
                } else {
                    contrato
                }
                
                // Vincular equipamentos ao contrato
                val contratoComEquipamentos = if (equipamentos.isNotEmpty()) {
                    LogUtils.debug("ContratosViewModel", "Com ${equipamentos.size} equipamentos")
                    val equipamentosAtualizados = equipamentos.map { 
                        it.copy(contratoId = contratoComId.id) 
                    }
                    contratoComId.copy(equipamentos = equipamentosAtualizados)
                } else {
                    contratoComId
                }
                
                // Salvar contrato com equipamentos
                val resultado = repository.createContrato(contratoComEquipamentos)
                
                if (resultado.isSuccess()) {
                    val novoContrato = resultado.data!!
                    LogUtils.debug("ContratosViewModel", "Contrato criado com ID: ${novoContrato.id}")
                    
                    _operationState.value = UiState.Success(novoContrato)
                    loadContratos()
                } else {
                    LogUtils.error("ContratosViewModel", "Erro ao criar contrato: ${resultado.message}")
                    _operationState.value = UiState.Error(resultado.message ?: "Erro desconhecido ao criar contrato")
                }
            } catch (e: Exception) {
                LogUtils.error("ContratosViewModel", "Erro ao criar contrato", e)
                _operationState.value = UiState.Error("Erro ao criar contrato: ${e.message}")
            }
        }
    }
    
    /**
     * Atualiza um contrato existente
     */
    fun atualizarContrato(id: Int, contrato: Contrato, equipamentos: List<EquipamentoContrato> = emptyList()) {
        _operationState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                LogUtils.debug("ContratosViewModel", "Atualizando contrato: $id")
                
                // Vincular equipamentos ao contrato
                val contratoComEquipamentos = if (equipamentos.isNotEmpty()) {
                    LogUtils.debug("ContratosViewModel", "Com ${equipamentos.size} equipamentos")
                    val equipamentosAtualizados = equipamentos.map { 
                        it.copy(contratoId = id) 
                    }
                    contrato.copy(equipamentos = equipamentosAtualizados)
                } else {
                    contrato
                }
                
                val resultado = repository.updateContrato(id, contratoComEquipamentos)
                
                if (resultado.isSuccess()) {
                    val contratoAtualizado = resultado.data!!
                    LogUtils.debug("ContratosViewModel", "Contrato atualizado com ID: ${contratoAtualizado.id}")
                    
                    _operationState.value = UiState.Success(contratoAtualizado)
                    loadContratos()
                } else {
                    LogUtils.error("ContratosViewModel", "Erro ao atualizar contrato: ${resultado.message}")
                    _operationState.value = UiState.Error(resultado.message ?: "Erro desconhecido ao atualizar contrato")
                }
            } catch (e: Exception) {
                LogUtils.error("ContratosViewModel", "Erro ao atualizar contrato", e)
                _operationState.value = UiState.Error("Erro ao atualizar contrato: ${e.message}")
            }
        }
    }
    
    /**
     * Exclui um contrato
     */
    fun excluirContrato(id: Int) {
        _operationState.value = UiState.loading()
        
        viewModelScope.launch {
            LogUtils.debug("ContratosViewModel", "Excluindo contrato: $id")
            
            when (val result = repository.deleteContrato(id)) {
                is Resource.Success -> {
                    LogUtils.info("ContratosViewModel", "Contrato excluído com sucesso: $id")
                    _operationState.value = UiState.Success(
                        Contrato(
                            id = id,
                            clienteId = 0,
                            contratoNum = "",
                            dataHoraEmissao = "",
                            dataVenc = "",
                            contratoValor = 0.0,
                            obraLocal = "",
                            contratoPeriodo = "",
                            entregaLocal = ""
                        )
                    )
                    loadContratos() // Recarrega a lista
                    // Resetar o estado após um breve delay para garantir que o observador já processou o resultado
                    viewModelScope.launch {
                        delay(500)
                        _operationState.value = null
                    }
                }
                
                is Resource.Error -> {
                    if (result.message == "Operação cancelada") {
                        // Não exibir erro para operações canceladas normalmente
                        LogUtils.debug("ContratosViewModel", "Exclusão de contrato cancelada")
                        _operationState.value = null
                    } else {
                        LogUtils.error("ContratosViewModel", "Erro ao excluir contrato: ${result.message}")
                        _operationState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ContratosViewModel", "Estado desconhecido ao excluir contrato")
                    _operationState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
    
    /**
     * Gera o próximo número de contrato para um cliente
     */
    suspend fun getNextContratoNum(clienteId: Int): String {
        return repository.getNextContratoNum(clienteId)
    }
    
    /**
     * Gera dados iniciais para um novo contrato
     */
    fun getDadosIniciais(): Pair<String, String> {
        val dataHoraEmissao = repository.getDataHoraAtual()
        val dataVencimento = repository.getDataVencimento()
        return Pair(dataHoraEmissao, dataVencimento)
    }
    
    /**
     * Define o termo de busca e aplica o filtro
     */
    fun setSearchTerm(term: String) {
        _searchTerm.value = term
        applySearchFilter(term)
    }
    
    /**
     * Aplica o filtro de busca por nome de cliente
     */
    private fun applySearchFilter(term: String) {
        if (allContratos.isEmpty()) {
            _uiState.value = UiState.Empty()
            return
        }
        
        if (term.isEmpty()) {
            _uiState.value = UiState.Success(allContratos)
            return
        }
        
        val filteredList = allContratos.filter { contrato ->
            val clienteNome = contrato.resolverNomeCliente().lowercase()
            clienteNome.contains(term.lowercase())
        }
        
        if (filteredList.isEmpty()) {
            _uiState.value = UiState.Empty()
        } else {
            _uiState.value = UiState.Success(filteredList)
        }
    }
    
    /**
     * Gera a data atual formatada para o padrão ISO 8601
     */
    fun getDataHoraAtual(): String {
        return repository.getDataHoraAtual()
    }
    
    /**
     * Gera a data de vencimento (atual + 30 dias) formatada
     */
    fun getDataVencimento(): String {
        return repository.getDataVencimento()
    }
    
    /**
     * Carrega os equipamentos de um contrato específico
     */
    fun getEquipamentosContrato(contratoId: Int) {
        _equipamentosContratoState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                val resultado = equipamentoContratoRepository.getEquipamentosContrato(contratoId)
                
                if (resultado.isSuccess()) {
                    val equipamentos = resultado.data ?: emptyList()
                    
                    // Log para debug dos dados processados
                    equipamentos.forEach { equip ->
                        LogUtils.debug("ContratosViewModel", 
                            "Processando equipamento:\n" +
                            "ID: ${equip.id}\n" +
                            "ContratoId: ${equip.contratoId}\n" +
                            "EquipamentoId: ${equip.equipamentoId}\n" +
                            "Nome: ${equip.equipamentoNome}\n" +
                            "Quantidade: ${equip.quantidadeEquip}\n" +
                            "Valor Unitário: ${equip.valorUnitario}\n" +
                            "Valor Total: ${equip.valorTotal}\n" +
                            "Valor Frete: ${equip.valorFrete}")
                    }
                    
                    _equipamentosContratoState.value = UiState.Success(equipamentos)
                    LogUtils.info("ContratosViewModel", "Equipamentos do contrato carregados: ${equipamentos.size}")
                } else {
                    _equipamentosContratoState.value = UiState.Error(resultado.message ?: "Erro desconhecido")
                    LogUtils.error("ContratosViewModel", "Erro ao carregar equipamentos do contrato: ${resultado.message}")
                    errorHandler.handleException(Exception(resultado.message), "Erro ao carregar equipamentos")
                }
            } catch (e: Exception) {
                _equipamentosContratoState.value = UiState.Error("Erro inesperado: ${e.message}")
                LogUtils.error("ContratosViewModel", "Exceção ao carregar equipamentos do contrato", e)
                errorHandler.handleException(e, "Erro ao carregar equipamentos")
            }
        }
    }
    
    /**
     * Reseta o estado de operação para null
     * Útil para evitar problemas com diálogos sendo reutilizados
     */
    fun resetOperationState() {
        _operationState.value = null
    }
    
    /**
     * Carrega os detalhes completos de um contrato, incluindo seus equipamentos.
     */
    fun carregarContratoComDetalhes(contratoId: Int) {
        _contratoDetalhado.value = UiState.loading()
        viewModelScope.launch {
            try {
                LogUtils.debug("ContratosViewModel", "Carregando detalhes completos para o contrato ID: $contratoId")
                // Supondo que repository.getContratoById(contratoId) retorna Resource<Contrato?>
                val contratoResource = repository.getContratoById(contratoId) 

                if (contratoResource is Resource.Success && contratoResource.data != null) {
                    val contratoBase = contratoResource.data
                    LogUtils.debug("ContratosViewModel", "Contrato base carregado: ${contratoBase.contratoNum}")

                    // Carregar os equipamentos para este contrato
                    val equipamentosResource = equipamentoContratoRepository.getEquipamentosContrato(contratoId)
                    if (equipamentosResource is Resource.Success) {
                        val listaEquipamentos = equipamentosResource.data ?: emptyList()
                        LogUtils.debug("ContratosViewModel", "Equipamentos para o contrato ${contratoBase.contratoNum} carregados: ${listaEquipamentos.size}")
                        
                        // Log detalhado dos equipamentos carregados
                        listaEquipamentos.forEach { equip ->
                            LogUtils.debug("ContratosViewModel", 
                                "Equipamento carregado para contrato $contratoId:\n" +
                                "ID: ${equip.id}\n" +
                                "Nome: ${equip.equipamentoNome}\n" +
                                "Quantidade: ${equip.quantidadeEquip}\n" +
                                "Valor Unitário: ${equip.valorUnitario}\n" +
                                "Valor Total: ${equip.valorTotal}")
                        }
                        
                        // Calcular o valor total do contrato a partir dos equipamentos
                        val valorTotalCalculado = listaEquipamentos.sumOf { it.valorTotal ?: 0.0 }
                        LogUtils.debug("ContratosViewModel", "Valor total calculado: $valorTotalCalculado")
                        
                        // Associar a lista de equipamentos ao contrato base e atualizar o valor total
                        val contratoCompleto = contratoBase.copy(
                            equipamentos = listaEquipamentos,
                            contratoValor = valorTotalCalculado
                        )
                        
                        _contratoDetalhado.postValue(UiState.Success(contratoCompleto))
                    } else {
                        LogUtils.error("ContratosViewModel", "Erro ao carregar equipamentos para o contrato ID $contratoId: ${equipamentosResource.message}")
                        // Postar o contrato base mesmo se os equipamentos falharem ao carregar
                        // A UI pode então decidir como lidar com a ausência de equipamentos
                        val contratoIncompleto = contratoBase.copy(equipamentos = emptyList()) // Garante que a lista não seja nula
                        _contratoDetalhado.postValue(UiState.Success(contratoIncompleto))
                    }
                } else {
                    LogUtils.error("ContratosViewModel", "Erro ao carregar contrato ID $contratoId: ${contratoResource.message}")
                    _contratoDetalhado.postValue(UiState.Error(contratoResource.message ?: "Contrato ID: $contratoId não encontrado"))
                }
            } catch (e: NullPointerException) {
                // Captura específica para o erro que está ocorrendo no ContratoRepository
                LogUtils.error("ContratosViewModel", "Erro de NullPointerException ao carregar contrato ID $contratoId", e)
                _contratoDetalhado.postValue(UiState.Error("Ocorreu um erro ao processar a resposta da API. Tente novamente."))
            } catch (e: Exception) {
                // Captura genérica para qualquer outro erro
                LogUtils.error("ContratosViewModel", "Exceção ao carregar contrato ID $contratoId", e)
                _contratoDetalhado.postValue(UiState.Error("Erro: ${e.message}"))
            }
        }
    }
}

/**
 * Factory para criar instâncias do ContratosViewModel
 */
class ContratosViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContratosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
                return ContratosViewModel(
                    repository = ContratoRepository(),
                    clienteRepository = ClienteRepository(),
                    equipamentoContratoRepository = EquipamentoContratoRepository() // Usado apenas para leitura de equipamentos
                ) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
