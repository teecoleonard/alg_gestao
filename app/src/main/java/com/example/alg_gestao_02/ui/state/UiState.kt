package com.example.alg_gestao_02.ui.state

/**
 * Classe selada para representar os diferentes estados da UI.
 * Usada para comunicar o estado atual da tela de forma consistente.
 */
sealed class UiState<out T> {
    /**
     * Estado inicial ou de carregamento
     */
    class Loading<T> : UiState<T>()
    
    /**
     * Estado de erro, com mensagem opcional
     */
    data class Error<T>(val message: String) : UiState<T>()
    
    /**
     * Estado vazio, quando não há dados para exibir
     */
    class Empty<T> : UiState<T>()
    
    /**
     * Estado de sucesso, com os dados carregados
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    companion object {
        fun <T> loading(): UiState<T> = Loading()
        fun <T> empty(): UiState<T> = Empty()
        fun <T> success(data: T): UiState<T> = Success(data)
        fun <T> error(message: String): UiState<T> = Error(message)
    }
} 