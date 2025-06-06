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
        
        // Log para requisições de dashboard
        if (originalRequest.url.toString().contains("/dashboard")) {
            LogUtils.info("AuthInterceptor", "🔍 ========== INTERCEPTANDO DASHBOARD ==========")
            LogUtils.info("AuthInterceptor", "🌐 URL: ${originalRequest.url}")
            LogUtils.info("AuthInterceptor", "📡 Método: ${originalRequest.method}")
        }
        
        // Não adiciona token para endpoints de autenticação
        if (originalRequest.url.encodedPath.contains("/login") || 
            originalRequest.url.encodedPath.contains("/register")) {
            LogUtils.debug("AuthInterceptor", "Endpoint de autenticação detectado, não adicionando token")
            return chain.proceed(originalRequest)
        }
        
        // Obtém o token de autenticação
        val token = sessionManager.getToken()
        
        // Log detalhado para dashboard
        if (originalRequest.url.toString().contains("/dashboard")) {
            LogUtils.info("AuthInterceptor", "🔍 Verificando estado da sessão:")
            LogUtils.info("AuthInterceptor", "📋 isLoggedIn: ${sessionManager.isLoggedIn()}")
            LogUtils.info("AuthInterceptor", "👤 User ID: ${sessionManager.getUserId()}")
            
            if (token.isNullOrEmpty()) {
                LogUtils.error("AuthInterceptor", "❌ TOKEN NÃO DISPONÍVEL para requisição de dashboard!")
                LogUtils.error("AuthInterceptor", "🚨 PROBLEMA: Interceptor não conseguiu obter token!")
            } else {
                LogUtils.info("AuthInterceptor", "✅ Token disponível para dashboard (${token.take(30)}...)")
                LogUtils.info("AuthInterceptor", "📏 Tamanho do token: ${token.length} caracteres")
            }
        }
        
        // Log para requisições de devolução
        if (originalRequest.url.toString().contains("/devolucoes") && originalRequest.method == "PUT") {
            if (token.isNullOrEmpty()) {
                LogUtils.error("AuthInterceptor", "❌ TOKEN NÃO DISPONÍVEL para requisição de devolução!")
            } else {
                LogUtils.debug("AuthInterceptor", "✅ Token disponível para devolução (${token.take(10)}...)")
            }
        }
        
        // Se não houver token, prossegue com a requisição original
        if (token.isNullOrEmpty()) {
            if (originalRequest.url.toString().contains("/dashboard")) {
                LogUtils.error("AuthInterceptor", "⚠️ Enviando requisição SEM TOKEN para dashboard!")
                LogUtils.info("AuthInterceptor", "🔍 ==========================================")
            }
            return chain.proceed(originalRequest)
        }
        
        // Adiciona o token no cabeçalho da requisição
        val requestWithToken = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
            
        // Log para dashboard
        if (originalRequest.url.toString().contains("/dashboard")) {
            LogUtils.info("AuthInterceptor", "📤 Token adicionado ao header Authorization")
            LogUtils.info("AuthInterceptor", "🔐 Header: Bearer ${token.take(30)}...")
            LogUtils.info("AuthInterceptor", "🔍 ==========================================")
        }
            
        return chain.proceed(requestWithToken)
    }
} 