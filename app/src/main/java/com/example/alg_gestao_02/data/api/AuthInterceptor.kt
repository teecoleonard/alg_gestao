package com.example.alg_gestao_02.data.api

import com.example.alg_gestao_02.utils.SessionManager
import com.example.alg_gestao_02.utils.LogUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que adiciona o token de autenticação em todas as requisições
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Log para requisições de devolução
        if (originalRequest.url.toString().contains("/devolucoes") && originalRequest.method == "PUT") {
            LogUtils.debug("AuthInterceptor", "Interceptando requisição PUT para devolução: ${originalRequest.url}")
        }
        
        // Não adiciona token para endpoints de autenticação
        if (originalRequest.url.encodedPath.contains("/login") || 
            originalRequest.url.encodedPath.contains("/register")) {
            LogUtils.debug("AuthInterceptor", "Endpoint de autenticação detectado, não adicionando token")
            return chain.proceed(originalRequest)
        }
        
        // Obtém o token de autenticação
        val token = sessionManager.getToken()
        
        // Log para debugging
        if (originalRequest.url.toString().contains("/devolucoes") && originalRequest.method == "PUT") {
            if (token.isNullOrEmpty()) {
                LogUtils.error("AuthInterceptor", "❌ TOKEN NÃO DISPONÍVEL para requisição de devolução!")
            } else {
                LogUtils.debug("AuthInterceptor", "✅ Token disponível para devolução (${token.take(10)}...)")
            }
        }
        
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