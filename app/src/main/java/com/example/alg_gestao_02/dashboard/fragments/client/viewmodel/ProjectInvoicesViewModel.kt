package com.example.alg_gestao_02.dashboard.fragments.client.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectInvoiceItem
import com.example.alg_gestao_02.ui.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

class ProjectInvoicesViewModel : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<ProjectInvoiceItem>>>()
    val uiState: LiveData<UiState<List<ProjectInvoiceItem>>> = _uiState

    private var currentMonth = 0
    private var currentYear = 0
    private var projectId: String? = null

    fun setProjectId(id: String) {
        projectId = id
        loadInvoices(currentMonth, currentYear)
    }

    fun loadInvoices(month: Int, year: Int) {
        currentMonth = month
        currentYear = year

        viewModelScope.launch {
            _uiState.value = UiState.Loading()
            
            try {
                // Simular uma chamada de rede
                delay(1000)
                
                // Verificar se temos um ID de projeto válido
                if (projectId == null) {
                    _uiState.value = UiState.Error("ID do projeto não especificado")
                    return@launch
                }
                
                val invoices = getMockInvoices(projectId!!, month, year)
                
                if (invoices.isEmpty()) {
                    _uiState.value = UiState.Empty()
                } else {
                    _uiState.value = UiState.Success(invoices)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro ao carregar faturas: ${e.message}")
            }
        }
    }

    fun navigateToNextMonth() {
        if (currentMonth == 11) {
            loadInvoices(0, currentYear + 1)
        } else {
            loadInvoices(currentMonth + 1, currentYear)
        }
    }

    fun navigateToPreviousMonth() {
        if (currentMonth == 0) {
            loadInvoices(11, currentYear - 1)
        } else {
            loadInvoices(currentMonth - 1, currentYear)
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading()
            
            try {
                // Simular uma chamada de rede para excluir a fatura
                delay(500)
                
                // Obter a lista atual de faturas
                val currentInvoices = (_uiState.value as? UiState.Success)?.data ?: emptyList()
                
                // Remover a fatura com o ID especificado
                val updatedInvoices = currentInvoices.filter { it.id != invoiceId }
                
                if (updatedInvoices.isEmpty()) {
                    _uiState.value = UiState.Empty()
                } else {
                    _uiState.value = UiState.Success(updatedInvoices)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro ao excluir fatura: ${e.message}")
            }
        }
    }

    // Método para gerar dados fictícios para teste
    private fun getMockInvoices(projectId: String, month: Int, year: Int): List<ProjectInvoiceItem> {
        val statusOptions = listOf("Pago", "Pendente", "Atrasado")
        val result = mutableListOf<ProjectInvoiceItem>()
        
        // Gerar dados diferentes com base no mês
        val count = when (month) {
            0, 2, 4, 6, 8, 10 -> 3  // Meses pares têm 3 faturas
            1, 5, 9 -> 2            // Alguns meses ímpares têm 2 faturas
            else -> 0               // Outros meses não têm faturas
        }
        
        for (i in 1..count) {
            val status = statusOptions[(i-1) % statusOptions.size]
            result.add(
                ProjectInvoiceItem(
                    id = "${projectId}_${month}_${i}",
                    numero = "INV-${year}${month+1}${projectId}${i}",
                    projectId = projectId,
                    valor = 1000.0 * i + (month * 100),
                    dataEmissao = "${i}/${month+1}/${year}",
                    dataVencimento = "${i+15}/${month+1}/${year}",
                    status = status
                )
            )
        }
        
        return result
    }
} 