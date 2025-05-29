package com.example.alg_gestao_02.dashboard.fragments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.data.repository.DashboardRepository

/**
 * Factory para criar instâncias do DashboardViewModel.
 * Usado pelo ViewModelProvider para passar dependências para o ViewModel.
 */
class DashboardViewModelFactory(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
} 