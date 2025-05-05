package com.example.alg_gestao_02.ui.invoice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.ui.invoice.repository.InvoiceRepository

/**
 * Factory para criação do ProjectInvoicesViewModel com injeção do repositório
 */
class ProjectInvoicesViewModelFactory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectInvoicesViewModel::class.java)) {
            return ProjectInvoicesViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
} 