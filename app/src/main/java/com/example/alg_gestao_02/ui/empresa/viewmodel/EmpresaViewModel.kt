package com.example.alg_gestao_02.ui.empresa.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Empresa
import com.example.alg_gestao_02.data.repository.EmpresaRepository
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch

/**
 * ViewModel para o módulo de empresas.
 * Gerencia os dados e a lógica de negócios relacionados a empresas,
 * isolando a UI da camada de dados.
 */
class EmpresaViewModel(private val repository: EmpresaRepository) : ViewModel() {
    
    // Adiciona ErrorViewModel para tratar erros de forma centralizada
    val errorHandler = ErrorViewModel()
    
    // LiveData privado para armazenar o estado da UI
    private val _empresasState = MutableLiveData<UiState<List<Empresa>>>()
    
    // LiveData público exposto para a UI
    val empresasState: LiveData<UiState<List<Empresa>>> = _empresasState
    
    // Filtro atual aplicado
    private var filtroAtual = FiltroEmpresa.TODOS
    
    // Texto de busca atual
    private var textoBusca = ""
    
    init {
        LogUtils.debug("EmpresaViewModel", "ViewModel inicializado")
        // Carrega as empresas ao inicializar o ViewModel
        loadEmpresas()
    }
    
    /**
     * Carrega a lista de empresas do repositório.
     */
    fun loadEmpresas() {
        LogUtils.debug("EmpresaViewModel", "Carregando empresas com filtro: $filtroAtual")
        
        _empresasState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                var empresas: List<Empresa> = emptyList()
                
                // Se tiver texto de busca, priorizar a busca
                if (textoBusca.isNotEmpty()) {
                    empresas = repository.searchEmpresas(textoBusca)
                } else {
                    // Aplicar filtro por status
                    empresas = when (filtroAtual) {
                        FiltroEmpresa.TODOS -> repository.getAllEmpresas()
                        FiltroEmpresa.ATIVOS -> repository.getEmpresasByStatus("ativo")
                        FiltroEmpresa.INATIVOS -> repository.getEmpresasByStatus("inativo")
                    }
                }
                
                if (empresas.isEmpty()) {
                    _empresasState.value = UiState.empty()
                } else {
                    _empresasState.value = UiState.success(empresas)
                }
                
            } catch (e: Exception) {
                // Primeiro, usar o sistema centralizado de tratamento de erros
                errorHandler.handleException(e, "EmpresaViewModel", true)
                
                // Segundo, atualizar o estado da UI para refletir o erro
                _empresasState.value = UiState.error("Falha ao carregar empresas: ${e.message}")
            }
        }
    }
    
    /**
     * Define o filtro de status e recarrega os dados
     */
    fun setFiltroTipo(filtro: FiltroEmpresa) {
        filtroAtual = filtro
        loadEmpresas()
    }
    
    /**
     * Define o texto de busca e recarrega os dados
     */
    fun setTextoBusca(query: String) {
        textoBusca = query
        loadEmpresas()
    }
    
    /**
     * Adiciona uma nova empresa (método temporário para teste)
     */
    fun adicionarEmpresa() {
        LogUtils.debug("EmpresaViewModel", "Solicitação para adicionar empresa")
        // No futuro, abrir formulário ou diálogo para criar empresa
    }
    
    /**
     * Editar uma empresa existente (método temporário para teste)
     */
    fun editarEmpresa(empresa: Empresa) {
        LogUtils.debug("EmpresaViewModel", "Solicitação para editar empresa: ${empresa.id}")
        // No futuro, abrir formulário ou diálogo para editar
    }
    
    /**
     * Excluir uma empresa (método temporário para teste)
     */
    fun excluirEmpresa(empresa: Empresa) {
        LogUtils.debug("EmpresaViewModel", "Solicitação para excluir empresa: ${empresa.id}")
        // No futuro, exibir confirmação e chamar repositório
        
        // Exemplo de chamada que seria feita:
        // viewModelScope.launch {
        //     try {
        //         val success = repository.excluirEmpresa(empresa.id)
        //         if (success) {
        //             loadEmpresas()
        //         }
        //     } catch (e: Exception) {
        //         errorHandler.handleException(e, "EmpresaViewModel", true)
        //     }
        // }
    }
    
    /**
     * Tipos de filtro disponíveis para empresas
     */
    enum class FiltroEmpresa {
        TODOS,
        ATIVOS,
        INATIVOS
    }
} 