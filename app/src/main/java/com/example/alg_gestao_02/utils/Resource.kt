package com.example.alg_gestao_02.utils

/**
 * Classe para encapsular diferentes estados de resposta das operações
 */
sealed class Resource<out T> {
    /**
     * Operação bem-sucedida com dados
     */
    data class Success<out T>(override val data: T) : Resource<T>() {
        override val message: String? = null
    }
    
    /**
     * Operação em andamento
     */
    object Loading : Resource<Nothing>() {
        override val data: Nothing? = null
        override val message: String? = null
    }
    
    /**
     * Operação com erro
     */
    data class Error(override val message: String) : Resource<Nothing>() {
        override val data: Nothing? = null
    }
    
    /**
     * Propriedade para acessar os dados (null se não for Success)
     */
    abstract val data: T?
    
    /**
     * Propriedade para acessar a mensagem de erro (null se não for Error)
     */
    abstract val message: String?
    
    /**
     * Verifica se o recurso é um sucesso
     */
    fun isSuccess(): Boolean {
        return this is Success
    }
    
    /**
     * Verifica se o recurso é um erro
     */
    fun isError(): Boolean {
        return this is Error
    }
    
    /**
     * Verifica se o recurso está carregando
     */
    fun isLoading(): Boolean {
        return this is Loading
    }
} 