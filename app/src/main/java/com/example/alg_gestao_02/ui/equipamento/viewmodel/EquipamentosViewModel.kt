package com.example.alg_gestao_02.ui.equipamento.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.data.repository.EquipamentoRepository
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar a tela de listagem de equipamentos
 */
class EquipamentosViewModel(
    private val repository: EquipamentoRepository
) : ViewModel() {

    // Estado da UI
    private val _uiState = MutableLiveData<UiState<List<Equipamento>>>()
    val uiState: LiveData<UiState<List<Equipamento>>> = _uiState
    
    // Manipulador de erros para BaseFragment
    val errorHandler = ErrorViewModel()
    
    // Equipamento selecionado para detalhes ou edição
    private val _selectedEquipamento = MutableLiveData<Equipamento?>()
    val selectedEquipamento: LiveData<Equipamento?> = _selectedEquipamento
    
    // Texto de busca
    private var textoBusca: String = ""
    
    // Tipo de filtro
    private var filtroTipo: FiltroEquipamento = FiltroEquipamento.TODOS
    
    init {
        loadEquipamentos()
    }
    
    /**
     * Carrega a lista de equipamentos
     */
    fun loadEquipamentos() {
        _uiState.value = UiState.loading()
        
        viewModelScope.launch {
            val result = when (filtroTipo) {
                FiltroEquipamento.TODOS -> repository.getEquipamentos()
                FiltroEquipamento.DISPONIVEIS -> repository.getEquipamentosDisponiveis()
            }
            
            when {
                result.isSuccess() -> {
                    val equipamentos = result.data!!
                    val filteredEquipamentos = filtrarEquipamentos(equipamentos)
                    
                    if (filteredEquipamentos.isEmpty()) {
                        _uiState.value = UiState.Empty()
                    } else {
                        _uiState.value = UiState.Success(filteredEquipamentos)
                    }
                    LogUtils.debug("EquipamentosViewModel", "Equipamentos carregados: ${equipamentos.size}")
                }
                result.isError() -> {
                    _uiState.value = UiState.Error(result.message ?: "Erro desconhecido")
                    errorHandler.handleException(Exception(result.message), "EquipamentosViewModel")
                    LogUtils.error("EquipamentosViewModel", "Erro ao carregar equipamentos: ${result.message}")
                }
            }
        }
    }
    
    /**
     * Filtra equipamentos de acordo com o texto de busca
     */
    private fun filtrarEquipamentos(equipamentos: List<Equipamento>): List<Equipamento> {
        if (textoBusca.isBlank()) return equipamentos
        
        return equipamentos.filter { equipamento ->
            equipamento.nomeEquip.contains(textoBusca, ignoreCase = true) ||
            equipamento.codigoEquip.contains(textoBusca, ignoreCase = true)
        }
    }
    
    /**
     * Define o texto de busca e filtra os equipamentos
     */
    fun setTextoBusca(texto: String) {
        this.textoBusca = texto
        loadEquipamentos() // Recarrega com o novo filtro
    }
    
    /**
     * Define o tipo de filtro
     */
    fun setFiltroTipo(filtro: FiltroEquipamento) {
        this.filtroTipo = filtro
        loadEquipamentos() // Recarrega com o novo filtro
    }
    
    /**
     * Seleciona um equipamento para visualização/edição
     */
    fun selecionarEquipamento(equipamento: Equipamento) {
        _selectedEquipamento.value = equipamento
    }
    
    /**
     * Limpa o equipamento selecionado
     */
    fun limparSelecao() {
        _selectedEquipamento.value = null
    }
    
    /**
     * Cria um novo equipamento
     */
    fun criarEquipamento(equipamento: Equipamento) {
        viewModelScope.launch {
            val result = repository.createEquipamento(equipamento)
            
            if (result.isSuccess()) {
                LogUtils.info("EquipamentosViewModel", "Equipamento criado com sucesso")
                loadEquipamentos() // Recarrega a lista
            } else {
                errorHandler.handleException(Exception(result.message), "EquipamentosViewModel")
                LogUtils.error("EquipamentosViewModel", "Erro ao criar equipamento: ${result.message}")
            }
        }
    }
    
    /**
     * Atualiza um equipamento existente
     */
    fun atualizarEquipamento(id: Int, equipamento: Equipamento) {
        viewModelScope.launch {
            val result = repository.updateEquipamento(id, equipamento)
            
            if (result.isSuccess()) {
                LogUtils.info("EquipamentosViewModel", "Equipamento atualizado com sucesso")
                loadEquipamentos() // Recarrega a lista
            } else {
                errorHandler.handleException(Exception(result.message), "EquipamentosViewModel")
                LogUtils.error("EquipamentosViewModel", "Erro ao atualizar equipamento: ${result.message}")
            }
        }
    }
    
    /**
     * Exclui um equipamento
     */
    fun excluirEquipamento(id: Int) {
        viewModelScope.launch {
            val result = repository.deleteEquipamento(id)
            
            if (result.isSuccess()) {
                LogUtils.info("EquipamentosViewModel", "Equipamento excluído com sucesso")
                loadEquipamentos() // Recarrega a lista
            } else {
                errorHandler.handleException(Exception(result.message), "EquipamentosViewModel")
                LogUtils.error("EquipamentosViewModel", "Erro ao excluir equipamento: ${result.message}")
            }
        }
    }
    
    /**
     * Enum para representar os possíveis filtros de equipamentos
     */
    enum class FiltroEquipamento {
        TODOS,
        DISPONIVEIS
    }
}

/**
 * Factory para criar instâncias do EquipamentosViewModel com o repositório correto
 */
class EquipamentosViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EquipamentosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EquipamentosViewModel(EquipamentoRepository()) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
} 