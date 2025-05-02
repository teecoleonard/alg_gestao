package com.example.alg_gestao_02.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.api.ApiService
import com.example.alg_gestao_02.data.models.User
import com.example.alg_gestao_02.data.repository.AuthRepository
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    
    // Estados possíveis do registro
    sealed class RegisterState {
        object Loading : RegisterState()
        data class Success(val user: User) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
    
    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState
    
    private val authRepository = AuthRepository()
    
    /**
     * Registra um novo usuário
     * @param cpf CPF do usuário
     * @param nome Nome do usuário
     * @param senha Senha do usuário
     * @param role Papel do usuário (cliente ou funcionario)
     */
    fun register(cpf: String, nome: String, senha: String, role: String) {
        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            LogUtils.debug("RegisterViewModel", "Tentando registrar usuário: $nome")
            
            when (val result = authRepository.register(cpf, nome, senha, role)) {
                is Resource.Success -> {
                    val response = result.data
                    LogUtils.info("RegisterViewModel", "Registro bem-sucedido para: $nome")
                    _registerState.value = RegisterState.Success(response.user)
                }
                
                is Resource.Error -> {
                    LogUtils.warning("RegisterViewModel", "Falha ao registrar: ${result.message}")
                    _registerState.value = RegisterState.Error(result.message)
                }
                
                else -> { /* Ignorar estado de carregamento */ }
            }
        }
    }
} 