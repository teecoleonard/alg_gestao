package com.example.alg_gestao_02.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.data.repository.ClienteRepository
import com.example.alg_gestao_02.data.repository.ContratoRepository
import com.example.alg_gestao_02.data.repository.DevolucaoRepository
import com.example.alg_gestao_02.ui.client.viewmodel.ClientDetailsViewModel

/**
 * Factory para criação de ViewModels que precisa de parâmetros
 */
class ViewModelFactory(
    private val clienteRepository: ClienteRepository? = null,
    private val contratoRepository: ContratoRepository? = null,
    private val devolucaoRepository: DevolucaoRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ClientDetailsViewModel::class.java) -> {
                if (clienteRepository != null && contratoRepository != null && devolucaoRepository != null) {
                    ClientDetailsViewModel(clienteRepository, contratoRepository, devolucaoRepository) as T
                } else {
                    throw IllegalArgumentException("Repositórios necessários não fornecidos para ClientDetailsViewModel")
                }
            }
            // Adicione outros ViewModels aqui conforme necessário
            else -> throw IllegalArgumentException("ViewModel desconhecida: ${modelClass.name}")
        }
    }
} 