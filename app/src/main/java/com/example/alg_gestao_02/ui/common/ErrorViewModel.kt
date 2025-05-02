package com.example.alg_gestao_02.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.alg_gestao_02.utils.LogUtils
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Classe auxiliar para padronizar e centralizar o tratamento de erros nos ViewModels.
 * Implementa o padrão Event para evitar que eventos sejam processados múltiplas vezes.
 */
open class ErrorViewModel {
    
    // LiveData privado para erros
    protected val _errorEvent = MutableLiveData<Event<ErrorEvent>>()
    
    // LiveData público para observação externa
    val errorEvent: LiveData<Event<ErrorEvent>> = _errorEvent
    
    /**
     * Processa um erro e publica um evento de erro apropriado
     */
    fun handleException(exception: Throwable, tag: String, shouldLog: Boolean = true) {
        // Loga o erro se necessário
        if (shouldLog) {
            LogUtils.error(tag, "Erro: ${exception.message}", exception)
        }
        
        // Determina o tipo de erro com base na exceção
        val errorType = when (exception) {
            is IOException, is SocketTimeoutException -> ErrorType.NETWORK
            else -> ErrorType.GENERAL
        }
        
        // Determina a mensagem de erro
        val errorMessage = determineErrorMessage(exception, errorType)
        
        // Cria e publica o evento de erro
        _errorEvent.postValue(Event(ErrorEvent(errorType, errorMessage, exception)))
    }
    
    /**
     * Determina a mensagem de erro com base no tipo e na exceção
     */
    private fun determineErrorMessage(exception: Throwable, errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NETWORK -> "Falha na conexão. Verifique sua internet e tente novamente."
            ErrorType.API -> "O servidor não pôde processar sua solicitação. Tente novamente."
            ErrorType.DATABASE -> "Erro ao acessar dados locais. Tente reiniciar o aplicativo."
            ErrorType.GENERAL -> exception.message ?: "Ocorreu um erro inesperado."
        }
    }
    
    /**
     * Representa o tipo de erro que ocorreu
     */
    enum class ErrorType {
        NETWORK,   // Erro de rede/conectividade
        API,       // Erro na API (servidor)
        DATABASE,  // Erro no banco de dados local
        GENERAL    // Outros erros
    }
    
    /**
     * Evento de erro que contém informações detalhadas sobre o erro
     */
    data class ErrorEvent(
        val type: ErrorType,
        val message: String,
        val exception: Throwable? = null
    )
    
    /**
     * Wrapper para um evento que só deve ser consumido uma vez.
     * Resolvendo o problema de LiveData com mudanças de configuração.
     */
    class Event<T>(private val content: T) {
        private var hasBeenHandled = false
        
        /**
         * Retorna o conteúdo e impede seu uso novamente.
         */
        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }
        
        /**
         * Retorna o conteúdo, mesmo que já tenha sido tratado.
         */
        fun peekContent(): T = content
    }
} 