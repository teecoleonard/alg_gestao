package com.example.alg_gestao_02.ui.invoice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectInvoiceItem
import com.example.alg_gestao_02.ui.invoice.repository.InvoiceRepository
import com.example.alg_gestao_02.ui.state.UiState
import kotlinx.coroutines.launch
import java.time.YearMonth

class ProjectInvoicesViewModel(private val repository: InvoiceRepository) : ViewModel() {
    private val _uiState = MutableLiveData<UiState<List<ProjectInvoiceItem>>>()
    val uiState: LiveData<UiState<List<ProjectInvoiceItem>>> = _uiState
    
    private var currentMonth = YearMonth.now()
    private var currentProjectId: String = ""
    
    fun loadInvoices(projectId: String) {
        currentProjectId = projectId
        loadInvoicesByMonth()
    }
    
    fun loadPreviousMonth() {
        currentMonth = currentMonth.minusMonths(1)
        loadInvoicesByMonth()
    }
    
    fun loadNextMonth() {
        currentMonth = currentMonth.plusMonths(1)
        loadInvoicesByMonth()
    }
    
    fun getCurrentMonth(): YearMonth {
        return currentMonth
    }
    
    private fun loadInvoicesByMonth() {
        _uiState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                val result = repository.getInvoicesByProjectAndMonth(
                    projectId = currentProjectId,
                    month = currentMonth.monthValue,
                    year = currentMonth.year
                )
                
                if (result.isEmpty()) {
                    _uiState.value = UiState.empty()
                } else {
                    _uiState.value = UiState.success(result)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.error(e.message ?: "Erro ao carregar faturas")
            }
        }
    }
    
    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteInvoice(invoiceId)
                // Recarregar as faturas após a exclusão
                loadInvoicesByMonth()
            } catch (e: Exception) {
                _uiState.value = UiState.error("Falha ao excluir fatura: ${e.message}")
            }
        }
    }
} 