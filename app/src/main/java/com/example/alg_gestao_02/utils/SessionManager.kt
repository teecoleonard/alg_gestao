package com.example.alg_gestao_02.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.alg_gestao_02.data.models.User
import java.util.Date

/**
 * Gerencia a sessão do usuário e armazena dados de autenticação
 */
class SessionManager(context: Context) {
    private var prefs: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()
    
    companion object {
        private const val PREF_NAME = "ALG_Gestao_Session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_EXPIRES_IN = "expiresIn"
        
        // 30 dias em milissegundos
        private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    }
    
    /**
     * Verifica se existe uma sessão ativa
     */
    fun isLoggedIn(): Boolean {
        if (!prefs.contains(KEY_TOKEN)) {
            return false
        }
        
        val expiresIn = prefs.getLong(KEY_EXPIRES_IN, 0)
        val isExpired = expiresIn < System.currentTimeMillis()
        
        if (isExpired) {
            LogUtils.info("SessionManager", "Sessão expirada")
            logout()
            return false
        }
        
        return true
    }
    
    /**
     * Salva os dados da sessão após o login
     */
    fun saveUserSession(token: String, user: User) {
        LogUtils.debug("SessionManager", "Salvando sessão para usuário: ${user.id}")
        
        val expiresIn = System.currentTimeMillis() + THIRTY_DAYS_MS
        
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putLong(KEY_EXPIRES_IN, expiresIn)
        editor.apply()
        
        LogUtils.userEvent("login", user.id)
    }
    
    /**
     * Obtém o token de autenticação
     */
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    /**
     * Obtém o ID do usuário logado
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Obtém o nome do usuário logado
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Obtém o email do usuário logado
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Realiza o logout do usuário
     */
    fun logout() {
        LogUtils.userEvent("logout", getUserId())
        editor.clear()
        editor.apply()
    }
} 