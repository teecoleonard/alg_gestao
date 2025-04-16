package com.example.alg_gestao_02.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.User
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.delay
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
    
    /**
     * Realiza o login com mock (fake) para demonstração
     */
    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            LogUtils.debug("LoginViewModel", "Tentando login para: $email")
            
            // Simula uma chamada de API com delay
            delay(1500)
            
            try {
                // Credenciais para login fake (apenas para teste)
                if (email == "admin@alg.com" && password == "123456") {
                    // Login bem-sucedido
                    val mockUser = User(
                        id = "1",
                        name = "Administrador",
                        email = email,
                        role = "admin"
                    )
                    
                    val mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IkFkbWluaXN0cmFkb3IiLCJpYXQiOjE2Mjk3NDg4MDB9"
                    
                    LogUtils.info("LoginViewModel", "Login bem-sucedido para: $email")
                    _loginState.value = LoginState.Success(mockToken, mockUser)
                } else {
                    // Credenciais inválidas
                    LogUtils.warning("LoginViewModel", "Credenciais inválidas para: $email")
                    _loginState.value = LoginState.Error("E-mail ou senha incorretos")
                }
            } catch (e: Exception) {
                // Erro genérico
                LogUtils.error("LoginViewModel", "Erro ao fazer login", e)
                _loginState.value = LoginState.Error("Ocorreu um erro ao tentar fazer login. Tente novamente.")
            }
        }
    }
} 