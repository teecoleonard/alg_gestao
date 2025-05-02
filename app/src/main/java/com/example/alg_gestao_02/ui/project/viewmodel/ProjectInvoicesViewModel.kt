package com.example.alg_gestao_02.ui.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectInvoiceItem
import com.example.alg_gestao_02.data.repository.InvoiceRepository
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel para gerenciar faturas de um projeto específico.
 * Segue o padrão MVVM para separar a lógica de negócios da UI.
 */
class ProjectInvoicesViewModel(private val repository: InvoiceRepository) : ViewModel() {
    
    // LiveData privado para armazenar o estado da UI
    private val _uiState = MutableLiveData<UiState<List<ProjectInvoiceItem>>>()
    
    // LiveData público exposto para a UI
    val uiState: LiveData<UiState<List<ProjectInvoiceItem>>> = _uiState
    
    // Calendar para controle do filtro de mês
    private val calendar = Calendar.getInstance()
    
    // ID do projeto atual
    private var currentProjectId: String? = null
    
    /**
     * Carrega faturas para um projeto específico no mês atual
     */
    fun loadInvoicesForProject(projectId: String) {
        currentProjectId = projectId
        _uiState.value = UiState.Companion.loading()
        
        viewModelScope.launch {
            try {
                val invoices = repository.getInvoicesByProject(projectId)
                
                // Filtra pelas faturas do mês atual, se necessário
                val filteredInvoices = filterInvoicesByMonth(invoices)
                
                if (filteredInvoices.isEmpty()) {
                    _uiState.value = UiState.Companion.empty()
                } else {
                    _uiState.value = UiState.Companion.success(filteredInvoices)
                }
            } catch (e: Exception) {
                LogUtils.error("ProjectInvoicesViewModel", "Erro ao carregar faturas: ${e.message}")
                _uiState.value = UiState.Companion.error("Falha ao carregar faturas: ${e.message}")
            }
        }
    }
    
    /**
     * Carrega faturas para um projeto específico em um mês específico
     */
    fun loadInvoicesByProjectAndMonth(projectId: String, month: Calendar) {
        currentProjectId = projectId
        calendar.timeInMillis = month.timeInMillis
        refreshInvoices()
    }
    
    /**
     * Navega para o mês anterior e recarrega as faturas
     */
    fun loadPreviousMonth(projectId: String) {
        calendar.add(Calendar.MONTH, -1)
        currentProjectId = projectId
        refreshInvoices()
    }
    
    /**
     * Navega para o próximo mês e recarrega as faturas
     */
    fun loadNextMonth(projectId: String) {
        calendar.add(Calendar.MONTH, 1)
        currentProjectId = projectId
        refreshInvoices()
    }
    
    /**
     * Obtém o mês atual formatado em português
     */
    fun getCurrentMonthFormatted(): String {
        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("pt", "BR"))
        return monthFormat.format(calendar.time).replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() 
        }
    }
    
    /**
     * Filtra faturas por status
     */
    fun filterByStatus(status: String?) {
        if (currentProjectId == null) {
            LogUtils.error("ProjectInvoicesViewModel", "Tentativa de filtrar sem um projeto definido")
            _uiState.value = UiState.Companion.error("Nenhum projeto selecionado")
            return
        }
        
        _uiState.value = UiState.Companion.loading()
        
        viewModelScope.launch {
            try {
                val invoices = if (status == null) {
                    repository.getInvoicesByProject(currentProjectId!!)
                } else {
                    repository.getInvoicesByProjectAndStatus(currentProjectId!!, status)
                }
                
                // Filtra pelas faturas do mês atual, se necessário
                val filteredInvoices = filterInvoicesByMonth(invoices)
                
                if (filteredInvoices.isEmpty()) {
                    _uiState.value = UiState.Companion.empty()
                } else {
                    _uiState.value = UiState.Companion.success(filteredInvoices)
                }
            } catch (e: Exception) {
                LogUtils.error("ProjectInvoicesViewModel", "Erro ao filtrar faturas: ${e.message}")
                _uiState.value = UiState.Companion.error("Falha ao filtrar faturas: ${e.message}")
            }
        }
    }
    
    /**
     * Atualiza dados (por exemplo, ao fazer swipe refresh)
     */
    fun refreshInvoices() {
        if (currentProjectId == null) {
            return
        }
        
        _uiState.value = UiState.Companion.loading()
        
        viewModelScope.launch {
            try {
                val invoices = repository.getInvoicesByProject(currentProjectId!!)
                
                // Filtra pelas faturas do mês atual, se necessário
                val filteredInvoices = filterInvoicesByMonth(invoices)
                
                if (filteredInvoices.isEmpty()) {
                    _uiState.value = UiState.Companion.empty()
                } else {
                    _uiState.value = UiState.Companion.success(filteredInvoices)
                }
            } catch (e: Exception) {
                LogUtils.error("ProjectInvoicesViewModel", "Erro ao atualizar faturas: ${e.message}")
                _uiState.value = UiState.Companion.error("Falha ao atualizar faturas: ${e.message}")
            }
        }
    }
    
    /**
     * Filtra as faturas pelo mês atual do calendário
     */
    private fun filterInvoicesByMonth(invoices: List<ProjectInvoiceItem>): List<ProjectInvoiceItem> {
        // Exemplo de lógica para filtrar por mês, ajuste de acordo com a estrutura da data
        // No mock atual, estamos simulando que todas as faturas são do mês atual
        
        // Em uma implementação real, você iria converter a string de data em um objeto Date
        // e comparar com o mês/ano atuais
        return invoices
    }
} 