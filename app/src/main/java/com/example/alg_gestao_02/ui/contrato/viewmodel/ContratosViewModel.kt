package com.example.alg_gestao_02.ui.contrato.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.repository.ClienteRepository
import com.example.alg_gestao_02.data.repository.ContratoRepository
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
    private val clienteRepository: ClienteRepository
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
    val errorViewModel = ErrorViewModel()
    
    // Cliente selecionado para novo contrato
    private val _selectedCliente = MutableLiveData<Cliente?>()
    val selectedCliente: LiveData<Cliente?> = _selectedCliente
    
    // Lista completa de contratos (para filtragem local)
    private var allContratos: List<Contrato> = emptyList()
    
    // Termo de busca atual
    private val _searchTerm = MutableLiveData<String>("")
    val searchTerm: LiveData<String> = _searchTerm
    
    init {
        loadContratos()
        loadClientes()
    }
    
    /**
     * Carrega a lista de contratos
     */
    fun loadContratos() {
        _uiState.value = UiState.Loading()
        
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
        _clientesState.value = UiState.Loading()
        
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
    fun criarContrato(contrato: Contrato) {
        _operationState.value = UiState.Loading()
        
        viewModelScope.launch {
            LogUtils.debug("ContratosViewModel", "Criando contrato para cliente: ${contrato.clienteId}")
            
            when (val result = repository.createContrato(contrato)) {
                is Resource.Success -> {
                    LogUtils.info("ContratosViewModel", "Contrato criado com sucesso: ${result.data.contratoNum}")
                    _operationState.value = UiState.Success(result.data)
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
                        LogUtils.debug("ContratosViewModel", "Criação de contrato cancelada")
                        _operationState.value = null
                    } else {
                        LogUtils.error("ContratosViewModel", "Erro ao criar contrato: ${result.message}")
                        _operationState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ContratosViewModel", "Estado desconhecido ao criar contrato")
                    _operationState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
    
    /**
     * Atualiza um contrato existente
     */
    fun atualizarContrato(id: Int, contrato: Contrato) {
        _operationState.value = UiState.Loading()
        
        viewModelScope.launch {
            LogUtils.debug("ContratosViewModel", "Atualizando contrato: $id")
            
            when (val result = repository.updateContrato(id, contrato)) {
                is Resource.Success -> {
                    LogUtils.info("ContratosViewModel", "Contrato atualizado com sucesso: ${result.data.contratoNum}")
                    _operationState.value = UiState.Success(result.data)
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
                        LogUtils.debug("ContratosViewModel", "Atualização de contrato cancelada")
                        _operationState.value = null
                    } else {
                        LogUtils.error("ContratosViewModel", "Erro ao atualizar contrato: ${result.message}")
                        _operationState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ContratosViewModel", "Estado desconhecido ao atualizar contrato")
                    _operationState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
    
    /**
     * Exclui um contrato
     */
    fun excluirContrato(id: Int) {
        _operationState.value = UiState.Loading()
        
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
                clienteRepository = ClienteRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 