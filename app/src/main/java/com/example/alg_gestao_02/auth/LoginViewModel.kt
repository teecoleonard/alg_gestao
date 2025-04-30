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

class LoginViewModel : ViewModel() {
    
    // Estados possíveis do login
    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val token: String, val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }
    
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    private val authRepository = AuthRepository()
    
    /**
     * Realiza o login do usuário
     * @param cpf CPF do usuário
     * @param senha Senha do usuário
     */
    fun login(cpf: String, senha: String) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            LogUtils.debug("LoginViewModel", "Tentando login para: $cpf")
            
            when (val result = authRepository.login(cpf, senha)) {
                is Resource.Success -> {
                    val response = result.data
                    LogUtils.info("LoginViewModel", "Login bem-sucedido para: $cpf")
                    _loginState.value = LoginState.Success(response.token, response.user)
                }
                
                is Resource.Error -> {
                    LogUtils.warning("LoginViewModel", "Credenciais inválidas para: $cpf")
                    _loginState.value = LoginState.Error(result.message)
                }
                
                else -> { /* Ignorar estado de carregamento */ }
            }
        }
    }
} 