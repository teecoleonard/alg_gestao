package com.example.alg_gestao_02.ui.fatura.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Fatura
import com.example.alg_gestao_02.data.models.FaturaPdfResponse
import com.example.alg_gestao_02.data.repository.FaturaRepository
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.launch

data class FaturaPdfUiResult(
    val fatura: Fatura,
    val pdf: FaturaPdfResponse,
    val compartilharDireto: Boolean = false,
)

class FaturasViewModel(
    private val repository: FaturaRepository,
) : ViewModel() {
    private val _uiState = MutableLiveData<UiState<List<Fatura>>>()
    val uiState: LiveData<UiState<List<Fatura>>> = _uiState

    private val _pdfState = MutableLiveData<UiState<FaturaPdfUiResult>?>()
    val pdfState: LiveData<UiState<FaturaPdfUiResult>?> = _pdfState

    private var allFaturas: List<Fatura> = emptyList()
    private var searchTerm: String = ""
    private var statusFilter: String? = null

    fun loadFaturas() {
        _uiState.value = UiState.loading()
        viewModelScope.launch {
            when (val result = repository.getFaturas(limit = 100)) {
                is Resource.Success -> {
                    allFaturas = result.data.data
                    aplicarFiltrosLocais()
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                else -> {
                    _uiState.value = UiState.Error("Erro ao carregar faturas")
                }
            }
        }
    }

    fun setSearchTerm(term: String) {
        searchTerm = term
        aplicarFiltrosLocais()
    }

    fun setStatusFilter(status: String?) {
        statusFilter = status
        aplicarFiltrosLocais()
    }

    fun gerarPdfFatura(faturaId: Int, compartilharDireto: Boolean = false) {
        _pdfState.value = UiState.loading()
        viewModelScope.launch {
            when (val result = repository.gerarPdfFatura(faturaId)) {
                is Resource.Success -> {
                    val (fatura, pdf) = result.data
                    _pdfState.value = UiState.Success(
                        FaturaPdfUiResult(
                            fatura = fatura,
                            pdf = pdf,
                            compartilharDireto = compartilharDireto,
                        ),
                    )
                }
                is Resource.Error -> {
                    _pdfState.value = UiState.Error(result.message)
                }
                else -> {
                    _pdfState.value = UiState.Error("Erro ao gerar PDF da fatura")
                }
            }
        }
    }

    fun clearPdfState() {
        _pdfState.value = null
    }

    private fun aplicarFiltrosLocais() {
        if (allFaturas.isEmpty()) {
            _uiState.value = UiState.Empty()
            return
        }

        val term = searchTerm.trim().lowercase()
        val status = statusFilter?.trim()?.uppercase()

        val filtradas = allFaturas.filter { fatura ->
            val statusOk = status.isNullOrBlank() || fatura.status.orEmpty().uppercase() == status
            val buscaOk = if (term.isBlank()) {
                true
            } else {
                fatura.numero.lowercase().contains(term) ||
                    fatura.cliente?.nome.orEmpty().lowercase().contains(term) ||
                    fatura.periodo.orEmpty().lowercase().contains(term) ||
                    fatura.status.orEmpty().lowercase().contains(term)
            }
            statusOk && buscaOk
        }

        if (filtradas.isEmpty()) {
            _uiState.value = UiState.Empty()
        } else {
            _uiState.value = UiState.Success(filtradas.sortedByDescending { it.id })
        }

        LogUtils.debug("FaturasViewModel", "Filtro aplicado: termo='$searchTerm' status='$statusFilter' resultados=${filtradas.size}")
    }
}

class FaturasViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FaturasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FaturasViewModel(FaturaRepository()) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
