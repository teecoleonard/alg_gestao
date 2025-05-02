package com.example.alg_gestao_02.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Classe auxiliar para padronizar e centralizar o tratamento de erros nos ViewModels.
 * Simplifica a interface do ErrorViewModel para uso mais direto nos ViewModels.
 */
class ErrorHandler(private val tag: String) : ErrorViewModel() {
    
    /**
     * Trata uma mensagem de erro simples
     */
    fun handleError(errorMessage: String?) {
        val message = errorMessage ?: "Erro desconhecido"
        LogUtils.error(tag, message)
        val errorEvent = ErrorEvent(ErrorType.GENERAL, message)
        postErrorEvent(errorEvent)
    }
    
    /**
     * Trata uma exceção
     */
    fun handleException(exception: Throwable) {
        handleException(exception, tag)
    }
    
    /**
     * Publica um evento de erro
     */
    private fun postErrorEvent(errorEvent: ErrorEvent) {
        _errorEvent.postValue(Event(errorEvent))
    }
} 