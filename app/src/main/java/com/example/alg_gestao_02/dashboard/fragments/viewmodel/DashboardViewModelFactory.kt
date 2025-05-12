package com.example.alg_gestao_02.dashboard.fragments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory para criar instâncias do DashboardViewModel.
 * Necessário para ViewModels com parâmetros no construtor ou situações
 * específicas de inicialização.
 */
class DashboardViewModelFactory : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel() as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
} 