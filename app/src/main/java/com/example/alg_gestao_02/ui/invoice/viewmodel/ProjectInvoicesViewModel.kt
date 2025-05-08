package com.example.alg_gestao_02.ui.invoice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.FaturaProjeto
import com.example.alg_gestao_02.ui.invoice.repository.InvoiceRepository
import com.example.alg_gestao_02.ui.state.UiState
import kotlinx.coroutines.launch
import java.util.Calendar

class ProjectInvoicesViewModel(private val repository: InvoiceRepository) : ViewModel() {
    private val _uiState = MutableLiveData<UiState<List<FaturaProjeto>>>()
    val uiState: LiveData<UiState<List<FaturaProjeto>>> = _uiState
    
    private var currentProjectId: String? = null
    private val currentCalendar = Calendar.getInstance()
    
    /**
     * Carrega as faturas de um projeto específico
     */
    fun loadInvoices(projectId: String) {
        currentProjectId = projectId
        _uiState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                val invoices = repository.getInvoicesByProject(projectId)
                
                if (invoices.isEmpty()) {
                    _uiState.value = UiState.Empty()
                } else {
                    _uiState.value = UiState.Success(invoices)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao carregar faturas")
            }
        }
    }
    
    /**
     * Carrega as faturas do mês anterior
     */
    fun loadPreviousMonth() {
        currentCalendar.add(Calendar.MONTH, -1)
        currentProjectId?.let { loadInvoicesForCurrentMonth(it) }
    }
    
    /**
     * Carrega as faturas do próximo mês
     */
    fun loadNextMonth() {
        currentCalendar.add(Calendar.MONTH, 1)
        currentProjectId?.let { loadInvoicesForCurrentMonth(it) }
    }
    
    /**
     * Carrega as faturas para o mês atual do calendário
     */
    private fun loadInvoicesForCurrentMonth(projectId: String) {
        _uiState.value = UiState.loading()
        
        viewModelScope.launch {
            try {
                // Filtra manualmente pois não temos API real
                val allInvoices = repository.getInvoicesByProject(projectId)
                val month = currentCalendar.get(Calendar.MONTH) + 1 // Calendar.MONTH é 0-based
                val year = currentCalendar.get(Calendar.YEAR)
                
                val filteredInvoices = allInvoices.filter { 
                    it.month == month && it.year == year 
                }
                
                if (filteredInvoices.isEmpty()) {
                    _uiState.value = UiState.Empty()
                } else {
                    _uiState.value = UiState.Success(filteredInvoices)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao carregar faturas")
            }
        }
    }
    
    /**
     * Obtém o mês e ano atuais para exibição
     */
    fun getCurrentMonthYear(): Pair<Int, Int> {
        val month = currentCalendar.get(Calendar.MONTH) + 1
        val year = currentCalendar.get(Calendar.YEAR)
        return Pair(month, year)
    }
    
    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteInvoice(invoiceId)
                // Recarregar as faturas após a exclusão
                loadInvoices(currentProjectId ?: "")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Falha ao excluir fatura: ${e.message}")
            }
        }
    }
} 