package com.example.alg_gestao_02.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.R
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Classe responsável por centralizar o tratamento de erros em toda a aplicação.
 * Provê métodos padronizados para exibição de mensagens de erro e registro de logs.
 */
class ErrorHandler {
    
    companion object {
        // Tipos de erros conhecidos
        enum class ErrorType {
            NETWORK,
            API,
            DATABASE,
            AUTH,
            VALIDATION,
            UNKNOWN
        }
        
        /**
         * Determina o tipo de erro baseado na exceção
         */
        fun getErrorTypeFromException(exception: Throwable): ErrorType {
            return when (exception) {
                is IOException,
                is SocketTimeoutException,
                is UnknownHostException -> ErrorType.NETWORK
                // Adicionar HttpException quando implementar Retrofit
                // is HttpException -> ErrorType.API
                // Adicione outros tipos conforme necessário
                else -> ErrorType.UNKNOWN
            }
        }
        
        /**
         * Obtém uma mensagem amigável baseada no tipo de erro
         */
        fun getFriendlyMessage(context: Context, type: ErrorType, exception: Throwable?): String {
            return when (type) {
                ErrorType.NETWORK -> context.getString(R.string.error_network)
                ErrorType.API -> context.getString(R.string.error_api)
                ErrorType.DATABASE -> context.getString(R.string.error_database)
                ErrorType.AUTH -> context.getString(R.string.error_auth)
                ErrorType.VALIDATION -> context.getString(R.string.error_validation)
                ErrorType.UNKNOWN -> exception?.message 
                    ?: context.getString(R.string.error_unknown)
            }
        }
        
        /**
         * Método para tratar e exibir o erro em um Fragment usando Snackbar
         */
        fun handleError(
            fragment: Fragment,
            exception: Throwable,
            actionLabel: String? = null,
            onRetry: (() -> Unit)? = null
        ) {
            val errorType = getErrorTypeFromException(exception)
            val message = getFriendlyMessage(fragment.requireContext(), errorType, exception)
            
            // Registra o erro no LogUtils
            LogUtils.error(
                fragment.javaClass.simpleName,
                "Erro ($errorType): ${exception.message}",
                exception
            )
            
            // Exibe um Snackbar com a mensagem de erro
            val snackbar = Snackbar.make(
                fragment.requireView(),
                message,
                Snackbar.LENGTH_LONG
            )
            
            // Adiciona ação de retry se fornecida
            if (actionLabel != null && onRetry != null) {
                snackbar.setAction(actionLabel) { onRetry() }
            }
            
            snackbar.show()
        }
        
        /**
         * Método para tratar e exibir o erro em uma Activity usando Toast
         */
        fun handleError(
            context: Context,
            exception: Throwable,
            tag: String
        ) {
            val errorType = getErrorTypeFromException(exception)
            val message = getFriendlyMessage(context, errorType, exception)
            
            // Registra o erro no LogUtils
            LogUtils.error(tag, "Erro ($errorType): ${exception.message}", exception)
            
            // Exibe um Toast com a mensagem de erro
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        
        /**
         * Método para registrar erros sem exibir UI
         */
        fun logError(tag: String, exception: Throwable, message: String? = null) {
            LogUtils.error(tag, message ?: "Erro: ${exception.message}", exception)
        }
        
        /**
         * Envia o erro para um serviço de monitoramento remoto (implementação futura)
         */
        private fun sendErrorToRemoteMonitoring(exception: Throwable, errorType: ErrorType, tag: String) {
            // TODO: Implementar integração com ferramentas como Firebase Crashlytics, 
            // Google Analytics, Sentry, etc.
            
            // Exemplo com Firebase Crashlytics:
            // Crashlytics.log("$tag: ${exception.message}")
            // Crashlytics.recordException(exception)
        }
    }
} 