package com.example.alg_gestao_02.ui.financial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory para criar instâncias do FinancialViewModel
 */
class FinancialViewModelFactory : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            return FinancialViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 