package com.example.alg_gestao_02.ui.material.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Material
import com.example.alg_gestao_02.data.repository.MaterialRepository
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch

class MateriaisViewModel(
    private val repository: MaterialRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<Material>>>()
    val uiState: LiveData<UiState<List<Material>>> = _uiState

    val errorHandler = ErrorViewModel()

    private var textoBusca: String = ""
    private var filtro: FiltroMaterial = FiltroMaterial.TODOS

    init {
        loadMateriais()
    }

    fun loadMateriais() {
        _uiState.value = UiState.loading()

        viewModelScope.launch {
            val result = repository.getMateriaisComDisponibilidade()
            when {
                result.isSuccess() -> {
                    val data = result.data ?: emptyList()
                    val filtradosPorTipo = when (filtro) {
                        FiltroMaterial.TODOS -> data
                        FiltroMaterial.DISPONIVEIS -> data.filter { it.isDisponivel() }
                    }
                    val filtrados = filtrarPorTexto(filtradosPorTipo)
                    _uiState.value = if (filtrados.isEmpty()) UiState.Empty() else UiState.Success(filtrados)
                }
                else -> {
                    val message = result.message ?: "Erro ao carregar materiais"
                    _uiState.value = UiState.Error(message)
                    errorHandler.handleException(Exception(message), "MateriaisViewModel")
                }
            }
        }
    }

    fun setTextoBusca(valor: String) {
        textoBusca = valor
        loadMateriais()
    }

    fun setFiltro(filtroMaterial: FiltroMaterial) {
        filtro = filtroMaterial
        loadMateriais()
    }

    fun criarMaterial(material: Material) {
        viewModelScope.launch {
            val result = repository.createMaterial(material)
            if (result.isSuccess()) {
                loadMateriais()
            } else {
                val message = result.message ?: "Erro ao criar material"
                LogUtils.error("MateriaisViewModel", message)
                errorHandler.handleException(Exception(message), "MateriaisViewModel")
            }
        }
    }

    fun atualizarMaterial(id: Int, material: Material) {
        viewModelScope.launch {
            val result = repository.updateMaterial(id, material)
            if (result.isSuccess()) {
                loadMateriais()
            } else {
                val message = result.message ?: "Erro ao atualizar material"
                LogUtils.error("MateriaisViewModel", message)
                errorHandler.handleException(Exception(message), "MateriaisViewModel")
            }
        }
    }

    fun excluirMaterial(id: Int) {
        viewModelScope.launch {
            val result = repository.deleteMaterial(id)
            if (result.isSuccess()) {
                loadMateriais()
            } else {
                val message = result.message ?: "Erro ao excluir material"
                LogUtils.error("MateriaisViewModel", message)
                errorHandler.handleException(Exception(message), "MateriaisViewModel")
            }
        }
    }

    fun carregarMateriaisDisponiveis(onResult: (List<Material>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getMateriaisComDisponibilidade()
            val materiaisAtivos = (result.data ?: emptyList()).filter { it.ativo }
            onResult(materiaisAtivos)
        }
    }

    private fun filtrarPorTexto(lista: List<Material>): List<Material> {
        if (textoBusca.isBlank()) return lista
        val termo = textoBusca.trim()
        return lista.filter {
            it.nome.contains(termo, ignoreCase = true) ||
                (it.codigo?.contains(termo, ignoreCase = true) == true)
        }
    }

    enum class FiltroMaterial {
        TODOS,
        DISPONIVEIS
    }
}

class MateriaisViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MateriaisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MateriaisViewModel(MaterialRepository()) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
