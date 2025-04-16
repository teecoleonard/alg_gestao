package com.example.alg_gestao_02.utils

import android.util.Log
import com.example.alg_gestao_02.BuildConfig

/**
 * Utilitário para gerenciamento de logs estruturados no aplicativo
 */
object LogUtils {
    private const val TAG_PREFIX = "ALG_Gestao_"
    private val isDebug = BuildConfig.DEBUG
    
    /**
     * Registra uma mensagem de log de depuração
     */
    fun debug(tag: String, message: String) {
        if (isDebug) {
            Log.d("$TAG_PREFIX$tag", message)
        }
    }
    
    /**
     * Registra uma mensagem de log informativa
     */
    fun info(tag: String, message: String) {
        if (isDebug) {
            Log.i("$TAG_PREFIX$tag", message)
        }
    }
    
    /**
     * Registra uma mensagem de log de aviso
     */
    fun warning(tag: String, message: String) {
        Log.w("$TAG_PREFIX$tag", message)
    }
    
    /**
     * Registra uma mensagem de log de erro
     */
    fun error(tag: String, message: String, exception: Throwable? = null) {
        if (exception != null) {
            Log.e("$TAG_PREFIX$tag", message, exception)
        } else {
            Log.e("$TAG_PREFIX$tag", message)
        }
    }
    
    /**
     * Registra eventos de rede
     */
    fun network(endpoint: String, method: String, responseCode: Int) {
        if (isDebug) {
            Log.d("${TAG_PREFIX}Network", "$method $endpoint - Código: $responseCode")
        }
    }
    
    /**
     * Registra eventos de usuário (login, logout, etc.)
     */
    fun userEvent(action: String, userId: String? = null) {
        val userInfo = userId?.let { " (userId: $it)" } ?: ""
        Log.i("${TAG_PREFIX}UserEvent", "$action$userInfo")
    }
} 