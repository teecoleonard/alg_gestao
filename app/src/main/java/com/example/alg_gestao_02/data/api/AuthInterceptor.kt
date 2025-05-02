package com.example.alg_gestao_02.data.api

import com.example.alg_gestao_02.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que adiciona o token de autenticação em todas as requisições
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Não adiciona token para endpoints de autenticação
        if (originalRequest.url.encodedPath.contains("/login") || 
            originalRequest.url.encodedPath.contains("/register")) {
            return chain.proceed(originalRequest)
        }
        
        // Obtém o token de autenticação
        val token = sessionManager.getToken()
        
        // Se não houver token, prossegue com a requisição original
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // Adiciona o token no cabeçalho da requisição
        val requestWithToken = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
            
        return chain.proceed(requestWithToken)
    }
} 