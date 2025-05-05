package com.example.alg_gestao_02.ui.cliente.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.repository.ClienteRepository
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * ViewModel para gerenciar a tela de listagem e CRUD de clientes
 */
class ClientesViewModel(
    private val repository: ClienteRepository
) : ViewModel() {

    // Estado da UI
    private val _uiState = MutableLiveData<UiState<List<Cliente>>>()
    val uiState: LiveData<UiState<List<Cliente>>> = _uiState
    
    // Estado da operação de criação/edição/exclusão
    private val _operationState = MutableLiveData<UiState<Cliente>?>()
    val operationState: LiveData<UiState<Cliente>?> = _operationState
    
    // ViewModel para tratamento de erros
    val errorViewModel = ErrorViewModel()
    
    // Lista completa de clientes (para filtragem local)
    private var allClientes: List<Cliente> = emptyList()
    
    // Termo de busca atual
    private val _searchTerm = MutableLiveData<String>("")
    val searchTerm: LiveData<String> = _searchTerm
    
    init {
        loadClientes()
    }
    
    /**
     * Carrega a lista de clientes
     */
    fun loadClientes() {
        _uiState.value = UiState.Loading()
        
        viewModelScope.launch {
            LogUtils.debug("ClientesViewModel", "Carregando clientes")
            
            when (val result = repository.getClientes()) {
                is com.example.alg_gestao_02.utils.Resource.Success -> {
                    val clientes = result.data
                    if (clientes.isNotEmpty()) {
                        LogUtils.info("ClientesViewModel", "Clientes carregados com sucesso: ${clientes.size}")
                        allClientes = clientes
                        applySearchFilter(_searchTerm.value ?: "")
                    } else {
                        LogUtils.info("ClientesViewModel", "Nenhum cliente encontrado")
                        allClientes = emptyList()
                        _uiState.value = UiState.Empty()
                    }
                }
                
                is com.example.alg_gestao_02.utils.Resource.Error -> {
                    if (result.message == "Operação cancelada") {
                        // Não exibir erro para operações canceladas normalmente
                        LogUtils.debug("ClientesViewModel", "Carregamento de clientes cancelado")
                    } else {
                        LogUtils.error("ClientesViewModel", "Erro ao carregar clientes: ${result.message}")
                        _uiState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ClientesViewModel", "Estado desconhecido")
                    _uiState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
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
        if (allClientes.isEmpty()) {
            _uiState.value = UiState.Empty()
            return
        }
        
        if (term.isEmpty()) {
            _uiState.value = UiState.Success(allClientes)
            return
        }
        
        val filteredList = allClientes.filter { cliente ->
            val nome = cliente.contratante.lowercase()
            nome.contains(term.lowercase())
        }
        
        if (filteredList.isEmpty()) {
            _uiState.value = UiState.Empty()
        } else {
            _uiState.value = UiState.Success(filteredList)
        }
    }
    
    /**
     * Cria um novo cliente
     */
    fun criarCliente(cliente: Cliente) {
        _operationState.value = UiState.Loading()
        
        viewModelScope.launch {
            LogUtils.debug("ClientesViewModel", "Criando cliente: ${cliente.contratante}")
            
            when (val result = repository.createCliente(cliente)) {
                is com.example.alg_gestao_02.utils.Resource.Success -> {
                    LogUtils.info("ClientesViewModel", "Cliente criado com sucesso: ${cliente.contratante}")
                    _operationState.value = UiState.Success(result.data)
                    loadClientes() // Recarrega a lista
                    // Resetar o estado após um breve delay para garantir que o observador já processou o resultado
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(500)
                        _operationState.value = null
                    }
                }
                
                is com.example.alg_gestao_02.utils.Resource.Error -> {
                    if (result.message == "Operação cancelada") {
                        // Não exibir erro para operações canceladas normalmente
                        LogUtils.debug("ClientesViewModel", "Criação de cliente cancelada")
                        _operationState.value = null
                    } else {
                        LogUtils.error("ClientesViewModel", "Erro ao criar cliente: ${result.message}")
                        _operationState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ClientesViewModel", "Estado desconhecido")
                    _operationState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
    
    /**
     * Atualiza um cliente existente
     */
    fun atualizarCliente(id: Int, cliente: Cliente) {
        _operationState.value = UiState.Loading()
        
        viewModelScope.launch {
            LogUtils.debug("ClientesViewModel", "Atualizando cliente: ${cliente.contratante}")
            
            when (val result = repository.updateCliente(id, cliente)) {
                is com.example.alg_gestao_02.utils.Resource.Success -> {
                    LogUtils.info("ClientesViewModel", "Cliente atualizado com sucesso: ${cliente.contratante}")
                    _operationState.value = UiState.Success(result.data)
                    loadClientes() // Recarrega a lista
                    // Resetar o estado após um breve delay para garantir que o observador já processou o resultado
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(500)
                        _operationState.value = null
                    }
                }
                
                is com.example.alg_gestao_02.utils.Resource.Error -> {
                    if (result.message == "Operação cancelada") {
                        // Não exibir erro para operações canceladas normalmente
                        LogUtils.debug("ClientesViewModel", "Atualização de cliente cancelada")
                        _operationState.value = null
                    } else {
                        LogUtils.error("ClientesViewModel", "Erro ao atualizar cliente: ${result.message}")
                        _operationState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ClientesViewModel", "Estado desconhecido")
                    _operationState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
    
    /**
     * Exclui um cliente
     */
    fun excluirCliente(id: Int) {
        _operationState.value = UiState.Loading()
        
        viewModelScope.launch {
            LogUtils.debug("ClientesViewModel", "Excluindo cliente: $id")
            
            when (val result = repository.deleteCliente(id)) {
                is com.example.alg_gestao_02.utils.Resource.Success -> {
                    LogUtils.info("ClientesViewModel", "Cliente excluído com sucesso: $id")
                    _operationState.value = UiState.Success(Cliente(id = id, contratante = "", cpfCnpj = "", rgIe = "", endereco = "", bairro = "", cidade = "", estado = ""))
                    loadClientes() // Recarrega a lista
                    // Resetar o estado após um breve delay para garantir que o observador já processou o resultado
                    viewModelScope.launch {
                        delay(500)
                        _operationState.value = null
                    }
                }
                
                is com.example.alg_gestao_02.utils.Resource.Error -> {
                    if (result.message == "Operação cancelada") {
                        // Não exibir erro para operações canceladas normalmente
                        LogUtils.debug("ClientesViewModel", "Exclusão de cliente cancelada")
                        _operationState.value = null
                    } else {
                        LogUtils.error("ClientesViewModel", "Erro ao excluir cliente: ${result.message}")
                        _operationState.value = UiState.Error(result.message)
                    }
                }
                
                else -> {
                    LogUtils.error("ClientesViewModel", "Estado desconhecido")
                    _operationState.value = UiState.Error("Estado desconhecido")
                }
            }
        }
    }
}

/**
 * Factory para criar instâncias do ClientesViewModel
 */
class ClientesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientesViewModel(ClienteRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 