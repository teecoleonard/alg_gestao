package com.example.alg_gestao_02.ui.empresa.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.data.repository.EmpresaRepository

/**
 * Factory para criar instâncias do EmpresaViewModel com suas dependências.
 * Permite injetar o repositório no ViewModel de forma flexível.
 */
class EmpresaViewModelFactory(
    private val repository: EmpresaRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpresaViewModel::class.java)) {
            return EmpresaViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
} 